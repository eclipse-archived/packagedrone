/*******************************************************************************
 * Copyright (c) 2015, 2016 IBH SYSTEMS GmbH.
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
    PUBKEYS ( 266 ),
    RSAHEADER ( 268 ),
    SHA1HEADER ( 269 ),
    LONGARCHIVESIZE ( 271 ),
    SHA256HEADER ( 273 ),

    SIZE ( 1000 ),
    PGP ( 1002 ),
    MD5 ( 1004 ),
    PAYLOAD_SIZE ( 1007 ),
    LONGSIZE ( 5009 );

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
