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

    public SigningStream ( final OutputStream stream, final PGPPrivateKey privateKey, final boolean inline, final String version )
    {
        this.stream = stream;
        this.privateKey = privateKey;
        this.inline = inline;
        this.version = version;
    }

    public SigningStream ( final OutputStream stream, final PGPPrivateKey privateKey, final boolean inline )
    {
        this ( stream, privateKey, inline, null );
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
            final int digest = HashAlgorithmTags.SHA1;
            this.signatureGenerator = new PGPSignatureGenerator ( new BcPGPContentSignerBuilder ( this.privateKey.getPublicKeyPacket ().getAlgorithm (), digest ) );
            this.signatureGenerator.init ( PGPSignature.BINARY_DOCUMENT, this.privateKey );

            this.armoredOutput = new ArmoredOutputStream ( this.stream );
            this.armoredOutput.setHeader ( "Version", this.version );

            if ( this.inline )
            {
                this.armoredOutput.beginClearText ( digest );
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
