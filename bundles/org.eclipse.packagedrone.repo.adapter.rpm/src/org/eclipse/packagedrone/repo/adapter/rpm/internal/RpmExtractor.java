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
package org.eclipse.packagedrone.repo.adapter.rpm.internal;

import java.io.BufferedInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;

import org.eclipse.packagedrone.repo.adapter.rpm.Constants;
import org.eclipse.packagedrone.repo.adapter.rpm.RpmInformationsJson;
import org.eclipse.packagedrone.repo.aspect.extract.Extractor;
import org.eclipse.packagedrone.utils.rpm.RpmTag;
import org.eclipse.packagedrone.utils.rpm.info.RpmInformation;
import org.eclipse.packagedrone.utils.rpm.info.RpmInformations;
import org.eclipse.packagedrone.utils.rpm.parse.RpmInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpmExtractor implements Extractor
{

    private final static Logger logger = LoggerFactory.getLogger ( RpmExtractor.class );

    @Override
    public void extractMetaData ( final Context context, final Map<String, String> metadata )
    {
        final Path path = context.getPath ();

        try ( RpmInputStream in = new RpmInputStream ( new BufferedInputStream ( Files.newInputStream ( path, StandardOpenOption.READ ) ) ) )
        {
            final RpmInformation info = RpmInformations.makeInformation ( in );
            if ( info == null )
            {
                return;
            }

            metadata.put ( "artifactLabel", "RPM Package" );

            metadata.put ( "name", RpmInformations.asString ( in.getPayloadHeader ().getTag ( RpmTag.NAME ) ) );
            metadata.put ( "version", RpmInformations.asString ( in.getPayloadHeader ().getTag ( RpmTag.VERSION ) ) );
            metadata.put ( "os", RpmInformations.asString ( in.getPayloadHeader ().getTag ( RpmTag.OS ) ) );
            metadata.put ( "arch", RpmInformations.asString ( in.getPayloadHeader ().getTag ( RpmTag.ARCH ) ) );

            metadata.put ( Constants.KEY_INFO.getKey (), RpmInformationsJson.toJson ( info ) );
        }
        catch ( final Exception e )
        {
            logger.debug ( "Failed to parse RPM file", e );
            // ignore ... not an RPM file
        }
    }

}
