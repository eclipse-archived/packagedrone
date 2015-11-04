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
package org.eclipse.packagedrone.sec;

import java.util.Date;
import java.util.Set;

public class DatabaseDetails
{
    private final DatabaseDetailsBean bean;

    public DatabaseDetails ( final DatabaseDetailsBean bean )
    {
        this.bean = new DatabaseDetailsBean ( bean );
    }

    public Set<String> getRoles ()
    {
        return this.bean.getRoles ();
    }

    public Date getEmailTokenDate ()
    {
        return this.bean.getEmailTokenDate ();
    }

    public Date getRegistrationDate ()
    {
        return this.bean.getRegistrationDate ();
    }

    public String getName ()
    {
        return this.bean.getName ();
    }

    public String getEmail ()
    {
        return this.bean.getEmail ();
    }

    public boolean isEmailVerified ()
    {
        return this.bean.isEmailVerified ();
    }

    public boolean isLocked ()
    {
        return this.bean.isLocked ();
    }

    public boolean isDeleted ()
    {
        return this.bean.isDeleted ();
    }

    @Override
    public String toString ()
    {
        return this.bean.toString ();
    }
}
