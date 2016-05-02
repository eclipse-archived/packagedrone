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

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.eclipse.packagedrone.utils.rpm.RpmSignatureTag;
import org.eclipse.packagedrone.utils.rpm.Rpms;
import org.eclipse.packagedrone.utils.rpm.header.Header;

public final class SignatureProcessors
{
    private SignatureProcessors ()
    {
    }

    public static SignatureProcessor size ()
    {
        return new SignatureProcessor () {

            private long headerSize;

            private long payloadSize;

            @Override
            public void feedHeader ( final ByteBuffer header )
            {
                this.headerSize = header.remaining ();
            }

            @Override
            public void feedPayloadData ( final ByteBuffer data )
            {
                this.payloadSize += data.remaining ();
            }

            @Override
            public void finish ( final Header<RpmSignatureTag> signature )
            {
                signature.putSize ( this.headerSize + this.payloadSize, RpmSignatureTag.SIZE, RpmSignatureTag.LONGSIZE );
            }
        };
    }

    public static SignatureProcessor payloadSize ()
    {
        return new SignatureProcessor () {

            private long archiveSize;

            @Override
            public void init ( final long archiveSize )
            {
                this.archiveSize = archiveSize;
            }

            @Override
            public void feedHeader ( final ByteBuffer header )
            {
            }

            @Override
            public void feedPayloadData ( final ByteBuffer data )
            {
            }

            @Override
            public void finish ( final Header<RpmSignatureTag> signature )
            {
                signature.putSize ( this.archiveSize, RpmSignatureTag.PAYLOAD_SIZE, RpmSignatureTag.LONGARCHIVESIZE );
            }
        };
    }

    public static SignatureProcessor sha1Header ()
    {
        return new SignatureProcessor () {

            private String value;

            @Override
            public void feedHeader ( final ByteBuffer header )
            {
                try
                {
                    final MessageDigest md = MessageDigest.getInstance ( "SHA1" );
                    md.update ( header.slice () );
                    this.value = Rpms.toHex ( md.digest () ).toLowerCase ();
                }
                catch ( final NoSuchAlgorithmException e )
                {
                    throw new RuntimeException ( e );
                }
            }

            @Override
            public void feedPayloadData ( final ByteBuffer data )
            {
                // we only work with the header
            }

            @Override
            public void finish ( final Header<RpmSignatureTag> signature )
            {
                signature.putString ( RpmSignatureTag.SHA1HEADER, this.value );
            }
        };
    }

    public static SignatureProcessor md5 ()
    {
        return new SignatureProcessor () {

            private MessageDigest digest;

            @Override
            public void init ( final long archiveSize )
            {
                try
                {
                    this.digest = MessageDigest.getInstance ( "MD5" );
                }
                catch ( final NoSuchAlgorithmException e )
                {
                    throw new RuntimeException ( e );
                }
            }

            @Override
            public void feedHeader ( final ByteBuffer header )
            {
                this.digest.update ( header );
            }

            @Override
            public void feedPayloadData ( final ByteBuffer data )
            {
                this.digest.update ( data );
            }

            @Override
            public void finish ( final Header<RpmSignatureTag> signature )
            {
                signature.putBlob ( RpmSignatureTag.MD5, this.digest.digest () );
            }
        };
    }
}
