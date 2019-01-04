/*******************************************************************************
 * Copyright (c) 2018 Red Hat Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.utils.rpm.build;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.zip.Deflater;

import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipParameters;
import org.eclipse.packagedrone.utils.rpm.deps.Dependency;

public class GzipPayloadCoding implements PayloadCoding
{
    protected GzipPayloadCoding ()
    {
    }

    @Override
    public String getCoding ()
    {
        return "gzip";
    }

    @Override
    public void fillRequirements ( final Consumer<Dependency> requirementsConsumer )
    {
    }

    @Override
    public InputStream createInputStream ( final InputStream in ) throws IOException
    {
        return new GzipCompressorInputStream ( in );
    }

    @Override
    public OutputStream createOutputStream ( final OutputStream out, final Optional<String> optionalFlags ) throws IOException
    {
        final String flags;
        final int compressionLevel;

        if ( optionalFlags.isPresent () && ( flags = optionalFlags.get () ).length () > 0 )
        {
            compressionLevel = Integer.parseInt ( flags.substring ( 0, 1 ) );
        }
        else
        {
            compressionLevel = Deflater.BEST_COMPRESSION;
        }

        final GzipParameters parameters = new GzipParameters ();

        parameters.setCompressionLevel ( compressionLevel );

        return new GzipCompressorOutputStream ( out, parameters );
    }
}
