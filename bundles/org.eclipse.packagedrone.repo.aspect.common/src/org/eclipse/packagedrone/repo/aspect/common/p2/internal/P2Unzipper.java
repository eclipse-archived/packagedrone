/*******************************************************************************
 * Copyright (c) 2015, 2016 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.aspect.common.p2.internal;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.aspect.common.p2.P2UnzipAspectFactory;
import org.eclipse.packagedrone.repo.aspect.virtual.Virtualizer;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;

public class P2Unzipper implements Virtualizer
{

    private final static Logger logger = LoggerFactory.getLogger ( P2Unzipper.class );

    private static final MetaKey KEY_REUSE_METADATA = new MetaKey ( "p2.unzip", "reuse-metadata" );

    @Override
    public void virtualize ( final Context context )
    {
        final ArtifactInformation ai = context.getArtifactInformation ();

        if ( !isZip ( ai ) )
        {
            return;
        }

        final boolean reuseMetadata = Boolean.parseBoolean ( context.getProvidedChannelMetaData ().get ( KEY_REUSE_METADATA ) );

        try ( ZipInputStream zis = new ZipInputStream ( new BufferedInputStream ( new FileInputStream ( context.getFile ().toFile () ) ) ) )
        {
            ZipEntry entry;
            while ( ( entry = zis.getNextEntry () ) != null )
            {
                if ( entry.isDirectory () )
                {
                    // skip directories
                    continue;
                }
                if ( entry.getName ().startsWith ( "features/" ) )
                {
                    processEntry ( context, entry, zis );
                }
                else if ( entry.getName ().startsWith ( "plugins/" ) )
                {
                    processEntry ( context, entry, zis );
                }
                else if ( reuseMetadata && entry.getName ().equals ( "artifacts.jar" ) )
                {
                    processZippedMetaData ( context, zis, "articacts.xml" );
                }
                else if ( reuseMetadata && entry.getName ().equals ( "content.jar" ) )
                {
                    processZippedMetaData ( context, zis, "content.xml" );
                }
            }
        }
        catch ( final IOException e )
        {
            logger.debug ( "Failed to unzip", e );
            // we need to throw, since we might have already created virtual artifacts
            throw new RuntimeException ( "Failed to unzip", e );
        }
    }

    private void processZippedMetaData ( final Context context, final ZipInputStream zis, final String filename ) throws IOException
    {
        final JarInputStream jin = new JarInputStream ( zis, false );
        ZipEntry entry;
        while ( ( entry = jin.getNextEntry () ) != null )
        {
            if ( entry.getName ().equals ( filename ) )
            {
                processEntry ( context, entry, jin );
            }
        }
    }

    private boolean isZip ( final ArtifactInformation ai )
    {
        if ( ai.getName ().toLowerCase ().endsWith ( ".zip" ) )
        {
            return true;
        }

        return false;
    }

    private void processEntry ( final Context context, final ZipEntry entry, final InputStream in )
    {
        final String segs[] = entry.getName ().split ( "\\/" );
        final String name = segs[segs.length - 1];

        final Map<MetaKey, String> metaData = new HashMap<> ( 1 );
        metaData.put ( P2UnzipAspectFactory.MK_FULL_NAME, entry.getName () );

        context.createVirtualArtifact ( name, out -> ByteStreams.copy ( in, out ), metaData );
    }

}
