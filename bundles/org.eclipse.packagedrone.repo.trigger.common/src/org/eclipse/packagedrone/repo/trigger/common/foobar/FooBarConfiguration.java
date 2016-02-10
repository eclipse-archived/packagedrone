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
package org.eclipse.packagedrone.repo.trigger.common.foobar;

import com.google.gson.GsonBuilder;

public class FooBarConfiguration
{
    private boolean flag1;

    private String string1;

    public void setFlag1 ( final boolean flag1 )
    {
        this.flag1 = flag1;
    }

    public boolean isFlag1 ()
    {
        return this.flag1;
    }

    public void setString1 ( final String string1 )
    {
        this.string1 = string1;
    }

    public String getString1 ()
    {
        return this.string1;
    }

    public static FooBarConfiguration fromJson ( final String configuration )
    {
        if ( configuration == null )
        {
            return null;
        }

        return new GsonBuilder ().create ().fromJson ( configuration, FooBarConfiguration.class );
    }

    public String toJson ()
    {
        return new GsonBuilder ().create ().toJson ( this );
    }
}
