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
package org.eclipse.packagedrone.addon;

import org.osgi.framework.Version;

public class AddonDescription
{
    private final String id;

    private final String label;

    private final Version version;

    public AddonDescription ( final String id, final Version version, final String label )
    {
        this.id = id;
        this.version = version;
        this.label = label;
    }

    public String getId ()
    {
        return this.id;
    }

    public Version getVersion ()
    {
        return this.version;
    }

    public String getLabel ()
    {
        return this.label;
    }
}
