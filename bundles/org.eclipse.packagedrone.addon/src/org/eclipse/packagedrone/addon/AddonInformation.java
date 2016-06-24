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

public class AddonInformation
{
    private final State state;

    private final Throwable error;

    private final AddonDescription description;

    private final String stateInformation;

    public AddonInformation ( final State state, final AddonDescription description, final Throwable error, final String stateInformation )
    {
        this.state = state;
        this.error = error;
        this.description = description;
        this.stateInformation = stateInformation;
    }

    public State getState ()
    {
        return this.state;
    }

    public Throwable getError ()
    {
        return this.error;
    }

    public AddonDescription getDescription ()
    {
        return this.description;
    }

    public String getStateInformation ()
    {
        return this.stateInformation;
    }
}
