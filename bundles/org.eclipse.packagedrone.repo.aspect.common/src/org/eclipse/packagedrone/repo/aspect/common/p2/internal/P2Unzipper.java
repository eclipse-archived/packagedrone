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
package org.eclipse.packagedrone.repo.aspect.common.p2.internal;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.aspect.common.p2.P2UnzipAspectFactory;
import org.eclipse.packagedrone.repo.aspect.virtual.Virtualizer;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class P2Unzipper implements Virtualizer
{

    private final static Logger logger = LoggerFactory.getLogger ( P2Unzipper.class );

    @Override
    public void virtualize ( final Context context )
    {
        final ArtifactInformation ai = context.getArtifactInformation ();

        if ( !isZip ( ai ) )
        {
            return;
        }

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
            }
        }
        catch ( final IOException e )
        {
            logger.debug ( "Failed to unzip", e );
            // we don't do anything
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

    private void processEntry ( final Context context, final ZipEntry entry, final ZipInputStream zis )
    {
        final String segs[] = entry.getName ().split ( "\\/" );
        final String name = segs[segs.length - 1];

        final Map<MetaKey, String> metaData = new HashMap<> ( 1 );
        metaData.put ( P2UnzipAspectFactory.MK_FULL_NAME, entry.getName () );

        context.createVirtualArtifact ( name, zis, metaData );
    }

}
