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

import java.util.Collections;
import java.util.Map;

import org.eclipse.packagedrone.repo.MetaKey;

public class CoreServiceViewModel
{
    private final Map<MetaKey, String> properties;

    CoreServiceViewModel ( final Map<MetaKey, String> properties )
    {
        this.properties = Collections.unmodifiableMap ( properties );
    }

    public Map<MetaKey, String> getProperties ()
    {
        return this.properties;
    }
}
