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

import static org.eclipse.packagedrone.repo.XmlHelper.addElement;
import static org.eclipse.packagedrone.repo.XmlHelper.fixSize;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.MetaKeys;
import org.eclipse.packagedrone.repo.XmlHelper;
import org.eclipse.packagedrone.repo.aspect.common.osgi.OsgiAspectFactory;
import org.eclipse.packagedrone.repo.aspect.common.p2.InstallableUnit;
import org.eclipse.packagedrone.repo.aspect.common.p2.P2MetaDataInformation;
import org.eclipse.packagedrone.repo.aspect.virtual.Virtualizer;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.eclipse.packagedrone.repo.utils.osgi.bundle.BundleInformation;
import org.eclipse.packagedrone.repo.utils.osgi.feature.FeatureInformation;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class P2Virtualizer implements Virtualizer
{
    private final static Logger logger = LoggerFactory.getLogger ( P2Virtualizer.class );

    private final XmlHelper xml;

    public P2Virtualizer ()
    {
        this.xml = new XmlHelper ();
    }

    @Override
    public void virtualize ( final Context context )
    {
        try
        {
            processVirtualize ( context );
        }
        catch ( final Exception e )
        {
            throw new RuntimeException ( e );
        }
    }

    private void processVirtualize ( final Context context ) throws Exception
    {
        final ArtifactInformation art = context.getArtifactInformation ();

        final Map<MetaKey, String> metaData = context.getProvidedChannelMetaData ();
        final P2MetaDataInformation info = new P2MetaDataInformation ();
        MetaKeys.bind ( info, metaData );

        logger.debug ( "Process virtualize - artifactId: {} / {}", art.getId (), art.getName () );

        final BundleInformation bi = OsgiAspectFactory.fetchBundleInformation ( art.getMetaData () );
        if ( bi != null )
        {
            logger.debug ( "Process as bundle: {} ({})- {}", art.getName (), art.getId (), bi );
            createBundleP2MetaData ( context, info, art, bi );
            createBundleP2Artifacts ( context, info, art, bi );
            return;
        }

        final FeatureInformation fi = OsgiAspectFactory.fetchFeatureInformation ( art.getMetaData () );
        if ( fi != null )
        {
            logger.debug ( "Process as feature: {} ({}) - {}", art.getName (), art.getId (), fi );
            createFeatureP2MetaData ( context, info, art, fi );
            createFeatureP2Artifacts ( context, info, art, fi );
            return;
        }
    }

    private void createP2Artifacts ( final Context context, final String id, final Version version, final String type, final ArtifactInformation artifact, final String contentType ) throws Exception
    {
        final Document doc = this.xml.create ();

        final Element artifacts = doc.createElement ( "artifacts" );
        doc.appendChild ( artifacts );

        final Element a = addElement ( artifacts, "artifact" );
        a.setAttribute ( "classifier", type );
        a.setAttribute ( "id", id );
        a.setAttribute ( "version", version.toString () );

        final String md5 = artifact.getMetaData ().get ( new MetaKey ( "hasher", "md5" ) );

        final Element props = addElement ( a, "properties" );

        if ( md5 != null )
        {
            addProperty ( props, "download.md5", md5 );
        }

        addProperty ( props, "download.size", "" + artifact.getSize () );
        addProperty ( props, "artifact.size", "" + artifact.getSize () );
        addProperty ( props, "download.contentType", contentType );
        addProperty ( props, "drone.artifact.id", artifact.getId () );

        fixSize ( props );
        fixSize ( artifacts );

        createXmlVirtualArtifact ( context, artifact, doc, "-p2artifacts.xml" );
    }

    private void createFeatureP2Artifacts ( final Context context, final P2MetaDataInformation info, final ArtifactInformation artifact, final FeatureInformation fi ) throws Exception
    {
        createP2Artifacts ( context, fi.getId (), fi.getVersion (), "org.eclipse.update.feature", artifact, "application/zip" );
    }

    private void createBundleP2Artifacts ( final Context context, final P2MetaDataInformation info, final ArtifactInformation artifact, final BundleInformation bi ) throws Exception
    {
        createP2Artifacts ( context, bi.getId (), bi.getVersion (), "osgi.bundle", artifact, null );
    }

    private void addProperty ( final Element props, final String key, final String value )
    {
        if ( value == null )
        {
            return;
        }

        final Element p = addElement ( props, "property" );
        p.setAttribute ( "name", key );
        p.setAttribute ( "value", value );
    }

    private void createFeatureP2MetaData ( final Context context, final P2MetaDataInformation info, final ArtifactInformation art, final FeatureInformation fi ) throws Exception
    {
        final List<InstallableUnit> ius = InstallableUnit.fromFeature ( fi );
        createXmlVirtualArtifact ( context, art, InstallableUnit.toXml ( ius ), "-p2metadata.xml" );
    }

    private void createBundleP2MetaData ( final Context context, final P2MetaDataInformation info, final ArtifactInformation art, final BundleInformation bi ) throws Exception
    {
        createXmlVirtualArtifact ( context, art, InstallableUnit.fromBundle ( bi, info ).toXml (), "-p2metadata.xml" );
    }

    private void createXmlVirtualArtifact ( final Context context, final ArtifactInformation art, final Document doc, final String suffix ) throws Exception
    {
        final byte[] data = this.xml.toData ( doc );

        String name = art.getName ();
        name = name.replaceFirst ( "\\.jar$", suffix );

        context.createVirtualArtifact ( name, new ByteArrayInputStream ( data ), null );
    }

}
