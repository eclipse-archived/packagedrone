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
package org.eclipse.packagedrone.web.common.menu;

import org.eclipse.packagedrone.web.common.Button;
import org.eclipse.packagedrone.web.common.Modifier;

public class FunctionalButton extends Button
{
    private final ButtonFunction function;

    public FunctionalButton ( final ButtonFunction function, final String label, final String icon, final Modifier modifier )
    {
        super ( label, icon, modifier );
        this.function = function;
    }

    public FunctionalButton ( final ButtonFunction function, final String label )
    {
        super ( label );
        this.function = function;
    }

    public ButtonFunction getFunction ()
    {
        return this.function;
    }

}
