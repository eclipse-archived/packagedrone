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
package org.eclipse.packagedrone.repo.manage.core.apm;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.packagedrone.repo.MetaKey;

public class CoreServiceModel
{
    private Map<MetaKey, String> properties;

    public CoreServiceModel ( final CoreServiceModel other )
    {
        this.properties = new HashMap<> ( other.properties );
    }

    public CoreServiceModel ( final Map<MetaKey, String> result )
    {
        this.properties = result;
    }

    public void setProperties ( final Map<MetaKey, String> properties )
    {
        this.properties = properties;
    }

    public Map<MetaKey, String> getProperties ()
    {
        return this.properties;
    }
}
