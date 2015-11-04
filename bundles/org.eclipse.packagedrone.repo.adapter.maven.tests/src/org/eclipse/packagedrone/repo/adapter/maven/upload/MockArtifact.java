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
package org.eclipse.packagedrone.repo.adapter.maven.upload;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Map;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.scada.utils.str.StringHelper;

import com.google.common.io.ByteStreams;

public class MockArtifact
{
    private final String id;

    private final String parentId;

    private final String name;

    private final byte[] data;

    private final Map<MetaKey, String> metaData;

    public MockArtifact ( final String id, final String parentId, final String name, final InputStream stream, final Map<MetaKey, String> metaData ) throws IOException
    {
        this.id = id;
        this.parentId = parentId;
        this.name = name;
        if ( stream != null )
        {
            this.data = ByteStreams.toByteArray ( stream );
        }
        else
        {
            this.data = new byte[0];
        }
        this.metaData = metaData;
    }

    public String getId ()
    {
        return this.id;
    }

    public String getParentId ()
    {
        return this.parentId;
    }

    public String getName ()
    {
        return this.name;
    }

    public byte[] getData ()
    {
        return this.data;
    }

    public Map<MetaKey, String> getMetaData ()
    {
        return this.metaData;
    }

    public String calcChecksum ( final String algorithm )
    {
        try
        {
            final MessageDigest md = MessageDigest.getInstance ( algorithm );
            final byte[] result = md.digest ( this.data );

            return StringHelper.toHex ( result );
        }
        catch ( final Exception e )
        {
            return null;
        }
    }
}
