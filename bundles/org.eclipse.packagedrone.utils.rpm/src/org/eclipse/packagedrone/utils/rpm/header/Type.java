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

public enum Type
{
    NULL ( 0, 1 ), //
    CHAR ( 1, 1 ), //
    BYTE ( 2, 1 ), //
    SHORT ( 3, 2 ), //
    INT ( 4, 4 ), //
    LONG ( 5, 8 ), //
    STRING ( 6, 1 ), //
    BLOB ( 7, 1 ), //
    STRING_ARRAY ( 8, 1 ), //
    I18N_STRING ( 9, 1 ), //
    ;

    private int type;

    private int align;

    private Type ( final int type, final int align )
    {
        this.type = type;
        this.align = align;
    }

    public int type ()
    {
        return this.type;
    }

    public int align ()
    {
        return this.align;
    }
}
