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
package org.eclipse.packagedrone.repo.signing.pgp.web.managed;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

public class AddEntry
{
    private String label;

    @NotNull
    @NotEmpty
    private String secretKey;

    private String passphrase;

    public void setLabel ( final String description )
    {
        this.label = description;
    }

    public String getLabel ()
    {
        return this.label;
    }

    public void setSecretKey ( final String secretKey )
    {
        this.secretKey = secretKey;
    }

    public String getSecretKey ()
    {
        return this.secretKey;
    }

    public void setPassphrase ( final String passphrase )
    {
        this.passphrase = passphrase;
    }

    public String getPassphrase ()
    {
        return this.passphrase;
    }

}
