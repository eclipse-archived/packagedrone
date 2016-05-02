/*******************************************************************************
 * Copyright (c) 2016 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.utils.rpm.signature;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.eclipse.packagedrone.utils.rpm.RpmSignatureTag;
import org.eclipse.packagedrone.utils.rpm.Rpms;
import org.eclipse.packagedrone.utils.rpm.build.PayloadProvider;
import org.eclipse.packagedrone.utils.rpm.header.Header;

import com.google.common.io.ByteStreams;

public final class SignatureProcessors
{
    private SignatureProcessors ()
    {
    }

    private static final SignatureProcessor SIZE = new SignatureProcessor () {

        @Override
        public void sign ( final ByteBuffer header, final PayloadProvider payloadProvider, final Header<RpmSignatureTag> signature ) throws IOException
        {
            signature.putSize ( header.remaining () + payloadProvider.getPayloadSize (), RpmSignatureTag.SIZE, RpmSignatureTag.LONGSIZE );
        }
    };

    private static final SignatureProcessor PAYLOAD_SIZE = new SignatureProcessor () {

        @Override
        public void sign ( final ByteBuffer header, final PayloadProvider payloadProvider, final Header<RpmSignatureTag> signature ) throws IOException
        {
            signature.putSize ( payloadProvider.getArchiveSize (), RpmSignatureTag.PAYLOAD_SIZE, RpmSignatureTag.LONGARCHIVESIZE );
        }
    };

    private static final SignatureProcessor SHA1_HEADER = new SignatureProcessor () {

        @Override
        public void sign ( final ByteBuffer header, final PayloadProvider payloadProvider, final Header<RpmSignatureTag> signature ) throws IOException
        {
            signature.putString ( RpmSignatureTag.SHA1HEADER, makeSha1HeaderChecksum ( header ) );
        }

        private String makeSha1HeaderChecksum ( final ByteBuffer header )
        {
            try
            {
                final MessageDigest md = MessageDigest.getInstance ( "SHA1" );
                md.update ( header.slice () );
                return Rpms.toHex ( md.digest () ).toLowerCase ();
            }
            catch ( final NoSuchAlgorithmException e )
            {
                throw new RuntimeException ( e );
            }
        }

    };

    private static final SignatureProcessor MD5 = new SignatureProcessor () {
        @Override
        public void sign ( final ByteBuffer header, final PayloadProvider payloadProvider, final Header<RpmSignatureTag> signature ) throws IOException
        {
            signature.putBlob ( RpmSignatureTag.MD5, makeMd5Checksum ( header, payloadProvider ) );
        }

        private byte[] makeMd5Checksum ( final ByteBuffer header, final PayloadProvider payloadProvider ) throws IOException
        {
            try
            {
                final MessageDigest md = MessageDigest.getInstance ( "MD5" );

                // feed header

                md.update ( header.slice () );

                // feed payload file

                try ( ReadableByteChannel payloadChannel = payloadProvider.openChannel () )
                {
                    ByteStreams.copy ( new DigestInputStream ( Channels.newInputStream ( payloadChannel ), md ), ByteStreams.nullOutputStream () );
                }

                // digest

                return md.digest ();
            }
            catch ( final NoSuchAlgorithmException e )
            {
                throw new RuntimeException ( e );
            }
        }

    };

    public static SignatureProcessor size ()
    {
        return SIZE;
    }

    public static SignatureProcessor payloadSize ()
    {
        return PAYLOAD_SIZE;
    }

    public static SignatureProcessor sha1Header ()
    {
        return SHA1_HEADER;
    }

    public static SignatureProcessor md5 ()
    {
        return MD5;
    }
}
