/*******************************************************************************
 * Copyright (c) 2015, 2016 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.utils.security.pgp;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.BCPGOutputStream;
import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureGenerator;
import org.bouncycastle.openpgp.operator.bc.BcPGPContentSignerBuilder;

public class SigningStream extends OutputStream
{
    private final OutputStream stream;

    private final PGPPrivateKey privateKey;

    private final boolean inline;

    private PGPSignatureGenerator signatureGenerator;

    private ArmoredOutputStream armoredOutput;

    private boolean initialized;

    private final String version;

    private final int digestAlgorithm;

    /**
     * Create a new signing stream
     *
     * @param stream
     *            the actual output stream
     * @param privateKey
     *            the private key to sign with
     * @param digestAlgorithm
     *            the digest algorithm to use, from {@link HashAlgorithmTags}
     * @param inline
     *            whether to sign inline or just write the signature
     * @param version
     *            the optional version which will be in the signature comment
     */
    public SigningStream ( final OutputStream stream, final PGPPrivateKey privateKey, final int digestAlgorithm, final boolean inline, final String version )
    {
        this.stream = stream;
        this.privateKey = privateKey;
        this.digestAlgorithm = digestAlgorithm;
        this.inline = inline;
        this.version = version;
    }

    /**
     * Create a new signing stream
     *
     * @param stream
     *            the actual output stream
     * @param privateKey
     *            the private key to sign with
     * @param digestAlgorithm
     *            the digest algorithm to use, from {@link HashAlgorithmTags}
     * @param inline
     *            whether to sign inline or just write the signature
     */
    public SigningStream ( final OutputStream stream, final PGPPrivateKey privateKey, final int digestAlgorithm, final boolean inline )
    {
        this ( stream, privateKey, digestAlgorithm, inline, null );
    }

    protected void testInit () throws IOException
    {
        if ( this.initialized )
        {
            return;
        }

        this.initialized = true;

        try
        {
            this.signatureGenerator = new PGPSignatureGenerator ( new BcPGPContentSignerBuilder ( this.privateKey.getPublicKeyPacket ().getAlgorithm (), this.digestAlgorithm ) );
            this.signatureGenerator.init ( PGPSignature.BINARY_DOCUMENT, this.privateKey );

            this.armoredOutput = new ArmoredOutputStream ( this.stream );
            if ( this.version != null )
            {
                this.armoredOutput.setHeader ( "Version", this.version );
            }

            if ( this.inline )
            {
                this.armoredOutput.beginClearText ( this.digestAlgorithm );
            }
        }
        catch ( final PGPException e )
        {
            throw new IOException ( e );
        }
    }

    @Override
    public void write ( final int b ) throws IOException
    {
        write ( new byte[] { (byte)b } );
    }

    @Override
    public void write ( final byte[] b, final int off, final int len ) throws IOException
    {
        Objects.requireNonNull ( b );

        testInit ();

        if ( this.inline )
        {
            this.armoredOutput.write ( b, off, len );
        }
        this.signatureGenerator.update ( b, off, len );
    }

    @Override
    public void close () throws IOException
    {
        testInit ();

        if ( this.inline )
        {
            this.armoredOutput.endClearText ();
        }

        try
        {
            final PGPSignature signature = this.signatureGenerator.generate ();
            signature.encode ( new BCPGOutputStream ( this.armoredOutput ) );
        }
        catch ( final PGPException e )
        {
            throw new IOException ( e );
        }

        this.armoredOutput.close ();

        super.close ();
    }
}
