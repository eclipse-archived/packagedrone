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
package org.eclipse.packagedrone.repo.utils;

import java.security.SecureRandom;

import org.eclipse.packagedrone.utils.Strings;

/**
 * Helper methods when working with tokens
 * <p>
 * A token is an array of random bytes, encoded in a lower case hex string. It
 * can be compared to a class 4 UUID, but with a flexible length and no other
 * class variants.
 * </p>
 * <p>
 * Tokens are used for creating salts, or e-mail verification links.
 * </p>
 */
public final class Tokens
{
    private static final SecureRandom random = new SecureRandom ();

    private Tokens ()
    {
    }

    /**
     * Create a random token
     * <p>
     * This method uses the internal instance of of a {@link SecureRandom}
     * generator to create a new token.
     * </p>
     *
     * @param length
     *            the length of the token
     * @return the newly created token, never returns <code>null</code>
     */
    public static String createToken ( final int length )
    {
        final byte[] data = new byte[length];

        random.nextBytes ( data );

        return Strings.hex ( data );
    }
}
