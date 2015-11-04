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
package org.eclipse.packagedrone.repo.aspect.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.eclipse.packagedrone.repo.aspect.ChannelAspect;
import org.eclipse.packagedrone.repo.aspect.ChannelAspectFactory;
import org.eclipse.packagedrone.repo.aspect.extract.Extractor;

public class MimeTypeAspectFactory implements ChannelAspectFactory
{
    public static final String ID = "mime";

    private static class ChannelAspectImpl implements ChannelAspect
    {
        @Override
        public Extractor getExtractor ()
        {
            return new Extractor () {

                @Override
                public void extractMetaData ( final Extractor.Context context, final Map<String, String> metadata ) throws Exception
                {
                    final String type = getMimeType ( context.getPath () );
                    if ( type != null )
                    {
                        metadata.put ( "type", type );
                    }
                }

                public String getMimeType ( final Path file ) throws IOException
                {
                    if ( file.toString ().endsWith ( ".pom" ) )
                    {
                        return "application/xml";
                    }
                    return Files.probeContentType ( file );
                }
            };
        }

        @Override
        public String getId ()
        {
            return ID;
        }

    }

    @Override
    public ChannelAspect createAspect ()
    {
        return new ChannelAspectImpl ();
    }

}
