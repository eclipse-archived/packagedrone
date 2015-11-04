/*******************************************************************************
 * Copyright (c) 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.signing.pgp;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.bc.BcPGPSecretKeyRingCollection;

public final class PgpHelper
{
    private PgpHelper ()
    {
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
