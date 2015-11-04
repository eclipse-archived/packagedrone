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
package org.eclipse.packagedrone.repo.aspect.common.osgi;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.eclipse.packagedrone.repo.aspect.extract.Extractor;
import org.eclipse.packagedrone.repo.utils.osgi.bundle.BundleInformation;
import org.eclipse.packagedrone.repo.utils.osgi.bundle.BundleInformationParser;
import org.eclipse.packagedrone.repo.utils.osgi.feature.FeatureInformation;
import org.eclipse.packagedrone.repo.utils.osgi.feature.FeatureInformationParser;
import org.osgi.framework.Constants;

import com.google.common.io.ByteStreams;

public class OsgiExtractor implements Extractor
{
    public static final String KEY_CLASSIFIER = "classifier";

    public static final String KEY_MANIFEST = "manifest";

    public static final String KEY_FULL_MANIFEST = "fullManifest";

    public static final String KEY_VERSION = "version";

    public static final String KEY_NAME = "name";

    public static final String KEY_BUNDLE_INFORMATION = "bundle-information";

    public static final String KEY_FEATURE_INFORMATION = "feature-information";

    public static final String NAMESPACE = OsgiAspectFactory.ID;

    @Override
    public void extractMetaData ( final Extractor.Context context, final Map<String, String> metadata ) throws Exception
    {
        extractBundleInformation ( context, metadata );
        extractFeatureInformation ( context, metadata );
    }

    private void extractFeatureInformation ( final Extractor.Context context, final Map<String, String> metadata ) throws Exception
    {
        final FeatureInformation fi;
        try ( ZipFile zipFile = new ZipFile ( context.getPath ().toFile () ) )
        {
            fi = new FeatureInformationParser ( zipFile ).parse ();
            if ( fi == null )
            {
                return;
            }
        }
        catch ( final ZipException e )
        {
            return;
        }

        metadata.put ( KEY_NAME, fi.getId () );
        metadata.put ( KEY_VERSION, "" + fi.getVersion () );
        metadata.put ( KEY_CLASSIFIER, "eclipse.feature" );
        metadata.put ( org.eclipse.packagedrone.repo.aspect.Constants.KEY_ARTIFACT_LABEL, "Eclipse Feature" );

        // store feature information

        metadata.put ( KEY_FEATURE_INFORMATION, fi.toJson () );
    }

    private void extractBundleInformation ( final Extractor.Context context, final Map<String, String> metadata ) throws Exception
    {
        final BundleInformation bi;
        try ( ZipFile zipFile = new ZipFile ( context.getPath ().toFile () ) )
        {

            final ZipEntry m = zipFile.getEntry ( JarFile.MANIFEST_NAME );
            if ( m == null )
            {
                return;
            }

            // store full manifest

            try ( InputStream is = zipFile.getInputStream ( m ) )
            {
                final byte[] data = ByteStreams.toByteArray ( is );
                metadata.put ( KEY_FULL_MANIFEST, StandardCharsets.UTF_8.decode ( ByteBuffer.wrap ( data ) ).toString () );
            }

            // parse bundle information

            Manifest manifest;
            try ( InputStream is = zipFile.getInputStream ( m ) )
            {
                manifest = new Manifest ( is );
            }

            bi = new BundleInformationParser ( zipFile, manifest ).parse ();
            if ( bi == null )
            {
                return;
            }
        }
        catch ( final ZipException e )
        {
            return;
        }

        // perform in-place validation

        if ( !validateBundle ( context, bi ) )
        {
            // invalid ... so ignore
            return;
        }

        // store main attributes

        metadata.put ( KEY_NAME, bi.getId () );
        metadata.put ( KEY_VERSION, bi.getVersion () != null ? bi.getVersion ().toString () : null );
        metadata.put ( KEY_CLASSIFIER, "bundle" );
        metadata.put ( org.eclipse.packagedrone.repo.aspect.Constants.KEY_ARTIFACT_LABEL, "OSGi Bundle" );

        // serialize manifest

        final ByteArrayOutputStream bos = new ByteArrayOutputStream ();
        final Manifest mf = new Manifest ();
        mf.getMainAttributes ().putValue ( Constants.BUNDLE_SYMBOLICNAME, bi.getId () );
        mf.getMainAttributes ().putValue ( Constants.BUNDLE_VERSION, bi.getVersion () != null ? bi.getVersion ().toString () : null );
        mf.getMainAttributes ().put ( Attributes.Name.MANIFEST_VERSION, "1.0" );
        mf.write ( bos );
        bos.close ();
        metadata.put ( KEY_MANIFEST, bos.toString ( "UTF-8" ) );

        // store bundle information
        metadata.put ( KEY_BUNDLE_INFORMATION, bi.toJson () );

    }

    private boolean validateBundle ( final Context context, final BundleInformation bi )
    {
        boolean valid = true;

        final String bsn = bi.getId (); // this is ensured not be null
        if ( bsn.contains ( "," ) )
        {
            context.validationError ( "Bundle Symbolic Name contains a comma: " + bsn );
            valid = false;
        }

        return valid;
    }
}
