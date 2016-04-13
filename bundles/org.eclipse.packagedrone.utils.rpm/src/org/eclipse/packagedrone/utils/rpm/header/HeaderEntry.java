/*******************************************************************************
 * Copyright (c) 2016 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.utils.rpm.header;

public class HeaderEntry
{
    private final Type type;

    private final int tag;

    private final int count;

    private final byte[] data;

    public HeaderEntry ( final Type type, final int tag, final int count, final byte[] data )
    {
        this.type = type;
        this.tag = tag;
        this.count = count;
        this.data = data;
    }

    public Type getType ()
    {
        return this.type;
    }

    public int getTag ()
    {
        return this.tag;
    }

    public int getCount ()
    {
        return this.count;
    }

    public byte[] getData ()
    {
        return this.data;
    }
}
