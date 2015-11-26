/*******************************************************************************
 * Copyright (c) 2014, 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.aspect.common.p2.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.function.Supplier;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.eclipse.packagedrone.repo.aspect.common.p2.InstallableUnit;
import org.eclipse.packagedrone.repo.aspect.common.p2.P2MetaDataInformation;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.eclipse.packagedrone.repo.utils.osgi.bundle.BundleInformation;
import org.eclipse.packagedrone.repo.utils.osgi.feature.FeatureInformation;
import org.eclipse.packagedrone.utils.io.IOConsumer;
import org.osgi.framework.Version;

public class Creator
{
    public interface Context
    {
        public void create ( String name, IOConsumer<OutputStream> producer ) throws IOException;
    }

    private final Context context;

    private final Supplier<XMLOutputFactory> factoryProvider;

    public Creator ( final Context context, final Supplier<XMLOutputFactory> factoryProvider )
    {
        this.context = context;

        this.factoryProvider = factoryProvider;
    }

    private void createP2Artifacts ( final String id, final Version version, final String type, final ArtifactInformation artifact, final String contentType ) throws Exception
    {
        final String md5 = artifact.getMetaData ().get ( P2Virtualizer.KEY_MD5 );

        this.context.create ( makeName ( artifact, "-p2artifacts.xml" ), out -> {
            try
            {

                final XMLStreamWriter xsw = this.factoryProvider.get ().createXMLStreamWriter ( out );

                xsw.writeStartDocument ();

                xsw.writeStartElement ( "artifacts" );
                xsw.writeAttribute ( "size", "1" );

                xsw.writeStartElement ( "artifact" );
                xsw.writeAttribute ( "classifier", type );
                xsw.writeAttribute ( "id", id );
                xsw.writeAttribute ( "version", version.toString () );

                xsw.writeStartElement ( "properties" );

                int size = 3;
                if ( md5 != null )
                {
                    size++;
                }
                if ( contentType != null )
                {
                    size++;
                }

                xsw.writeAttribute ( "size", "" + size );

                writeProperty ( xsw, "download.size", "" + artifact.getSize () ); // #1
                writeProperty ( xsw, "artifact.size", "" + artifact.getSize () ); // #2
                writeProperty ( xsw, "download.contentType", contentType ); // #3
                writeProperty ( xsw, "drone.artifact.id", artifact.getId () ); // #4
                writeProperty ( xsw, "download.md5", md5 ); // #5

                xsw.writeEndElement (); // properties

                xsw.writeEndElement (); // artifact

                xsw.writeEndElement (); // artifacts

                xsw.writeEndDocument ();

                xsw.close ();
            }
            catch ( final XMLStreamException e )
            {
                throw new IOException ( e );
            }
        } );
    }

    private void writeProperty ( final XMLStreamWriter xsw, final String key, final String value ) throws XMLStreamException
    {
        if ( value == null )
        {
            return;
        }

        xsw.writeEmptyElement ( "property" );
        xsw.writeAttribute ( "name", key );
        xsw.writeAttribute ( "value", value );
    }

    public void createFeatureP2Artifacts ( final ArtifactInformation artifact, final FeatureInformation fi ) throws Exception
    {
        createP2Artifacts ( fi.getId (), fi.getVersion (), "org.eclipse.update.feature", artifact, "application/zip" );
    }

    public void createBundleP2Artifacts ( final ArtifactInformation artifact, final BundleInformation bi ) throws Exception
    {
        createP2Artifacts ( bi.getId (), bi.getVersion (), "osgi.bundle", artifact, null );
    }

    public void createFeatureP2MetaData ( final ArtifactInformation art, final FeatureInformation fi ) throws Exception
    {
        final List<InstallableUnit> ius = InstallableUnit.fromFeature ( fi );

        this.context.create ( makeName ( art, "-p2metadata.xml" ), out -> {
            try
            {
                final XMLStreamWriter xsw = this.factoryProvider.get ().createXMLStreamWriter ( out );
                InstallableUnit.writeXml ( xsw, ius );
                xsw.close ();
            }
            catch ( final XMLStreamException e )
            {
                throw new IOException ( e );
            }
        } );
    }

    public void createBundleP2MetaData ( final P2MetaDataInformation info, final ArtifactInformation art, final BundleInformation bi ) throws Exception
    {
        this.context.create ( makeName ( art, "-p2metadata.xml" ), out -> {
            try
            {
                final XMLStreamWriter xsw = this.factoryProvider.get ().createXMLStreamWriter ( out );
                InstallableUnit.fromBundle ( bi, info ).writeXml ( xsw );
                xsw.close ();
            }
            catch ( final XMLStreamException e )
            {
                throw new IOException ( e );
            }
        } );
    }

    private String makeName ( final ArtifactInformation art, final String suffix )
    {
        String name = art.getName ();
        name = name.replaceFirst ( "\\.jar$", suffix );
        return name;
    }
}
