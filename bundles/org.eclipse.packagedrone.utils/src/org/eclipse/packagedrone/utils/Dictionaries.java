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
package org.eclipse.packagedrone.utils;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

public final class Dictionaries
{
    private Dictionaries ()
    {
    }

    public static Hashtable<String, Object> copy ( final Dictionary<String, ?> properties )
    {
        final Hashtable<String, Object> newProps = new Hashtable<> ( properties.size () );
        final Enumeration<String> en = properties.keys ();
        while ( en.hasMoreElements () )
        {
            final String key = en.nextElement ();
            newProps.put ( key, properties.get ( key ) );
        }
        return newProps;
    }
}
