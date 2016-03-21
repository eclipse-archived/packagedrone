/*******************************************************************************
 * Copyright (c) 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.channel.impl.transfer;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.eclipse.packagedrone.VersionInformation;
import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.XmlHelper;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.eclipse.packagedrone.repo.channel.ChannelDetails;
import org.eclipse.packagedrone.repo.channel.ChannelId;
import org.eclipse.packagedrone.repo.channel.ChannelService;
import org.eclipse.packagedrone.repo.channel.ChannelService.By;
import org.eclipse.packagedrone.repo.channel.DescriptorAdapter;
import org.eclipse.packagedrone.repo.channel.ModifiableChannel;
import org.eclipse.packagedrone.repo.channel.ReadableChannel;
import org.eclipse.packagedrone.repo.channel.transfer.TransferService;
import org.eclipse.packagedrone.utils.xml.XmlToolsFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class TransferServiceImpl implements TransferService
{
    private ChannelService channelService;

    private XmlToolsFactory xmlToolsFactory;

    public void setChannelService ( final ChannelService channelService )
    {
        this.channelService = channelService;
    }

    public void setXmlToolsFactory ( final XmlToolsFactory xmlToolsFactory )
    {
        this.xmlToolsFactory = xmlToolsFactory;
    }

    private class Entry
    {
        private final Map<String, Entry> children = new HashMap<> ();

        private ZipEntry zipEntry;

        private final List<String> ids;

        public Entry ()
        {
            this.ids = Collections.emptyList ();
        }

        public Entry ( final List<String> ids )
        {
            this.ids = ids;
        }

        public void addChild ( final LinkedList<String> path, final ZipEntry zipEntry )
        {
            final String seg = path.pop ();

            Entry child = this.children.get ( seg );
            if ( child == null )
            {
                final List<String> ids = new ArrayList<> ( this.ids );
                ids.add ( seg );
                child = new Entry ( ids );
                this.children.put ( seg, child );
            }

            if ( path.isEmpty () )
            {
                child.zipEntry = zipEntry;
            }
            else
            {
                child.addChild ( path, zipEntry );
            }
        }

        public void store ( final ZipFile zip, final ModifiableChannel channel, final Optional<ArtifactInformation> parent ) throws Exception
        {
            for ( final Entry child : this.children.values () )
            {
                final String baseName = String.format ( "%s%s/", "artifacts/", child.ids.stream ().collect ( Collectors.joining ( "/" ) ) );

                final String name = getData ( zip, baseName + "name" );

                final String generatorId = getData ( zip, baseName + "generator" );

                final ArtifactInformation result;
                try ( final InputStream stream = zip.getInputStream ( child.zipEntry ) )
                {
                    final Map<MetaKey, String> providedMetaData = getProperties ( zip, baseName + "properties.xml" );

                    if ( generatorId != null )
                    {
                        result = channel.getContext ().createGeneratorArtifact ( generatorId, stream, name, providedMetaData );
                    }
                    else
                    {
                        final String parentId = parent.map ( ArtifactInformation::getId ).orElse ( null );
                        result = channel.getContext ().createArtifact ( parentId, stream, name, providedMetaData );
                    }
                }

                if ( result != null )
                {
                    // only add children if we did not get a veto
                    child.store ( zip, channel, Optional.of ( result ) );
                }
            }
        }
    }

    @Override
    public void importAll ( final InputStream inputStream, final boolean useChannelNames, final boolean wipe ) throws IOException
    {
        ZipEntry ze;

        if ( wipe )
        {
            this.channelService.wipeClean ();
        }

        final ZipInputStream zis = new ZipInputStream ( inputStream );
        while ( ( ze = zis.getNextEntry () ) != null )
        {
            if ( ze.isDirectory () )
            {
                continue;
            }

            final String name = ze.getName ();
            if ( !name.endsWith ( ".zip" ) )
            {
                continue;
            }

            importChannel ( zis, useChannelNames );
        }
    }

    @Override
    public ChannelId importChannel ( final InputStream inputStream, final boolean useChannelName ) throws IOException
    {
        final Path tmp = Files.createTempFile ( "imp", null );
        try
        {
            try ( OutputStream tmpStream = new BufferedOutputStream ( new FileOutputStream ( tmp.toFile () ) ) )
            {
                ByteStreams.copy ( inputStream, tmpStream );
            }

            return processImport ( tmp, useChannelName );
        }
        finally
        {
            Files.deleteIfExists ( tmp );
        }
    }

    private ChannelId processImport ( final Path tmp, final boolean useChannelName ) throws IOException
    {
        try ( final ZipFile zip = new ZipFile ( tmp.toFile () ) )
        {
            final String version = getData ( zip, "version" );

            if ( !"1".equals ( version ) )
            {
                throw new IllegalArgumentException ( String.format ( "Version '%s' is not supported", version ) );
            }

            // read basic channel data

            final String name = getData ( zip, "name" );
            final Set<String> names = parseNames ( getData ( zip, "names" ) );

            if ( name != null )
            {
                names.add ( name );
            }

            final String description = getData ( zip, "description" );
            final Map<MetaKey, String> properties = getProperties ( zip, "properties.xml" );
            final Set<String> aspects = getAspects ( zip );

            // create the channel

            final ChannelDetails details = new ChannelDetails ();
            details.setDescription ( description );
            final ChannelId channelId = this.channelService.create ( "apm", details, Collections.emptyMap () );

            // apply the name if required and present

            if ( useChannelName && !names.isEmpty () )
            {
                this.channelService.accessRun ( By.id ( channelId.getId () ), DescriptorAdapter.class, channel -> {
                    channel.setNames ( names );
                } );
            }

            this.channelService.accessRun ( By.id ( channelId.getId () ), ModifiableChannel.class, channel -> {

                // set provided meta data

                if ( properties != null && !properties.isEmpty () )
                {
                    channel.applyMetaData ( properties );
                }

                // process artifacts

                processArtifacts ( channel, zip );

                // finally enable the aspects

                if ( aspects != null && !aspects.isEmpty () )
                {
                    channel.getContext ().addAspects ( aspects );
                }

            } );

            return channelId;
        }
    }

    public void processArtifacts ( final ModifiableChannel channel, final ZipFile zip ) throws IOException
    {
        // first gather a artifacts

        final Enumeration<? extends ZipEntry> entries = zip.entries ();

        final Entry root = new Entry ();
        while ( entries.hasMoreElements () )
        {
            final ZipEntry ze = entries.nextElement ();

            final String name = ze.getName ();
            if ( name.startsWith ( "artifacts/" ) && name.endsWith ( "/data" ) )
            {
                final List<String> segs = Arrays.asList ( name.split ( "\\/" ) );
                root.addChild ( new LinkedList<> ( segs.subList ( 1, segs.size () - 1 ) ), ze );
            }
        }

        // now import them hierarchically

        try
        {
            root.store ( zip, channel, Optional.empty () );
        }
        catch ( final Exception e )
        {
            throw new IOException ( "Failed to import artifacts", e );
        }
    }

    private Map<MetaKey, String> getProperties ( final ZipFile zip, final String name ) throws IOException
    {
        final ZipEntry ze = zip.getEntry ( name );
        if ( ze == null )
        {
            return null;
        }

        try ( InputStream stream = zip.getInputStream ( ze ) )
        {
            return readProperties ( stream );
        }
    }

    private String getData ( final ZipFile zip, final String name ) throws IOException
    {
        final ZipEntry ze = zip.getEntry ( name );
        if ( ze == null )
        {
            return null;
        }

        try ( Reader reader = new InputStreamReader ( zip.getInputStream ( ze ), StandardCharsets.UTF_8 ) )
        {
            return CharStreams.toString ( reader );
        }
    }

    /**
     * Read in a map of properties
     *
     * @param stream
     *            input stream
     * @return the map of properties
     * @throws IOException
     *             if anything goes wrong reading the file
     */
    private Map<MetaKey, String> readProperties ( final InputStream stream ) throws IOException
    {
        try
        {
            // wrap the input stream since we don't want the XML parser to close the stream while parsing

            final Document doc = this.xmlToolsFactory.newDocumentBuilder ().parse ( new FilterInputStream ( stream ) {
                @Override
                public void close ()
                {
                    // do nothing
                }
            } );

            final Element root = doc.getDocumentElement ();
            if ( !"properties".equals ( root.getNodeName () ) )
            {
                throw new IllegalStateException ( String.format ( "Root element must be of type '%s'", "properties" ) );
            }

            final Map<MetaKey, String> result = new HashMap<> ();

            for ( final Element ele : XmlHelper.iterElement ( root, "property" ) )
            {
                final String namespace = ele.getAttribute ( "namespace" );
                final String key = ele.getAttribute ( "key" );
                final String value = ele.getTextContent ();

                if ( namespace.isEmpty () || key.isEmpty () )
                {
                    continue;
                }

                result.put ( new MetaKey ( namespace, key ), value );
            }

            return result;
        }
        catch ( final Exception e )
        {
            throw new IOException ( "Failed to read properties", e );
        }
    }

    private Set<String> getAspects ( final ZipFile zip ) throws IOException
    {
        final ZipEntry ze = zip.getEntry ( "aspects" );
        if ( ze == null )
        {
            return Collections.emptySet ();
        }

        try ( InputStream stream = zip.getInputStream ( ze ) )
        {
            final List<String> lines = CharStreams.readLines ( new InputStreamReader ( stream, StandardCharsets.UTF_8 ) );
            return new HashSet<> ( lines );
        }
    }

    protected void initExportFile ( final ZipOutputStream zos ) throws IOException
    {
        putDataEntry ( zos, "version", "1" );
        putDataEntry ( zos, "droneVersion", VersionInformation.VERSION );
    }

    @Override
    public void exportAll ( final OutputStream stream ) throws IOException
    {
        final ZipOutputStream zos = new ZipOutputStream ( stream );

        initExportFile ( zos );

        final Collection<? extends ChannelId> ids = this.channelService.list ();

        // TODO: run exportAll inside a channel service lock

        for ( final ChannelId channelId : ids )
        {
            zos.putNextEntry ( new ZipEntry ( String.format ( "%s.zip", channelId.getId () ) ) );
            exportChannel ( By.id ( channelId.getId () ), zos );
            zos.closeEntry ();
        }
        zos.finish ();
    }

    /**
     * Export the content of a channel
     *
     * @param by
     *            the channel to export
     * @param stream
     *            the stream to write the export file to
     * @throws IOException
     *             if the export cannot be performed
     */
    private void exportChannel ( final By by, final OutputStream stream ) throws IOException
    {
        final ZipOutputStream zos = new ZipOutputStream ( stream );

        initExportFile ( zos );

        this.channelService.accessRun ( by, ReadableChannel.class, channel -> {

            putDataEntry ( zos, "names", makeNames ( channel.getId () ) );
            putDataEntry ( zos, "description", channel.getId ().getDescription () );
            putDirEntry ( zos, "artifacts" );
            putProperties ( zos, "properties.xml", channel.getContext ().getProvidedMetaData () );
            putAspects ( zos, channel.getContext ().getAspectStates ().keySet () );

            // the first run receives all artifacts and filters for the root elements

            putArtifacts ( zos, "artifacts/", channel, channel.getArtifacts (), true );

        } );

        zos.finish (); // don't close stream, since there might be other channels following
    }

    private Set<String> parseNames ( final String data )
    {
        if ( data == null )
        {
            // we must return a modifiable list
            return new HashSet<> ( 1 );
        }

        final Gson gson = new GsonBuilder ().create ();
        final String[] names = gson.fromJson ( data, String[].class );
        return new HashSet<> ( Arrays.asList ( names ) );
    }

    private String makeNames ( final ChannelId channelId )
    {
        final String[] names = channelId.getNames ().toArray ( new String[channelId.getNames ().size ()] );
        if ( names.length == 0 )
        {
            return null;
        }

        Arrays.sort ( names );

        return new GsonBuilder ().create ().toJson ( names );
    }

    @Override
    public void exportChannel ( final String channelId, final OutputStream stream ) throws IOException
    {
        exportChannel ( By.id ( channelId ), stream );
    }

    private void putArtifacts ( final ZipOutputStream zos, final String baseName, final ReadableChannel channel, final Collection<? extends ArtifactInformation> inputArtifacts, final boolean onlyRoot ) throws IOException
    {
        final List<ArtifactInformation> artifacts = new ArrayList<> ( inputArtifacts ); // make new instance in order to sort them
        Collections.sort ( artifacts, Comparator.comparing ( ArtifactInformation::getId ) );

        for ( final ArtifactInformation art : artifacts )
        {
            if ( !art.is ( "stored" ) )
            {
                continue;
            }

            if ( onlyRoot && art.getParentId () != null )
            {
                // only root artifacts in this run
                continue;
            }

            final String name = String.format ( "%s%s/", baseName, art.getId () );

            // make a dir entry

            {
                final ZipEntry ze = new ZipEntry ( name );
                ze.setComment ( art.getName () );

                final FileTime timestamp = FileTime.fromMillis ( art.getCreationTimestamp ().getTime () );

                ze.setLastModifiedTime ( timestamp );
                ze.setCreationTime ( timestamp );
                ze.setLastAccessTime ( timestamp );

                zos.putNextEntry ( ze );
                zos.closeEntry ();
            }

            // put the provided properties

            putProperties ( zos, name + "properties.xml", art.getProvidedMetaData () );
            putDataEntry ( zos, name + "name", art.getName () );

            if ( art.is ( "generator" ) )
            {
                putDataEntry ( zos, name + "generator", art.getVirtualizerAspectId () );
            }

            // put the blob

            try
            {
                channel.getContext ().stream ( art.getId (), in -> {
                    zos.putNextEntry ( new ZipEntry ( name + "data" ) );
                    ByteStreams.copy ( in, zos );
                    zos.closeEntry ();
                } );
            }
            catch ( final Exception e )
            {
                throw new IOException ( "Failed to export artifact", e );
            }

            // further calls will process all artifacts but only get a filtered list

            final List<? extends ArtifactInformation> childs = art.getChildIds ().stream ().map ( id -> channel.getArtifact ( id ) ).filter ( opt -> opt.isPresent () ).map ( opt -> opt.get () ).collect ( Collectors.toList () );

            // put children of this entry

            putArtifacts ( zos, name, channel, childs, false );
        }
    }

    private void putAspects ( final ZipOutputStream zos, final Set<String> aspects ) throws IOException
    {
        final List<String> list = new ArrayList<> ( aspects );
        Collections.sort ( list );

        final StringBuilder sb = new StringBuilder ();

        for ( final String aspect : list )
        {
            sb.append ( aspect ).append ( '\n' );
        }

        putDataEntry ( zos, "aspects", sb.toString () );
    }

    private void putProperties ( final ZipOutputStream zos, final String name, final Map<MetaKey, String> providedMetaData ) throws IOException
    {
        if ( providedMetaData.isEmpty () )
        {
            return;
        }

        zos.putNextEntry ( new ZipEntry ( name ) );

        final XmlHelper xml = new XmlHelper ();

        final Document doc = xml.create ();
        final Element root = doc.createElement ( "properties" );
        doc.appendChild ( root );

        final SortedMap<MetaKey, String> sorted = new TreeMap<> ( providedMetaData );

        for ( final Map.Entry<MetaKey, String> entry : sorted.entrySet () )
        {
            final Element p = XmlHelper.addElement ( root, "property" );
            p.setAttribute ( "namespace", entry.getKey ().getNamespace () );
            p.setAttribute ( "key", entry.getKey ().getKey () );
            p.setTextContent ( entry.getValue () );
        }

        try
        {

            xml.write ( doc, zos );
            zos.closeEntry ();
        }
        catch ( final Exception e )
        {
            throw new IOException ( "Failed to serialize XML", e );
        }
    }

    private void putDirEntry ( final ZipOutputStream zos, String name ) throws IOException
    {
        if ( !name.endsWith ( "/" ) )
        {
            name = name + "/";
        }

        final ZipEntry entry = new ZipEntry ( name );
        zos.putNextEntry ( entry );
        zos.closeEntry ();
    }

    private void putDataEntry ( final ZipOutputStream stream, final String name, final String data ) throws IOException
    {
        if ( data == null )
        {
            return;
        }
        stream.putNextEntry ( new ZipEntry ( name ) );
        stream.write ( data.getBytes ( StandardCharsets.UTF_8 ) );
        stream.closeEntry ();
    }

}
