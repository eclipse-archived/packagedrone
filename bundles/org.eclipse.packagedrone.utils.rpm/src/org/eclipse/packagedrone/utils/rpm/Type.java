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
package org.eclipse.packagedrone.utils.rpm;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum Type
{
    BINARY ( (short)0 ),
    SOURCE ( (short)1 );

    private static final Map<Integer, Type> MAP = new HashMap<> ();

    static
    {
        for ( final Type type : Type.values () )
        {
            MAP.put ( (int)type.value, type );
        }
    }

    private short value;

    private Type ( final short value )
    {
        this.value = value;
    }

    public short getValue ()
    {
        return this.value;
    }

    public static Optional<Type> fromValue ( final int value )
    {
        return Optional.ofNullable ( MAP.get ( value ) );
    }
}
