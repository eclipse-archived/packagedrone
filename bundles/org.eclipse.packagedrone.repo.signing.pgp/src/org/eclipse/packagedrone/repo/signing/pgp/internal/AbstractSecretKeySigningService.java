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
package org.eclipse.packagedrone.repo.signing.pgp.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.BCPGOutputStream;
import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureGenerator;
import org.bouncycastle.openpgp.operator.bc.BcPBESecretKeyDecryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;
import org.eclipse.packagedrone.VersionInformation;
import org.eclipse.packagedrone.repo.signing.SigningService;
import org.eclipse.packagedrone.utils.security.pgp.SigningStream;

public abstract class AbstractSecretKeySigningService implements SigningService
{
    private static final byte[] NL_DATA = "\n".getBytes ( StandardCharsets.UTF_8 );

    private final PGPSecretKey secretKey;

    private final PGPPrivateKey privateKey;

    public AbstractSecretKeySigningService ( final PGPSecretKey secretKey, final String passphrase ) throws PGPException
    {
        this.secretKey = secretKey;
        this.privateKey = this.secretKey.extractPrivateKey ( new BcPBESecretKeyDecryptorBuilder ( new BcPGPDigestCalculatorProvider () ).build ( passphrase.toCharArray () ) );
    }

    @Override
    public void printPublicKey ( final OutputStream out ) throws IOException
    {
        final ArmoredOutputStream armoredOutput = new ArmoredOutputStream ( out );
        armoredOutput.setHeader ( "Version", VersionInformation.VERSIONED_PRODUCT );

        final PGPPublicKey pubKey = this.secretKey.getPublicKey ();
        pubKey.encode ( new BCPGOutputStream ( armoredOutput ) );
        armoredOutput.close ();
    }

    @Override
    public OutputStream signingStream ( final OutputStream stream, final boolean inline )
    {
        return new SigningStream ( stream, this.privateKey, HashAlgorithmTags.SHA1, inline, VersionInformation.VERSIONED_PRODUCT );
    }

    @Override
    public void sign ( final InputStream in, final OutputStream out, final boolean inline ) throws Exception
    {
        final int digest = HashAlgorithmTags.SHA1;
        final PGPSignatureGenerator signatureGenerator = new PGPSignatureGenerator ( new BcPGPContentSignerBuilder ( this.privateKey.getPublicKeyPacket ().getAlgorithm (), digest ) );

        if ( inline )
        {
            signatureGenerator.init ( PGPSignature.CANONICAL_TEXT_DOCUMENT, this.privateKey );
        }
        else
        {
            signatureGenerator.init ( PGPSignature.BINARY_DOCUMENT, this.privateKey );
        }

        final ArmoredOutputStream armoredOutput = new ArmoredOutputStream ( out );
        armoredOutput.setHeader ( "Version", VersionInformation.VERSIONED_PRODUCT );

        if ( inline )
        {
            armoredOutput.beginClearText ( digest );

            final LineNumberReader lnr = new LineNumberReader ( new InputStreamReader ( in, StandardCharsets.UTF_8 ) );

            String line;
            while ( ( line = lnr.readLine () ) != null )
            {
                if ( lnr.getLineNumber () > 1 )
                {
                    signatureGenerator.update ( NL_DATA );
                }

                final byte[] data = trimTrailing ( line ).getBytes ( StandardCharsets.UTF_8 );

                if ( inline )
                {
                    armoredOutput.write ( data );
                    armoredOutput.write ( NL_DATA );
                }
                signatureGenerator.update ( data );
            }

            armoredOutput.endClearText ();
        }
        else
        {

            final byte[] buffer = new byte[4096];
            int rc;
            while ( ( rc = in.read ( buffer ) ) >= 0 )
            {
                signatureGenerator.update ( buffer, 0, rc );
            }
        }

        final PGPSignature signature = signatureGenerator.generate ();
        signature.encode ( new BCPGOutputStream ( armoredOutput ) );

        armoredOutput.close ();
    }

    private static String trimTrailing ( final String line )
    {
        final char[] content = line.toCharArray ();
        int idx = content.length - 1;

        loop: while ( idx > 0 )
        {
            switch ( content[idx] )
            {
                case ' ':
                case '\t':
                    idx--;
                    break;
                default:
                    break loop;
            }
        }

        return String.valueOf ( content, 0, idx + 1 );
    }
}
