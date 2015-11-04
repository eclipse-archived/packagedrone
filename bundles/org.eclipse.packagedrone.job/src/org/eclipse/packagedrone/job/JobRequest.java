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
package org.eclipse.packagedrone.job;

public class JobRequest
{
    private String factoryId;

    private String data;

    public JobRequest ()
    {
    }

    public JobRequest ( final String factoryId, final String data )
    {
        this.factoryId = factoryId;
        this.data = data;
    }

    public String getFactoryId ()
    {
        return this.factoryId;
    }

    public void setFactoryId ( final String factoryId )
    {
        this.factoryId = factoryId;
    }

    public String getData ()
    {
        return this.data;
    }

    public void setData ( final String data )
    {
        this.data = data;
    }

}
