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
import java.util.Objects;

import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureGenerator;
import org.bouncycastle.openpgp.operator.bc.BcPGPContentSignerBuilder;
import org.eclipse.packagedrone.utils.rpm.RpmSignatureTag;
import org.eclipse.packagedrone.utils.rpm.header.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RsaHeaderSignatureProcessor implements SignatureProcessor
{
    private final static Logger logger = LoggerFactory.getLogger ( RsaHeaderSignatureProcessor.class );

    public static enum HashAlgorithm
    {
        SHA1 ( HashAlgorithmTags.SHA1 ),
        SHA512 ( HashAlgorithmTags.SHA512 );

        private int value;

        private HashAlgorithm ( final int value )
        {
            this.value = value;
        }

        public int getValue ()
        {
            return this.value;
        }

        /**
         * Get a hash algorithm from a string
         * <p>
         * This method will return the hash algorithm as specified by the
         * parameter "name". If this parameter is {@code null} or an empty
         * string, then the default algorithm {@link #SHA1} will be returned. If
         * algorithm is an invalid name, then an exception is thrown.
         * </p>
         *
         * @param name
         *            the name of hash algorithm, or {@code null}
         * @return a hash algorithm
         * @throws IllegalArgumentException
         *             if the name was provided, but is invalid
         */
        public static HashAlgorithm from ( final String name )
        {
            if ( name == null || name.isEmpty () )
            {
                return SHA1;
            }

            return HashAlgorithm.valueOf ( name );
        }
    }

    private final PGPPrivateKey privateKey;

    private final int hashAlgorithm;

    private byte[] value;

    protected RsaHeaderSignatureProcessor ( final PGPPrivateKey privateKey, final int hashAlgorithm )
    {
        Objects.requireNonNull ( privateKey );
        this.privateKey = privateKey;
        this.hashAlgorithm = hashAlgorithm;
    }

    public RsaHeaderSignatureProcessor ( final PGPPrivateKey privateKey, final HashAlgorithm hashAlgorithm )
    {
        this ( privateKey, Objects.requireNonNull ( hashAlgorithm ).getValue () );
    }

    public RsaHeaderSignatureProcessor ( final PGPPrivateKey privateKey )
    {
        this ( privateKey, HashAlgorithmTags.SHA1 );
    }

    @Override
    public void feedHeader ( final ByteBuffer header )
    {
        try
        {
            final BcPGPContentSignerBuilder contentSignerBuilder = new BcPGPContentSignerBuilder ( this.privateKey.getPublicKeyPacket ().getAlgorithm (), this.hashAlgorithm );
            final PGPSignatureGenerator signatureGenerator = new PGPSignatureGenerator ( contentSignerBuilder );

            signatureGenerator.init ( PGPSignature.BINARY_DOCUMENT, this.privateKey );

            if ( header.hasArray () )
            {
                signatureGenerator.update ( header.array (), header.position (), header.remaining () );
            }
            else
            {
                final byte[] buffer = new byte[header.remaining ()];
                header.get ( buffer );
                signatureGenerator.update ( buffer );
            }

            this.value = signatureGenerator.generate ().getEncoded ();
            logger.info ( "RSA HEADER: {}", this.value );
        }
        catch ( final Exception e )
        {
            throw new RuntimeException ( e );
        }
    }

    @Override
    public void feedPayloadData ( final ByteBuffer data )
    {
        // we only work on the header data
    }

    @Override
    public void finish ( final Header<RpmSignatureTag> signature )
    {
        signature.putBlob ( RpmSignatureTag.RSAHEADER, this.value );
    }
}
