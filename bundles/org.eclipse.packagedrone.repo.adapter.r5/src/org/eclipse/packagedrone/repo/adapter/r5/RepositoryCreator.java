/*******************************************************************************
 * Copyright (c) 2015 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *     Julius Fingerle - fix a few repository generations issues
 *******************************************************************************/
package org.eclipse.packagedrone.repo.adapter.r5;

import static org.eclipse.packagedrone.utils.Filters.and;
import static org.eclipse.packagedrone.utils.Filters.pair;
import static org.eclipse.packagedrone.utils.Filters.versionRange;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.eclipse.packagedrone.VersionInformation;
import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.aspect.common.osgi.OsgiAspectFactory;
import org.eclipse.packagedrone.repo.aspect.common.spool.OutputSpooler;
import org.eclipse.packagedrone.repo.aspect.common.spool.SpoolOutTarget;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.eclipse.packagedrone.repo.utils.osgi.bundle.BundleInformation;
import org.eclipse.packagedrone.repo.utils.osgi.bundle.BundleInformation.BundleRequirement;
import org.eclipse.packagedrone.repo.utils.osgi.bundle.BundleInformation.PackageExport;
import org.eclipse.packagedrone.repo.utils.osgi.bundle.BundleInformation.PackageImport;
import org.eclipse.packagedrone.utils.Filters;
import org.eclipse.packagedrone.utils.Filters.Node;
import org.eclipse.packagedrone.utils.io.IOConsumer;
import org.osgi.framework.Version;

public class RepositoryCreator
{
    private static final MetaKey KEY_SHA_256 = new MetaKey ( "hasher", "sha256" );

    private static final DateFormat OBR_DATE_FORMAT = new SimpleDateFormat ( "YYYYMMDDHHmmss.SSS" );

    private static final String FRAMEWORK_PACKAGE = "org.osgi.framework";

    private final OutputSpooler indexStreamBuilder;

    private final String name;

    private final Function<ArtifactInformation, String> urlProvider;

    private final Supplier<XMLOutputFactory> outputFactory;

    private final OutputSpooler obrStreamBuilder;

    public static interface Context
    {
        public void addArtifact ( ArtifactInformation artifact ) throws IOException;
    }

    private static class ContextImpl implements Context
    {
        private final XMLStreamWriter indexWriter;

        private final XMLStreamWriter obrWriter;

        private final Function<ArtifactInformation, String> urlProvider;

        public ContextImpl ( final XMLStreamWriter indexWriter, final XMLStreamWriter obrWriter, final Function<ArtifactInformation, String> urlProvider )
        {
            this.indexWriter = indexWriter;
            this.obrWriter = obrWriter;
            this.urlProvider = urlProvider;
        }

        @Override
        public void addArtifact ( final ArtifactInformation art ) throws IOException
        {
            final Map<MetaKey, String> md = art.getMetaData ();

            final BundleInformation bi = OsgiAspectFactory.fetchBundleInformation ( md );

            if ( bi == null )
            {
                return;
            }

            if ( bi.getId () == null || bi.getVersion () == null )
            {
                return;
            }

            try
            {
                addIndexEntry ( art, bi );
                addObrEntry ( art, bi );
            }
            catch ( final XMLStreamException e )
            {
                throw new IOException ( e );
            }
        }

        private void addIndexEntry ( final ArtifactInformation art, final BundleInformation bi ) throws XMLStreamException
        {
            this.indexWriter.writeStartElement ( "resource" );
            this.indexWriter.writeCharacters ( "\n" );

            addIndexIdentity ( this.indexWriter, bi );
            addIndexContent ( this.indexWriter, art, this.urlProvider.apply ( art ) );
            addIndexDependencies ( this.indexWriter, bi );

            this.indexWriter.writeEndElement ();
            this.indexWriter.writeCharacters ( "\n\n" );
        }

