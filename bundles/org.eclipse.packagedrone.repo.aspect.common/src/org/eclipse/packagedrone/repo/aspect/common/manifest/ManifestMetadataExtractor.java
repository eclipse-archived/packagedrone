/*******************************************************************************
 * Copyright (c) 2017 Gemtec GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Peter Jeschke/Gemtec GmbH - initial implementation
 *******************************************************************************/

package org.eclipse.packagedrone.repo.aspect.common.manifest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.eclipse.packagedrone.repo.aspect.extract.Extractor;

/**
 * Extracts all manifest entries as metadata.
 *
 * @author Peter Jeschke
 */
final class ManifestMetadataExtractor implements Extractor
{

    @Override
    public void extractMetaData ( final Context context, final Map<String, String> metadata ) throws Exception
    {
        final Manifest manifest = extractManifest ( context );
        if ( manifest == null )
        {
            return;
        }
        storeManifestData ( manifest, metadata );
    }

    private Manifest extractManifest ( final Context context ) throws IOException
    {
        try ( ZipFile zipFile = new ZipFile ( context.getPath ().toFile () ) )
        {
            final ZipEntry m = zipFile.getEntry ( JarFile.MANIFEST_NAME );
            if ( m == null )
            {
                return null;
            }
            try ( InputStream is = zipFile.getInputStream ( m ) )
            {
                return new Manifest ( is );
            }
        }
        catch ( @SuppressWarnings ( "unused" ) final ZipException ex )
        {
            // probably not a zip file
            return null;
        }
    }

    private void storeManifestData ( final Manifest manifest, final Map<String, String> metadata )
    {
        for ( final Entry<Object, Object> entry : manifest.getMainAttributes ().entrySet () )
        {
            metadata.put ( ( (Attributes.Name)entry.getKey () ).toString (), (String)entry.getValue () );
        }
    }
}
