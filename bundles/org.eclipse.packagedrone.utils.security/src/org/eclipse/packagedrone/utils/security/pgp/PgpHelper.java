/*******************************************************************************
 * Copyright (c) 2015, 2016 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.utils.security.pgp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPKeyRing;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.bc.BcPGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.operator.bc.BcPBESecretKeyDecryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;

public final class PgpHelper
{
    private PgpHelper ()
    {
    }

    public static InputStream fromString ( final String data )
    {
        return new ByteArrayInputStream ( data.getBytes ( StandardCharsets.US_ASCII ) );
    }

    public static Stream<PGPKeyRing> streamKeyring ( final InputStream input ) throws IOException, PGPException
    {
        final BcPGPSecretKeyRingCollection keyrings = new BcPGPSecretKeyRingCollection ( PGPUtil.getDecoderStream ( input ) );

        final Iterator<?> keyRingIter = keyrings.getKeyRings ();

        final Stream<?> s = StreamSupport.stream ( Spliterators.spliteratorUnknownSize ( keyRingIter, Spliterator.ORDERED ), false );

        return s.map ( o -> (PGPKeyRing)o );
    }

    public static Stream<PGPSecretKeyRing> streamSecretKeyring ( final InputStream input ) throws IOException, PGPException
    {
        final Stream<PGPKeyRing> s = streamKeyring ( input );
        return s.filter ( k -> k instanceof PGPSecretKeyRing ).map ( o -> (PGPSecretKeyRing)o );
    }

    public static Stream<PGPSecretKey> streamSecretKeys ( final InputStream input ) throws IOException, PGPException
    {
        final Stream<PGPSecretKeyRing> s = streamSecretKeyring ( input );
        return s.flatMap ( k -> {
            final Iterator<?> i = k.getSecretKeys ();

            final Stream<?> ks = StreamSupport.stream ( Spliterators.spliteratorUnknownSize ( i, Spliterator.ORDERED ), false );

            return ks.map ( o -> (PGPSecretKey)o );
        } );
    }

    public static String makeShortKey ( final PGPSecretKey key )
    {
        final long shortId = key.getKeyID () & 0xFFFFFFFFL;
        return String.format ( "%08X", shortId );
    }

    public static Predicate<PGPSecretKey> keyShortId ( final String keyId )
    {
        final long keyIdNum = Long.parseUnsignedLong ( keyId, 16 );

        return new Predicate<PGPSecretKey> () {

            @Override
            public boolean test ( final PGPSecretKey key )
            {
                final long shortId = key.getKeyID () & 0xFFFFFFFFL;

                if ( key.getKeyID () != keyIdNum && shortId != keyIdNum )
                {
                    return false;
                }

                return true;
            }
        };
    }

    public static PGPPrivateKey loadPrivateKey ( final InputStream input, final String keyId, final String passPhrase ) throws IOException, PGPException
    {
        return loadPrivateKey ( input, keyId, passPhrase != null ? passPhrase.toCharArray () : null );
    }

    public static PGPPrivateKey loadPrivateKey ( final InputStream input, final String keyId, final char[] passPhrase ) throws IOException, PGPException
    {
        final PGPSecretKey secretKey = loadSecretKey ( input, keyId );
        if ( secretKey == null )
        {
            return null;
        }

        return secretKey.extractPrivateKey ( new BcPBESecretKeyDecryptorBuilder ( new BcPGPDigestCalculatorProvider () ).build ( passPhrase ) );
    }

    public static PGPSecretKey loadSecretKey ( final InputStream input, final String keyId ) throws IOException, PGPException
    {
        final long keyIdNum = Long.parseUnsignedLong ( keyId, 16 );

        final BcPGPSecretKeyRingCollection keyrings = new BcPGPSecretKeyRingCollection ( PGPUtil.getDecoderStream ( input ) );

        final Iterator<?> keyRingIter = keyrings.getKeyRings ();
        while ( keyRingIter.hasNext () )
        {
            final PGPSecretKeyRing secretKeyRing = (PGPSecretKeyRing)keyRingIter.next ();

            final Iterator<?> secretKeyIterator = secretKeyRing.getSecretKeys ();
            while ( secretKeyIterator.hasNext () )
            {
                final PGPSecretKey key = (PGPSecretKey)secretKeyIterator.next ();

                if ( !key.isSigningKey () )
                {
                    continue;
                }

                final long shortId = key.getKeyID () & 0xFFFFFFFFL;

                if ( key.getKeyID () != keyIdNum && shortId != keyIdNum )
                {
                    continue;
                }

                return key;
            }
        }

        return null;
    }
}
