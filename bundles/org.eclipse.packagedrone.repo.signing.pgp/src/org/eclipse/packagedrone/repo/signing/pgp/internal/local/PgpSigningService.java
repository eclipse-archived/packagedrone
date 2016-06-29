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
package org.eclipse.packagedrone.repo.signing.pgp.internal.local;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.eclipse.packagedrone.repo.signing.pgp.internal.AbstractSecretKeySigningService;
import org.eclipse.packagedrone.utils.security.pgp.PgpHelper;

public class PgpSigningService extends AbstractSecretKeySigningService
{
    public static PgpSigningService create ( final File file, final String keyId, final String passphrase ) throws IOException, PGPException
    {
        try ( InputStream is = new FileInputStream ( file ) )
        {
            return new PgpSigningService ( is, keyId, passphrase );
        }
    }

    private static PGPSecretKey loadKey ( final InputStream keyring, final String keyId ) throws IOException, PGPException
    {
        final PGPSecretKey secretKey = PgpHelper.loadSecretKey ( keyring, keyId );
        if ( secretKey == null )
        {
            throw new IllegalStateException ( String.format ( "Signing key '%08X' could not be found", keyId ) );
        }
        return secretKey;
    }

    public PgpSigningService ( final InputStream keyring, final String keyId, final String passphrase ) throws IOException, PGPException
    {
        super ( loadKey ( keyring, keyId ), passphrase );
    }

}
