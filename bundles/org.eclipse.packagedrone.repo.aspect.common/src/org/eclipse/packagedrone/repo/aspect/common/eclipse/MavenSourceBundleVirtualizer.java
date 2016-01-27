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
package org.eclipse.packagedrone.repo.aspect.common.eclipse;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.eclipse.packagedrone.VersionInformation;
import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.aspect.common.osgi.OsgiExtractor;
import org.eclipse.packagedrone.repo.aspect.virtual.Virtualizer;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.eclipse.packagedrone.repo.utils.osgi.bundle.BundleInformation;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;

public class MavenSourceBundleVirtualizer implements Virtualizer
{
    private final static Logger logger = LoggerFactory.getLogger ( MavenSourceBundleVirtualizer.class );

    @Override
    public void virtualize ( final Context context )
    {
        final ArtifactInformation ai = context.getArtifactInformation ();

        if ( ai.getParentId () == null )
        {
            logger.debug ( "don't create - parent id is null" );
            return;
        }

        if ( !ai.getName ().endsWith ( "-sources.jar" ) )
        {
            logger.debug ( "don't create - name does not match" );
            return;
        }

        if ( isSourceBundle ( ai ) )
        {
            // we don't generate source bundles for source bundles
            return;
        }

        final ArtifactInformation parent = findParent ( context );
        if ( parent == null )
        {
            return;
        }

        final BundleInformation bi = findBundleInformation ( parent );
        if ( bi == null )
        {
            logger.debug ( "don't create - parent has no bundle information" );
            return;
        }

        try
        {
            createSourceBundle ( context, bi );
        }
        catch ( final Exception e )
        {
            throw new RuntimeException ( "Failed to create virtual source bundle", e );
        }
    }

    private boolean isSourceBundle ( final ArtifactInformation parent )
    {
        final String full = parent.getMetaData ().get ( OsgiExtractor.KEY_FULL_MANIFEST );
        if ( full == null )
        {
            return false;
        }

        try
        {
            final Manifest mf = new Manifest ( new ByteArrayInputStream ( full.getBytes ( StandardCharsets.UTF_8 ) ) );
            final String esb = mf.getMainAttributes ().getValue ( "Eclipse-SourceBundle" );
            return esb != null;
        }
        catch ( final IOException e )
        {
            return false;
        }
    }

    protected void createSourceBundle ( final Context context, final BundleInformation bi ) throws Exception
    {
        final Map<MetaKey, String> providedMetaData = new HashMap<> ();

        final String name = String.format ( "%s.source_%s.jar", bi.getId (), bi.getVersion () );

        context.createVirtualArtifact ( name, out -> createSourceBundle ( out, context, bi ), providedMetaData );
    }

    private void createSourceBundle ( final OutputStream out, final Context context, final BundleInformation bi ) throws IOException
    {
        try ( ZipInputStream zis = new ZipInputStream ( new BufferedInputStream ( Files.newInputStream ( context.getFile () ) ) );
              ZipOutputStream zos = new ZipOutputStream ( out ); )
        {
            ZipEntry entry;
            while ( ( entry = zis.getNextEntry () ) != null )
            {
                if ( entry.getName ().equals ( "META-INF/MANIFEST.MF" ) )
                {
                    continue;
                }

                zos.putNextEntry ( entry );
                ByteStreams.copy ( zis, zos );

            }

            entry = new ZipEntry ( "META-INF/MANIFEST.MF" );
            zos.putNextEntry ( entry );
            final Manifest mf = new Manifest ();
            fillManifest ( mf, bi );
            mf.write ( zos );

            if ( bi.getLocalization () != null && !bi.getLocalization ().isEmpty () )
            {
                for ( final Map.Entry<String, Properties> le : bi.getLocalization ().entrySet () )
                {
                    final String locale = le.getKey ();
                    final String suffix = locale != null && !locale.isEmpty () ? "_" + locale : "";
                    entry = new ZipEntry ( bi.getLocalization () + suffix );
                    zos.putNextEntry ( entry );
                    le.getValue ().store ( zos, null );
                }
            }
        }
    }

    private void fillManifest ( final Manifest mf, final BundleInformation bi )
    {
        final Attributes attr = mf.getMainAttributes ();

        attr.put ( Attributes.Name.MANIFEST_VERSION, "1.0" );

        attr.putValue ( Constants.BUNDLE_SYMBOLICNAME, bi.getId () + ".source" );
        attr.putValue ( Constants.BUNDLE_VERSION, "" + bi.getVersion () );
        attr.putValue ( Constants.BUNDLE_MANIFESTVERSION, "2" );
        attr.putValue ( Constants.BUNDLE_VENDOR, bi.getVendor () );
        attr.putValue ( Constants.BUNDLE_NAME, String.format ( "Source bundle for '%s'", bi.getId () ) );

        attr.putValue ( "Created-By", VersionInformation.VERSIONED_PRODUCT );

        attr.putValue ( "Eclipse-SourceBundle", makeSourceString ( bi ) );

        if ( bi.getBundleLocalization () != null )
        {
            attr.putValue ( Constants.BUNDLE_LOCALIZATION, bi.getBundleLocalization () );
        }
    }

    private String makeSourceString ( final BundleInformation bi )
    {
        final StringBuilder sb = new StringBuilder ();

        sb.append ( bi.getId () );
        sb.append ( ';' );
        sb.append ( "version=\"" ).append ( bi.getVersion () ).append ( "\"" );
        sb.append ( ';' );
        sb.append ( "roots:=" );
        sb.append ( "\".\"" );

        return sb.toString ();
    }

    private ArtifactInformation findParent ( final Context context )
    {
        return context.getOtherArtifactInformation ( context.getArtifactInformation ().getParentId () );
    }

    private BundleInformation findBundleInformation ( final ArtifactInformation parent )
    {
        final String biString = parent.getMetaData ().get ( BundleInformation.META_KEY );
        return BundleInformation.fromJson ( biString );
    }
}
