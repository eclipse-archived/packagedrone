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
package org.eclipse.packagedrone.mail.web;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;

public class MailSettings
{
    private String username;

    private String password;

    private String host;

    @DecimalMin ( "0" )
    @DecimalMax ( "" + ( 64 * 1024 - 1 ) )
    private Integer port;

    private String from;

    private String prefix;

    private boolean enableStartTls;

    public void setEnableStartTls ( final boolean enableStartTls )
    {
        this.enableStartTls = enableStartTls;
    }

    public boolean isEnableStartTls ()
    {
        return this.enableStartTls;
    }

    public void setPrefix ( final String prefix )
    {
        this.prefix = prefix;
    }

    public String getPrefix ()
    {
        return this.prefix;
    }

    public void setFrom ( final String from )
    {
        this.from = from;
    }

    public String getFrom ()
    {
        return this.from;
    }

    public void setPort ( final Integer port )
    {
        this.port = port;
    }

    public Integer getPort ()
    {
        return this.port;
    }

    public String getUsername ()
    {
        return this.username;
    }

    public void setUsername ( final String username )
    {
        this.username = username;
    }

    public String getPassword ()
    {
        return this.password;
    }

    public void setPassword ( final String password )
    {
        this.password = password;
    }

    public String getHost ()
    {
        return this.host;
    }

    public void setHost ( final String host )
    {
        this.host = host;
    }

}
