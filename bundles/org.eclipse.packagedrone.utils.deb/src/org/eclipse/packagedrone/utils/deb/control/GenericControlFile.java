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
package org.eclipse.packagedrone.utils.deb.control;

import java.util.HashMap;
import java.util.Map;

/**
 * A generic control file <br>
 * This type can be used to implement other control files are directly.
 */
public class GenericControlFile
{
    protected final Map<String, String> values = new HashMap<> ();

    public GenericControlFile ()
    {
    }

    public void set ( final String name, final String value )
    {
        this.values.put ( name, value );
    }

    public String get ( final String field )
    {
        return this.values.get ( field );
    }

    public Map<String, String> getValues ()
    {
        return this.values;
    }
}
