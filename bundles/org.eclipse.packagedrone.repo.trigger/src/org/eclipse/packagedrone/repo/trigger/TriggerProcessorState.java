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
package org.eclipse.packagedrone.repo.trigger;

public class TriggerProcessorState
{
    private Throwable instantiationError;

    private String htmlState;

    public TriggerProcessorState ( final String htmlState )
    {
        this.htmlState = htmlState;
    }

    public TriggerProcessorState ( final Throwable instantiationError )
    {
        this.instantiationError = instantiationError;
    }

    public boolean isValid ()
    {
        return this.instantiationError == null;
    }

    public String getHtmlState ()
    {
        return this.htmlState;
    }

    public Throwable getInstantiationError ()
    {
        return this.instantiationError;
    }
}
