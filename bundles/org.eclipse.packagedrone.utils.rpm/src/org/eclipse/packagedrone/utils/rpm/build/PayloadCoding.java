/*******************************************************************************
 * Copyright (c) 2016 IBH SYSTEMS GmbH and others.
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
import java.util.zip.Deflater;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipParameters;
import org.apache.commons.compress.compressors.lzma.LZMACompressorInputStream;
import org.apache.commons.compress.compressors.lzma.LZMACompressorOutputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream;
import org.apache.commons.compress.compressors.zstandard.ZstdCompressorInputStream;
import org.apache.commons.compress.compressors.zstandard.ZstdCompressorOutputStream;
import org.apache.commons.compress.compressors.zstandard.ZstdUtils;
import org.eclipse.packagedrone.utils.rpm.deps.Dependency;
import org.eclipse.packagedrone.utils.rpm.deps.RpmDependencyFlags;
import org.tukaani.xz.LZMA2Options;

public enum PayloadCoding
{
    NONE ( null ),
    GZIP ( "gzip" ),
    BZIP2 ( "bzip2" ),
    LZMA ( "lzma" ),
    XZ ( "xz" ),
    ZSTD ( "zstd" );

    private String coding;

    private PayloadCoding ( String coding )
    {
        this.coding = coding;
    }

    public String getCoding ()
    {
        return coding;
    }

    public static PayloadCoding fromCoding ( String coding ) throws IOException
    {
        if ( coding == null )
        {
            return PayloadCoding.NONE;
        }

        for ( PayloadCoding payloadCoding : PayloadCoding.values () )
        {
            if ( coding.equals ( payloadCoding.getCoding () ) )
            {
                return payloadCoding;
            }
        }

        throw new IOException ( String.format ( "Unknown coding: %s", coding ) );
    }

    public static Optional<Dependency> getDependency ( String coding ) throws IOException
    {
        final PayloadCoding payloadCoding = PayloadCoding.fromCoding ( coding );

        switch ( payloadCoding )
        {
            case NONE:
                return Optional.empty ();
            case GZIP:
                return Optional.empty ();
            case BZIP2:
                return Optional.of ( new Dependency ( "PayloadIsBzip2", "3.0.5-1", RpmDependencyFlags.LESS, RpmDependencyFlags.EQUAL, RpmDependencyFlags.RPMLIB ) );
            case LZMA:
                return Optional.of ( new Dependency ( "PayloadIsLzma", "4.4.6-1", RpmDependencyFlags.LESS, RpmDependencyFlags.EQUAL, RpmDependencyFlags.RPMLIB ) );
            case XZ:
                return Optional.of ( new Dependency ( "PayloadIsXz", "5.2-1", RpmDependencyFlags.LESS, RpmDependencyFlags.EQUAL, RpmDependencyFlags.RPMLIB ) );
            case ZSTD:
                return Optional.of ( new Dependency ( "PayloadIsZstd", "5.4.18-1", RpmDependencyFlags.LESS, RpmDependencyFlags.EQUAL, RpmDependencyFlags.RPMLIB ) );
            default:
                throw new IOException ( String.format ( "Unknown coding: %s", coding ) );
        }
    }

    public static InputStream createInputStream ( String coding, InputStream in ) throws IOException
    {
        final PayloadCoding payloadCoding = PayloadCoding.fromCoding ( coding );

        switch ( payloadCoding )
        {
            case NONE:
                return in;
            case GZIP:
                return new GzipCompressorInputStream ( in );
            case BZIP2:
                return new BZip2CompressorInputStream ( in );
            case LZMA:
                return new LZMACompressorInputStream ( in );
            case XZ:
                return new XZCompressorInputStream ( in );
            case ZSTD:
                if ( !ZstdUtils.isZstdCompressionAvailable () )
                {
                    throw new IOException( "Zstandard compression is not available" );
                }

                return new ZstdCompressorInputStream ( in );
            default:
                throw new IOException ( String.format ( "Unknown coding: %s", coding ) );
        }
    }

    public static OutputStream createOutputStream ( String coding, OutputStream out, Optional<String> optionalFlags ) throws IOException
    {
        final PayloadCoding payloadCoding = PayloadCoding.fromCoding ( coding );

        final String flags;

        switch ( payloadCoding )
        {
            case NONE:
                return out;
            case GZIP:
                final GzipParameters parameters = new GzipParameters ();
                final int compressionLevel;

                if ( optionalFlags.isPresent () && ( flags = optionalFlags.get () ).length() > 0 )
                {
                    compressionLevel = Integer.parseInt ( flags.substring ( 0, 1 ) );
                }
                else
                {
                    compressionLevel = Deflater.BEST_COMPRESSION;
                }

                parameters.setCompressionLevel ( compressionLevel );

                return new GzipCompressorOutputStream ( out, parameters );
            case BZIP2:
                final int blockSize;

                if ( optionalFlags.isPresent () && ( flags = optionalFlags.get () ).length() > 0 )
                {
                    blockSize = Integer.parseInt ( flags.substring ( 0, 1 ) );
                }
                else
                {
                    blockSize = BZip2CompressorOutputStream.MAX_BLOCKSIZE;
                }

                return new BZip2CompressorOutputStream ( out, blockSize );
            case LZMA:
                return new LZMACompressorOutputStream ( out );
            case XZ:
                final int preset;

                if ( optionalFlags.isPresent () && ( flags = optionalFlags.get () ).length() > 0 )
                {
                    preset = Integer.parseInt ( flags.substring ( 0, 1 ) );
                }
                else
                {
                    preset = LZMA2Options.PRESET_DEFAULT;
                }

                return new XZCompressorOutputStream ( out, preset );
            case ZSTD:
                if ( !ZstdUtils.isZstdCompressionAvailable () )
                {
                    throw new IOException( "Zstandard compression is not available" );
                }

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

        throw new IOException ( String.format ( "Unknown coding: %s", coding ) );
    }
}
