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

    private Class<?>[] supportedContexts;

    public TriggerProcessorState ( final String htmlState, final Class<?>[] supportedContexts )
    {
        this.htmlState = htmlState;
        this.supportedContexts = supportedContexts;
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

    public Class<?>[] getSupportedContexts ()
    {
        return this.supportedContexts;
    }

    public boolean supportsContext ( final Class<?> contextClass )
    {
        if ( contextClass == null )
        {
            return false;
        }

        for ( final Class<?> clazz : getSupportedContexts () )
        {
            if ( clazz.isAssignableFrom ( contextClass ) )
            {
                return true;
            }
        }

        return false;
    }
}
