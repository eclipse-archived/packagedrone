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

import java.util.Map;

public class JobRequest
{
    private String factoryId;

    private String data;

    private Map<String, String> properties;

    public JobRequest ()
    {
    }

    public JobRequest ( final String factoryId, final String data )
    {
        this.factoryId = factoryId;
        this.data = data;
        this.properties = null;
    }

    public JobRequest ( final String factoryId, final String data, final Map<String, String> properties )
    {
        this.factoryId = factoryId;
        this.data = data;
        this.properties = properties;
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

    public void setProperties ( final Map<String, String> properties )
    {
        this.properties = properties;
    }

    public Map<String, String> getProperties ()
    {
        return this.properties;
    }

}