        private void addObrEntry ( final ArtifactInformation art, final BundleInformation bi ) throws XMLStreamException
        {
            this.obrWriter.writeStartElement ( "resource" );

            this.obrWriter.writeAttribute ( "id", art.getId () );
            this.obrWriter.writeAttribute ( "symbolicname", bi.getId () );
            this.obrWriter.writeAttribute ( "version", "" + bi.getVersion () );

            if ( bi.getName () != null )
            {
                this.obrWriter.writeAttribute ( "presentationname", bi.getName () );
            }
            this.obrWriter.writeAttribute ( "uri", this.urlProvider.apply ( art ) );
            this.obrWriter.writeCharacters ( "\n" ); // resource

            addObrMainTag ( this.obrWriter, "size", "" + art.getSize () );
            if ( bi.getDocUrl () != null && !bi.getDocUrl ().isEmpty () )
            {
                addObrMainTag ( this.obrWriter, "documentation", "" + bi.getDocUrl () );
            }
            if ( bi.getDescription () != null && !bi.getDescription ().isEmpty () )
            {
                addObrMainTag ( this.obrWriter, "description", "" + bi.getDescription () );
            }

            addObrDependencies ( this.obrWriter, bi );

            this.obrWriter.writeEndElement ();
            this.obrWriter.writeCharacters ( "\n\n" );
        }

        private static void addObrMainTag ( final XMLStreamWriter obrWriter, final String name, final String value ) throws XMLStreamException
        {
            if ( value == null )
            {
                return;
            }

            obrWriter.writeCharacters ( "\t" );
            obrWriter.writeStartElement ( name );
            obrWriter.writeCharacters ( value );
            obrWriter.writeEndElement ();
            obrWriter.writeCharacters ( "\n" );
        }

    }

    private static void addIndexIdentity ( final XMLStreamWriter writer, final BundleInformation bi ) throws XMLStreamException
    {
        final Map<String, Object> caps = new HashMap<> ();

        caps.put ( "osgi.identity", bi.getId () );
        caps.put ( "version", bi.getVersion () );
        caps.put ( "type", "osgi.bundle" );

        addIndexCapability ( writer, "osgi.identity", caps );
    }

    public static void addIndexDependencies ( final XMLStreamWriter writer, final BundleInformation bi ) throws XMLStreamException
    {
        {
            final List<Node> nodes = new LinkedList<> ();

            for ( final String ee : bi.getRequiredExecutionEnvironments () )
            {
                nodes.add ( pair ( "osgi.ee", ee ) );
            }
            if ( !nodes.isEmpty () )
            {
                final Map<String, String> reqs = new HashMap<> ( 1 );
                reqs.put ( "filter", Filters.or ( nodes ) );
                addIndexRequirement ( writer, "osgi.ee", reqs );
            }
        }

        {
            final Map<String, Object> caps = new HashMap<> ();

            caps.put ( "osgi.wiring.bundle", bi.getId () );
            caps.put ( "bundle-version", bi.getVersion () );

            addIndexCapability ( writer, "osgi.wiring.bundle", caps );
        }

        {
            final Map<String, Object> caps = new HashMap<> ();

            caps.put ( "osgi.wiring.host", bi.getId () );
            caps.put ( "bundle-version", bi.getVersion () );

            addIndexCapability ( writer, "osgi.wiring.host", caps );
        }

        for ( final BundleRequirement br : bi.getBundleRequirements () )
        {
            final Map<String, String> reqs = new HashMap<> ();

            final String filter = and ( //
            pair ( "osgi.wiring.bundle", br.getId () ), //
            versionRange ( "bundle-version", br.getVersionRange () ) //
            );

            reqs.put ( "filter", filter );

            if ( br.isOptional () )
            {
                reqs.put ( "resolution", "optional" );
            }

            addIndexRequirement ( writer, "osgi.wiring.bundle", reqs );
        }

        for ( final PackageExport pe : bi.getPackageExports () )
        {
            final Map<String, Object> caps = new HashMap<> ();

            caps.put ( "osgi.wiring.package", pe.getName () );

            if ( pe.getVersion () != null )
            {
                caps.put ( "version", pe.getVersion () );
            }

            addIndexCapability ( writer, "osgi.wiring.package", caps );

            // Add a 'osgi.contract' capability if this bundle is a framework package
            if ( FRAMEWORK_PACKAGE.equals ( pe.getName () ) )
            {
                final Version specVersion = mapFrameworkPackageVersion ( pe.getVersion () );
                if ( specVersion != null )
                {
                    final Map<String, Object> frameworkCaps = new HashMap<> ();
                    frameworkCaps.put ( "osgi.contract", "OSGiFramework" );
                    frameworkCaps.put ( "version", specVersion );
                    addIndexCapability ( writer, "osgi.contract", frameworkCaps );
                }
            }
        }

        for ( final PackageImport pi : bi.getPackageImports () )
        {
            final Map<String, String> reqs = new HashMap<> ();

            final String filter = and ( //
            pair ( "osgi.wiring.package", pi.getName () ), //
            versionRange ( "version", pi.getVersionRange () ) //
            );

            reqs.put ( "filter", filter );

            if ( pi.isOptional () )
            {
                reqs.put ( "resolution", "optional" );
            }

            addIndexRequirement ( writer, "osgi.wiring.package", reqs );
        }
    }

