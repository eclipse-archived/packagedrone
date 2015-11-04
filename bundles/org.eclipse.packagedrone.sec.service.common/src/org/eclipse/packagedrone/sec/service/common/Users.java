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
package org.eclipse.packagedrone.sec.service.common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Base64;

import org.eclipse.packagedrone.repo.utils.Tokens;

public class Users
{
    public static MessageDigest createDigest ()
    {
        try
        {
            return MessageDigest.getInstance ( "SHA-256" );
        }
        catch ( final NoSuchAlgorithmException e )
        {
            throw new IllegalStateException ( String.format ( "Message digest could not be created: SHA-256" ) );
        }
    }

    public static String hashIt ( final String salt, String data )
    {
        data = Normalizer.normalize ( data, Form.NFC );

        final byte[] strData = data.getBytes ( StandardCharsets.UTF_8 );
        final byte[] saltData = salt.getBytes ( StandardCharsets.UTF_8 );

        final byte[] first = new byte[saltData.length + strData.length];
        System.arraycopy ( saltData, 0, first, 0, saltData.length );
        System.arraycopy ( strData, 0, first, saltData.length, strData.length );

        final MessageDigest md = createDigest ();

        byte[] digest = md.digest ( first );
        final byte[] current = new byte[saltData.length + digest.length];

        for ( int i = 0; i < 1000; i++ )
        {
            System.arraycopy ( saltData, 0, current, 0, saltData.length );
            System.arraycopy ( digest, 0, current, saltData.length, digest.length );

            digest = md.digest ( current );
        }

        return Base64.getEncoder ().encodeToString ( digest );
    }

    public static String createToken ( final int size )
    {
        return Tokens.createToken ( size );
    }
}
