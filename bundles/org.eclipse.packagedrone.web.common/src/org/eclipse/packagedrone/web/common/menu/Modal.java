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

public class Modal
{
    private String title;

    private String body;

    private Button[] buttons;

    public Modal ()
    {
    }

    public Modal ( final String title, final Button... buttons )
    {
        this.title = title;
        this.buttons = buttons;
    }

    public Modal ( final String title, final String body, final Button... buttons )
    {
        this.title = title;
        this.body = body;
        this.buttons = buttons;
    }

    public String getBody ()
    {
        return this.body;
    }

    public Button[] getButtons ()
    {
        return this.buttons;
    }

    public String getTitle ()
    {
        return this.title;
    }

    public void setBody ( final String body )
    {
        this.body = body;
    }

    public void setButtons ( final Button[] buttons )
    {
        this.buttons = buttons;
    }

    public void setTitle ( final String title )
    {
        this.title = title;
    }
}