    public static void addObrDependencies ( final XMLStreamWriter writer, final BundleInformation bi ) throws XMLStreamException
    {
        {
            final List<Node> nodes = new LinkedList<> ();

            for ( final String ee : bi.getRequiredExecutionEnvironments () )
            {
                nodes.add ( pair ( "ee", ee ) );
            }
            if ( !nodes.isEmpty () )
            {
                final String filter = Filters.or ( nodes );
                addObrRequirement ( writer, "ee", filter, false, false, false, String.format ( "Execution Environment %s", filter ) );
            }
        }

        {
            final Map<String, Object> caps = new HashMap<> ();

            caps.put ( "symbolicname", bi.getId () );
            if ( bi.getName () != null )
            {
                caps.put ( "presentationname", bi.getName () );
            }
            caps.put ( "version", bi.getVersion () );
            caps.put ( "manifestversion", "2" ); // FIXME: provide real manifest version

            addObrCapability ( writer, "bundle", caps );
        }

        for ( final BundleRequirement br : bi.getBundleRequirements () )
        {
            final String filter = and ( //
            pair ( "symbolicname", br.getId () ), //
            versionRange ( "version", br.getVersionRange () ) //
            );

            addObrRequirement ( writer, "bundle", filter, false, false, br.isOptional (), br.toString () );
        }

        for ( final PackageExport pe : bi.getPackageExports () )
        {
            final Map<String, Object> caps = new HashMap<> ();

            caps.put ( "package", pe.getName () );

            if ( pe.getVersion () != null )
            {
                caps.put ( "version", pe.getVersion () );
            }
            else
            {
                caps.put ( "version", "0.0.0" );
            }

            if ( pe.getUses () != null )
            {
                caps.put ( ":uses", pe.getUses () );
            }

            addObrCapability ( writer, "package", caps );
        }

        for ( final PackageImport pi : bi.getPackageImports () )
        {
            final String filter = and ( //
            pair ( "package", pi.getName () ), //
            versionRange ( "version", pi.getVersionRange () ) //
            );

            addObrRequirement ( writer, "package", filter, false, false, pi.isOptional (), pi.toString () );
        }
    }

    private static void addIndexRequirement ( final XMLStreamWriter writer, final String id, final Map<String, String> caps ) throws XMLStreamException
    {
        writer.writeCharacters ( "\t" );
        writer.writeStartElement ( "requirement" );
        writer.writeAttribute ( "namespace", id );
        writer.writeCharacters ( "\n" );

        for ( final Map.Entry<String, String> entry : caps.entrySet () )
        {
            writer.writeCharacters ( "\t\t" );
            writer.writeEmptyElement ( "directive" );
            writer.writeAttribute ( "name", entry.getKey () );
            writer.writeAttribute ( "value", entry.getValue () );

            writer.writeCharacters ( "\n" );
        }

        writer.writeCharacters ( "\t" );
        writer.writeEndElement ();
        writer.writeCharacters ( "\n" );
    }

