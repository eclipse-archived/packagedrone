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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.aspect.ChannelAspect;
import org.eclipse.packagedrone.repo.aspect.ChannelAspectFactory;
import org.eclipse.packagedrone.repo.aspect.extract.Extractor;
import org.eclipse.packagedrone.repo.aspect.virtual.Virtualizer;
import org.eclipse.packagedrone.repo.utils.HashHelper;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public class HashAspectFactory implements ChannelAspectFactory
{
    private static final Map<String, HashFunction> functions = new HashMap<> ();

    private static final String ID = "hasher";

    private static final MetaKey KEY_FILE_TYPE = new MetaKey ( ID, "checksum-file" );

    static
    {
        HashAspectFactory.functions.put ( "md5", Hashing.md5 () );
        HashAspectFactory.functions.put ( "sha1", Hashing.sha1 () );
        HashAspectFactory.functions.put ( "sha256", Hashing.sha256 () );
        HashAspectFactory.functions.put ( "sha512", Hashing.sha512 () );
    }

    private static class ChannelAspectImpl implements ChannelAspect
    {

        @Override
        public Extractor getExtractor ()
        {
            return new Extractor () {

                @Override
                public void extractMetaData ( final Extractor.Context context, final Map<String, String> metadata ) throws Exception
                {
                    makeChecksumMetadata ( context.getPath (), metadata );
                }
            };
        }

        @Override
        public Virtualizer getArtifactVirtualizer ()
        {
            return null; // return new VirtualizerImpl ();
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

    private static void makeChecksumMetadata ( final Path file, final Map<String, String> metadata ) throws IOException
    {
        final Map<String, HashCode> result = HashHelper.createChecksums ( file, functions );
        for ( final Map.Entry<String, HashCode> entry : result.entrySet () )
        {
            metadata.put ( entry.getKey (), entry.getValue ().toString () );
        }
    }

    /**
     * Create virtual checksum files
     * <p>
     * <em>Note:</em> This is currently not used and therefore not referenced
     * in the ChannelAspectImpl class above!
     * </p>
     */
    public static class VirtualizerImpl implements Virtualizer
    {
        @Override
        public void virtualize ( final Context context )
        {
            final Map<MetaKey, String> md = context.getArtifactInformation ().getMetaData ();

            if ( md.containsKey ( KEY_FILE_TYPE ) )
            {
                return;
            }

            final String md5 = md.get ( new MetaKey ( ID, "md5" ) );
            if ( md5 != null )
            {
                final Map<MetaKey, String> metadata = new HashMap<> ();
                metadata.put ( KEY_FILE_TYPE, "md5" );
                context.createVirtualArtifact ( context.getArtifactInformation ().getName () + ".md5", new ByteArrayInputStream ( md5.getBytes ( StandardCharsets.UTF_8 ) ), metadata );
            }
        }
    }
}
