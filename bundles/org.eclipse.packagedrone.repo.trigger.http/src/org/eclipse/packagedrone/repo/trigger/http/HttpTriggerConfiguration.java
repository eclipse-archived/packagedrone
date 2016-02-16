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
package org.eclipse.packagedrone.repo.trigger.http;

import org.hibernate.validator.constraints.NotBlank;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class HttpTriggerConfiguration
{
    @NotBlank
    private String endpoint;

    public void setEndpoint ( final String endpoint )
    {
        this.endpoint = endpoint;
    }

    public String getEndpoint ()
    {
        return this.endpoint;
    }

    protected static Gson createGson ()
    {
        final GsonBuilder gb = new GsonBuilder ();
        return gb.create ();
    }

    public String toJson ()
    {
        return createGson ().toJson ( this );
    }

    public static HttpTriggerConfiguration fromJson ( final String json )
    {
        return createGson ().fromJson ( json, HttpTriggerConfiguration.class );
    }
}
