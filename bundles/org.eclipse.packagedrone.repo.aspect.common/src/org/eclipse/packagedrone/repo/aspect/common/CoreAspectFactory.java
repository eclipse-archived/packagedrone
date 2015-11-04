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
package org.eclipse.packagedrone.repo.aspect.common;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.packagedrone.repo.aspect.ChannelAspect;
import org.eclipse.packagedrone.repo.aspect.ChannelAspectFactory;
import org.eclipse.packagedrone.repo.aspect.extract.Extractor;

/**
 * Provide core artifact information as meta data
 */
public class CoreAspectFactory implements ChannelAspectFactory
{
    private static final String ID = "core";

    private static final String KEY_NAME = "name";

    private static final String KEY_EXT = "extension";

    private static final String KEY_BASENAME = "basename";

    private static final String KEY_ISO_TIMESTAMP = "iso-timestamp";

    private static final String KEY_TIMESTAMP = "timestamp";

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern ( "yyyy-MM-dd HH:mm:ss.SSS" );

    private static class ChannelAspectImpl implements ChannelAspect
    {

        @Override
        public Extractor getExtractor ()
        {
            return new Extractor () {

                @Override
                public void extractMetaData ( final Extractor.Context context, final Map<String, String> metadata ) throws Exception
                {
                    makeMetadata ( context, metadata );
                }
            };
        }

        @Override
        public String getId ()
        {
            return ID;
        }
    };

    @Override
    public ChannelAspect createAspect ()
    {
        return new ChannelAspectImpl ();
    }

    private static void makeMetadata ( final Extractor.Context context, final Map<String, String> metadata ) throws IOException
    {
        metadata.put ( KEY_NAME, context.getName () );
        metadata.put ( KEY_EXT, FilenameUtils.getExtension ( context.getName () ) );
        metadata.put ( KEY_BASENAME, FilenameUtils.getBaseName ( context.getName () ) );
        metadata.put ( KEY_ISO_TIMESTAMP, context.getCreationTimestamp ().toString () );
        metadata.put ( KEY_TIMESTAMP, TIMESTAMP_FORMATTER.format ( context.getCreationTimestamp ().atOffset ( ZoneOffset.UTC ) ) );
    }

}
