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

import org.apache.commons.compress.compressors.zstandard.ZstdCompressorInputStream;
import org.apache.commons.compress.compressors.zstandard.ZstdCompressorOutputStream;
import org.apache.commons.compress.compressors.zstandard.ZstdUtils;
import org.eclipse.packagedrone.utils.rpm.deps.Dependency;
import org.eclipse.packagedrone.utils.rpm.deps.RpmDependencyFlags;

public class ZstdPayloadCoding implements PayloadCoding
{
    protected ZstdPayloadCoding ()
    {

    }

    @Override
    public String getCoding ()
    {
        return "zstd";
    }

    @Override
    public Optional<Dependency> getDependency ()
    {
        return Optional.of ( new Dependency ( "PayloadIsZstd", "5.4.18-1", RpmDependencyFlags.LESS, RpmDependencyFlags.EQUAL, RpmDependencyFlags.RPMLIB ) );
    }

    @Override
    public InputStream createInputStream ( final InputStream in ) throws IOException
    {
        if ( !ZstdUtils.isZstdCompressionAvailable () )
        {
            throw new IOException( "Zstandard compression is not available" );
        }

        return new ZstdCompressorInputStream ( in );
    }

    @Override
    public OutputStream createOutputStream ( final OutputStream out, final Optional<String> optionalFlags ) throws IOException
    {
        if ( !ZstdUtils.isZstdCompressionAvailable () )
        {
            throw new IOException( "Zstandard compression is not available" );
        }

        final String flags;

        final int level;

        if ( optionalFlags.isPresent () && ( flags = optionalFlags.get () ).length() > 0 )
        {
            level = Integer.parseInt ( flags.substring ( 0, 1 ) );
        }
        else
        {
            level = 3;
        }

        return new ZstdCompressorOutputStream ( out, level );
    }
}
