/*******************************************************************************
 * Copyright (c) 2014, 2016 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.utils.deb.internal;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Map;

/**
 * A filter input stream which create multiple message digests while reading
 * <br/>
 * Only the bytes actually read are processed.
 */
public class ChecksumInputStream extends FilterInputStream
{
    private final MessageDigest[] digests;

    private final Map<String, byte[]> results;

    public ChecksumInputStream ( final InputStream stream, final Map<String, byte[]> results, final MessageDigest... digests )
    {
        super ( stream );
        this.digests = digests;
        this.results = results;
    }

    @Override
    public int read ( final byte[] b, final int off, final int len ) throws IOException
    {
        final int result = super.read ( b, off, len );

        if ( result > 0 )
        {
            for ( final MessageDigest d : this.digests )
            {
                d.update ( b, off, result );
            }
        }

        return result;
    }

    @Override
    public int read () throws IOException
    {
        final int result = super.read ();
        if ( result > 0 )
        {
            for ( final MessageDigest d : this.digests )
            {
                d.update ( (byte)result );
            }
        }
        return result;
    }

    @Override
    public void close () throws IOException
    {
        super.close ();
        for ( final MessageDigest d : this.digests )
        {
            final byte[] result = d.digest ();
            this.results.put ( d.getAlgorithm (), result );
        }
    }

    public Map<String, byte[]> getResults ()
    {
        return this.results;
    }
}
