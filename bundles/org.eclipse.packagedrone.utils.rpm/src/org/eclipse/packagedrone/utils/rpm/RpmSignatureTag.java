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
package org.eclipse.packagedrone.utils.rpm;

import java.util.HashMap;
import java.util.Map;

public enum RpmSignatureTag implements RpmBaseTag
{
    SHA1 ( 269 ),

    SIZE ( 1000 ),
    MD5 ( 1004 ),
    PAYLOAD_SIZE ( 1007 );

    private Integer value;

    private RpmSignatureTag ( final Integer value )
    {
        this.value = value;
    }

    @Override
    public Integer getValue ()
    {
        return this.value;
    }

    private final static Map<Integer, RpmSignatureTag> all = new HashMap<> ( RpmSignatureTag.values ().length );

    static
    {
        for ( final RpmSignatureTag tag : values () )
        {
            all.put ( tag.getValue (), tag );
        }
    }

    public static RpmSignatureTag find ( final Integer value )
    {
        return all.get ( value );
    }
}
