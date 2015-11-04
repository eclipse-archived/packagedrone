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
package org.eclipse.packagedrone.repo.channel.web;

import org.hibernate.validator.constraints.NotEmpty;

public class ConfigureFileSystem
{
    @NotEmpty
    private String location;

    public void setLocation ( final String location )
    {
        this.location = location;
    }

    public String getLocation ()
    {
        return this.location;
    }
}
