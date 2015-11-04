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
package org.eclipse.packagedrone.repo.adapter.deb.aspect.internal;

import java.util.Map;

public class ControlInformation
{
    private Map<String, String> values;

    public ControlInformation ( final Map<String, String> values )
    {
        this.values = values;
    }

    public void setValues ( final Map<String, String> values )
    {
        this.values = values;
    }

    public Map<String, String> getValues ()
    {
        return this.values;
    }
}