    private static void addObrRequirement ( final XMLStreamWriter writer, final String id, final String filter, final boolean extend, final boolean multiple, final boolean optional, final String text ) throws XMLStreamException
    {
        writer.writeCharacters ( "\t" );
        writer.writeStartElement ( "require" );

        writer.writeAttribute ( "name", id );
        writer.writeAttribute ( "filter", filter );
        writer.writeAttribute ( "extend", "" + extend );
        writer.writeAttribute ( "multiple", "" + multiple );
        writer.writeAttribute ( "optional", "" + optional );

        writer.writeCharacters ( text );

        writer.writeEndElement ();
        writer.writeCharacters ( "\n" );
    }

    private static void addIndexContent ( final XMLStreamWriter writer, final ArtifactInformation a, final String url ) throws XMLStreamException
    {
        final String sha256 = a.getMetaData ().get ( KEY_SHA_256 );

        if ( sha256 == null )
        {
            return;
        }

        final Map<String, Object> caps = new HashMap<> ( 4 );

        caps.put ( "osgi.content", sha256 );
        caps.put ( "size", a.getSize () );
        caps.put ( "mime", "application/vnd.osgi.bundle" );
        caps.put ( "url", url );

        addIndexCapability ( writer, "osgi.content", caps );
    }

    private static void addIndexCapability ( final XMLStreamWriter writer, final String id, final Map<String, Object> caps ) throws XMLStreamException
    {
        writer.writeCharacters ( "\t" );
        writer.writeStartElement ( "capability" );
        writer.writeAttribute ( "namespace", id );
        writer.writeCharacters ( "\n" );

        for ( final Map.Entry<String, Object> entry : caps.entrySet () )
        {
            writer.writeCharacters ( "\t\t" );
            writer.writeEmptyElement ( "attribute" );
            writer.writeAttribute ( "name", entry.getKey () );

            final Object v = entry.getValue ();

            if ( ! ( v instanceof String ) )
            {
                writer.writeAttribute ( "type", v.getClass ().getSimpleName () );
            }

            writer.writeAttribute ( "value", "" + v );

            writer.writeCharacters ( "\n" );
        }

        writer.writeCharacters ( "\t" );
        writer.writeEndElement ();
        writer.writeCharacters ( "\n" );
    }

    private static void addObrCapability ( final XMLStreamWriter writer, final String id, final Map<String, Object> caps ) throws XMLStreamException
    {
        writer.writeCharacters ( "\t" );
        writer.writeStartElement ( "capability" );
        writer.writeAttribute ( "name", id );
        writer.writeCharacters ( "\n" );

        for ( final Map.Entry<String, Object> entry : caps.entrySet () )
        {
            writer.writeCharacters ( "\t\t" );
            writer.writeEmptyElement ( "p" );
            writer.writeAttribute ( "n", entry.getKey () );

            final Object v = entry.getValue ();

            if ( v instanceof Version )
            {
                writer.writeAttribute ( "t", v.getClass ().getSimpleName ().toLowerCase () );
            }

            writer.writeAttribute ( "v", "" + v );

            writer.writeCharacters ( "\n" );
        }

        writer.writeCharacters ( "\t" );
        writer.writeEndElement ();
        writer.writeCharacters ( "\n" );
    }

    public RepositoryCreator ( final String name, final SpoolOutTarget target, final Function<ArtifactInformation, String> urlProvider, final Supplier<XMLOutputFactory> outputFactory )
    {
        this.name = name;
        this.urlProvider = urlProvider;
        this.outputFactory = outputFactory;

        this.indexStreamBuilder = new OutputSpooler ( target );
        this.indexStreamBuilder.addOutput ( "index.xml", "application/xml" );

        this.obrStreamBuilder = new OutputSpooler ( target );
        this.obrStreamBuilder.addOutput ( "obr.xml", "application/xml" );
    }

