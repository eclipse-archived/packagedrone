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
package org.eclipse.packagedrone.repo.adapter.maven;

import static org.eclipse.packagedrone.repo.XmlHelper.addElement;
import static org.eclipse.packagedrone.repo.XmlHelper.addElementFirst;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.XmlHelper;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.eclipse.scada.utils.str.StringHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ChannelData
{
    protected static final DateFormat DATE_FORMAT = new SimpleDateFormat ( "yyyyMMddHHmmss" );

    private static final Pattern SNAPSHOT_PATTERN = Pattern.compile ( "(?<ts>[0-9]{8}-[0-9]{6})-1(?<bn>[0-9]+)" );

    public static abstract class Node
    {
        public boolean isDirectory ()
        {
            return false;
        }
    }

    public static class DirectoryNode extends Node
    {
        private final Map<String, Node> nodes = new HashMap<> ();

        public Map<String, Node> getNodes ()
        {
            return this.nodes;
        }

        @Override
        public boolean isDirectory ()
        {
            return true;
        }
    }

    public abstract static class ContentNode extends Node
    {
        public abstract String getMimeType ();

        public abstract byte[] getData ();
    }

    public static class DataNode extends ContentNode
    {
        private final byte[] data;

        private final String mimeType;

        public DataNode ( final byte[] data, final String mimeType )
        {
            this.data = data;
            this.mimeType = mimeType;
        }

        public DataNode ( final String data, final String mimeType )
        {
            this.data = data.getBytes ( StandardCharsets.UTF_8 );
            this.mimeType = mimeType;
        }

        @Override
        public String getMimeType ()
        {
            return this.mimeType;
        }

        @Override
        public byte[] getData ()
        {
            return this.data;
        }
    }

    public static class ChecksumNode extends ContentNode
    {

        private final ContentNode node;

        private final String alg;

        public ChecksumNode ( final ContentNode node, final String alg )
        {
            this.node = node;
            this.alg = alg;
        }

        @Override
        public String getMimeType ()
        {
            return "text/plain";
        }

        @Override
        public byte[] getData ()
        {
            try
            {
                final MessageDigest md = MessageDigest.getInstance ( this.alg );
                final byte[] result = md.digest ( this.node.getData () );
                return StringHelper.toHex ( result ).getBytes ( StandardCharsets.UTF_8 );
            }
            catch ( final Exception e )
            {
                throw new RuntimeException ( e );
            }
        }

    }

    public static class ArtifactNode extends Node
    {
        private final String artifactId;

        public ArtifactNode ( final String artifactId )
        {
            this.artifactId = artifactId;
        }

        public String getArtifactId ()
        {
            return this.artifactId;
        }
    }

    public static class VersionMetadataNode extends ContentNode
    {
        private final List<MavenInformation> infos = new LinkedList<> ();

        private final Map<MavenInformation, Date> timestamps = new HashMap<> ();

        private final String groupId;

        private final String artifactId;

        private final String version;

        public VersionMetadataNode ( final String groupId, final String artifactId, final String version )
        {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
        }

        @Override
        public String getMimeType ()
        {
            return "application/xml";
        }

        @Override
        public byte[] getData ()
        {
            final XmlHelper xml = new XmlHelper ();

            final Document doc = createMetaData ( this.groupId, this.artifactId, this.version, this.infos, this.timestamps );
            try
            {
                return xml.toData ( doc );
            }
            catch ( final Exception e )
            {
                throw new RuntimeException ( e );
            }
        }

        public void add ( final MavenInformation info, final Date date )
        {
            this.infos.add ( info );
            this.timestamps.put ( info, date );
        }
    }

    public static class ArtifactMetadataNode extends ContentNode
    {
        private final List<MavenInformation> infos = new LinkedList<> ();

        private final String groupId;

        private final String artifactId;

        public ArtifactMetadataNode ( final String groupId, final String artifactId )
        {
            this.groupId = groupId;
            this.artifactId = artifactId;
        }

        @Override
        public String getMimeType ()
        {
            return "application/xml";
        }

        @Override
        public byte[] getData ()
        {
            final XmlHelper xml = new XmlHelper ();

            final Document doc = createMetaData ( this.groupId, this.artifactId, this.infos );
            try
            {
                return xml.toData ( doc );
            }
            catch ( final Exception e )
            {
                throw new RuntimeException ( e );
            }
        }

        public void add ( final MavenInformation info, final Date date )
        {
            this.infos.add ( info );
        }
    }

    private final DirectoryNode root = new DirectoryNode ();

    public void add ( final MavenInformation info, final ArtifactInformation art )
    {
        final String[] gn = info.getGroupId ().split ( "\\." );
        final DirectoryNode groupNode = getGroup ( gn );

        final DirectoryNode artifactBase = addDirNode ( groupNode, info.getArtifactId () );

        final ArtifactMetadataNode mdNode = addArtifactMetaDataNode ( info, artifactBase );

        final DirectoryNode versionNode = addDirNode ( artifactBase, info.getVersion () );

        addNode ( versionNode, info.makeName (), new ArtifactNode ( art.getId () ) );

        addCheckSum ( versionNode, info.makeName (), art, "md5" );
        addCheckSum ( versionNode, info.makeName (), art, "sha1" );

        mdNode.add ( info, art.getCreationTimestamp () );

        if ( info.isSnapshot () )
        {
            final VersionMetadataNode versionMd = addVersionMetaDataNode ( info, versionNode );
            versionMd.add ( info, art.getCreationTimestamp () );
        }
    }

    protected <T extends ContentNode> T addMetaDataNode ( final MavenInformation info, final DirectoryNode base, final Class<T> clazz, final Supplier<T> supp )
    {
        final Node n = base.getNodes ().get ( "maven-metadata.xml" );
        if ( n == null )
        {
            final T result = addNode ( base, "maven-metadata.xml", supp.get () );

            addNode ( base, "maven-metadata.xml.md5", new ChecksumNode ( result, "MD5" ) );
            addNode ( base, "maven-metadata.xml.sha1", new ChecksumNode ( result, "SHA1" ) );

            return result;
        }
        else if ( clazz.isAssignableFrom ( n.getClass () ) )
        {
            return clazz.cast ( n );
        }
        else
        {
            throw new IllegalStateException ( String.format ( "Invalid hierarchy. Someone blocked meta data entry: 'maven-metadata.xml'" ) );
        }
    }

    protected ArtifactMetadataNode addArtifactMetaDataNode ( final MavenInformation info, final DirectoryNode artifactBase )
    {
        return addMetaDataNode ( info, artifactBase, ArtifactMetadataNode.class, () -> new ArtifactMetadataNode ( info.getGroupId (), info.getArtifactId () ) );
    }

    protected VersionMetadataNode addVersionMetaDataNode ( final MavenInformation info, final DirectoryNode artifactBase )
    {
        return addMetaDataNode ( info, artifactBase, VersionMetadataNode.class, () -> new VersionMetadataNode ( info.getGroupId (), info.getArtifactId (), info.getVersion () ) );
    }

    protected static Document makeMetaData ( final String groupId, final String artifactId, final BiConsumer<Document, Element> cons )
    {
        final XmlHelper xml = new XmlHelper ();
        final Document doc = xml.create ();
        final Element root = doc.createElement ( "metadata" );
        doc.appendChild ( root );

        addElement ( root, "groupId", groupId );
        addElement ( root, "artifactId", artifactId );

        final Element v = addElement ( root, "versioning" );

        cons.accept ( doc, v );

        XmlHelper.addElement ( v, "lastUpdated", DATE_FORMAT.format ( new Date () ) );

        return doc;
    }

    public static Document createMetaData ( final String groupId, final String artifactId, final String version, final List<MavenInformation> infos, final Map<MavenInformation, Date> timestamps )
    {
        return makeMetaData ( groupId, artifactId, ( doc, v ) -> {

            // insert right before "versioning"
            final Element ver = doc.createElement ( "version" );
            ver.setTextContent ( version );
            v.getParentNode ().insertBefore ( ver, v );

            final Map<String, List<MavenInformation>> gi = new TreeMap<> ();
            final TreeSet<String> snapshots = new TreeSet<> ();

            // group by snapshot version
            for ( final MavenInformation info : infos )
            {
                if ( info.isSnapshot () && info.getSnapshotVersion () == null )
                {
                    continue;
                }

                snapshots.add ( info.getSnapshotVersion () );

                List<MavenInformation> list = gi.get ( info.getSnapshotVersion () );
                if ( list == null )
                {
                    list = new LinkedList<> ();
                    gi.put ( info.getSnapshotVersion (), list );
                }
                list.add ( info );
            }

            if ( !gi.isEmpty () )
            {
                final Element svs = addElement ( v, "snapshotVersions" );
                for ( final Map.Entry<String, List<MavenInformation>> entry : gi.entrySet () )
                {
                    for ( final MavenInformation info : entry.getValue () )
                    {
                        final Element sv = addElement ( svs, "snapshotVersion" );

                        addElement ( sv, "extension", info.getExtension () );
                        addElement ( sv, "value", info.getSnapshotVersion () );

                        if ( info.getClassifier () != null && !info.getClassifier ().isEmpty () )
                        {
                            addElement ( sv, "classifier", info.getClassifier () );
                        }

                        final Date ts = timestamps.get ( info );
                        if ( ts != null )
                        {
                            addElement ( sv, "updated", DATE_FORMAT.format ( ts ) ); // FIXME: replace with artifact date
                        }
                    }
                }

                {
                    final String latest = snapshots.last ();

                    final Matcher m = SNAPSHOT_PATTERN.matcher ( latest );
                    if ( m.matches () )
                    {
                        final Element s = addElementFirst ( v, "snapshot" );
                        addElement ( s, "timestamp", m.group ( "ts" ) );
                        addElement ( s, "buildNumber", m.group ( "bn" ) );
                    }
                }
            }
        } );
    }

    public static Document createMetaData ( final String groupId, final String artifactId, final List<MavenInformation> infos )
    {
        return makeMetaData ( groupId, artifactId, ( doc, v ) -> {

            final Set<String> releases = new HashSet<> ();
            final Set<String> all = new HashSet<> ();

            for ( final MavenInformation info : infos )
            {
                all.add ( info.getVersion () );
                if ( info.isSnapshot () )
                {
                    continue;
                }

                releases.add ( info.getVersion () );
            }

            final List<String> allSorted = sorted ( all );
            if ( !all.isEmpty () )
            {
                final Element vs = addElement ( v, "versions" );
                for ( final String release : allSorted )
                {
                    addElement ( vs, "version", release );
                }
            }

            if ( !releases.isEmpty () )
            {
                final List<String> releasesSorted = sorted ( releases );

                final String releaseStr = best ( releasesSorted );
                if ( releaseStr != null )
                {
                    final Element release = addElementFirst ( v, "release" );
                    release.setTextContent ( releaseStr );
                }
            }

            final String latestStr = best ( allSorted );
            if ( latestStr != null )
            {
                final Element latest = addElementFirst ( v, "latest" );
                latest.setTextContent ( latestStr );
            }

        } );
    }

    private static List<String> sorted ( final Set<String> versions )
    {
        final List<String> list = new ArrayList<> ( versions );
        Collections.sort ( list );
        return list;
    }

    private static String best ( final List<String> versions )
    {
        if ( versions.isEmpty () )
        {
            return null;
        }

        return versions.get ( versions.size () - 1 );
    }

    private static void addCheckSum ( final DirectoryNode versionNode, final String name, final ArtifactInformation art, final String string )
    {
        final String data = art.getMetaData ().get ( new MetaKey ( "hasher", string ) );
        if ( data == null )
        {
            return;
        }

        addNode ( versionNode, name + "." + string, new DataNode ( data, "text/plain" ) );
    }

    private DirectoryNode getGroup ( final String[] gn )
    {
        final LinkedList<String> dir = new LinkedList<> ( Arrays.asList ( gn ) );

        DirectoryNode current = this.root;
        while ( !dir.isEmpty () )
        {
            current = addDirNode ( current, dir.pollFirst () );
        }

        return current;
    }

    private static <T extends Node> T addNode ( final DirectoryNode current, final String seg, final T node )
    {
        if ( current.nodes.containsKey ( seg ) )
        {
            throw new IllegalStateException ( String.format ( "Invalid hierarchy. %s is already used.", seg ) );
        }

        current.nodes.put ( seg, node );

        return node;
    }

    private DirectoryNode addDirNode ( final DirectoryNode current, final String seg )
    {
        Node g = current.nodes.get ( seg );

        if ( g == null )
        {
            g = new DirectoryNode ();
            current.nodes.put ( seg, g );
            return (DirectoryNode)g;
        }

        if ( g instanceof DirectoryNode )
        {
            return (DirectoryNode)g;
        }

        throw new IllegalStateException ( String.format ( "Invalid group hierarchy. %s is of type %s.", seg, g.getClass () ) );
    }

    public Node findNode ( final Deque<String> segs )
    {
        Node current = this.root;
        while ( !segs.isEmpty () )
        {
            if ( ! ( current instanceof DirectoryNode ) )
            {
                return null;
            }

            final String n = segs.pollFirst ();
            final Node node = ( (DirectoryNode)current ).nodes.get ( n );
            if ( node == null )
            {
                return null;
            }

            current = node;
        }

        return current;
    }

    public String toJson ()
    {
        final Gson gson = makeGson ( false );
        return gson.toJson ( this );
    }

    @Override
    public String toString ()
    {
        final Gson gson = makeGson ( true );
        return gson.toJson ( this );
    }

    public static ChannelData fromJson ( final String json )
    {
        final Gson gson = makeGson ( false );
        return gson.fromJson ( json, ChannelData.class );
    }

    public static ChannelData fromReader ( final Reader reader )
    {
        final Gson gson = makeGson ( false );
        return gson.fromJson ( reader, ChannelData.class );
    }

    /**
     * Make an appropriate Gson parser to processing ChannelData instances
     *
     * @param pretty
     *            if the gson output should be "pretty printed"
     * @return the new gson instance
     */
    public static Gson makeGson ( final boolean pretty )
    {
        final GsonBuilder gb = new GsonBuilder ();

        if ( pretty )
        {
            gb.setPrettyPrinting ();
        }

        gb.registerTypeAdapter ( Node.class, new NodeAdapter () );
        gb.registerTypeAdapter ( byte[].class, new ByteArrayAdapter () );

        return gb.create ();
    }

}