    public RepositoryCreator ( final String name, final SpoolOutTarget target, final Function<ArtifactInformation, String> urlProvider )
    {
        this ( name, target, urlProvider, XMLOutputFactory::newFactory );
    }

    public void process ( final IOConsumer<Context> consumer ) throws IOException
    {
        final XMLOutputFactory xml = this.outputFactory.get ();

        this.indexStreamBuilder.open ( indexStream -> {
            this.obrStreamBuilder.open ( obrStream -> {

                try
                {
                    processStreams ( consumer, xml, indexStream, obrStream );
                }
                catch ( final Exception e )
                {
                    throw new IOException ( e );
                }

            } );
        } );
    }

    private void processStreams ( final IOConsumer<Context> consumer, final XMLOutputFactory xml, final OutputStream indexStream, final OutputStream obrStream ) throws XMLStreamException, IOException
    {
        final XMLStreamWriter indexWriter = xml.createXMLStreamWriter ( indexStream );
        final XMLStreamWriter obrWriter = xml.createXMLStreamWriter ( obrStream );

        try
        {
            startIndex ( indexWriter );
            startObr ( obrWriter );

            final ContextImpl ctx = new ContextImpl ( indexWriter, obrWriter, this.urlProvider );
            consumer.accept ( ctx );

            endObr ( obrWriter );
            endIndex ( indexWriter );
        }
        finally
        {
            indexWriter.close ();
            obrWriter.close ();
        }
    }

    private void startIndex ( final XMLStreamWriter xsw ) throws XMLStreamException
    {
        xsw.writeStartDocument ();
        xsw.writeCharacters ( "\n\n" );

        xsw.writeComment ( String.format ( "Created by Package Drone %s - %tc", VersionInformation.VERSION, new Date () ) );

        xsw.writeStartElement ( "repository" );
        xsw.writeDefaultNamespace ( "http://www.osgi.org/xmlns/repository/v1.0.0" );
        xsw.writeAttribute ( "increment", "" + System.currentTimeMillis () );
        xsw.writeAttribute ( "name", this.name );

        xsw.writeCharacters ( "\n\n" );
    }

    private void endIndex ( final XMLStreamWriter xsw ) throws XMLStreamException
    {
        xsw.writeEndElement (); // repository
        xsw.writeEndDocument ();
    }

    private void startObr ( final XMLStreamWriter xsw ) throws XMLStreamException
    {
        xsw.writeStartDocument ();
        xsw.writeCharacters ( "\n\n" );

        xsw.writeComment ( String.format ( "Created by Package Drone %s - %tc", VersionInformation.VERSION, new Date () ) );

        xsw.writeStartElement ( "repository" );
        xsw.writeAttribute ( "lastmodified", OBR_DATE_FORMAT.format ( new Date () ) );
        xsw.writeAttribute ( "name", this.name );

        xsw.writeCharacters ( "\n\n" );
    }

    private void endObr ( final XMLStreamWriter xsw ) throws XMLStreamException
    {
        xsw.writeEndElement (); // repository
        xsw.writeEndDocument ();
    }

    private static Version mapFrameworkPackageVersion ( final Version pv )
    {
        if ( pv.getMajor () != 1 )
        {
            return null;
        }

        Version version;
        switch ( pv.getMinor () )
        {
            case 7:
                version = new Version ( 5, 0, 0 );
                break;
            case 6:
                version = new Version ( 4, 3, 0 );
                break;
            case 5:
                version = new Version ( 4, 2, 0 );
                break;
            case 4:
                version = new Version ( 4, 1, 0 );
                break;
            case 3:
                version = new Version ( 4, 0, 0 );
                break;
            case 2:
                version = new Version ( 3, 0, 0 );
                break;
            case 1:
                version = new Version ( 2, 0, 0 );
                break;
            case 0:
                version = new Version ( 1, 0, 0 );
                break;
            default:
                version = null;
                break;
        }

        return version;
    }

}
