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
package org.eclipse.packagedrone.repo.signing.pgp.web;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

public class AddEntry
{
    private String keyring;

    @NotEmpty
    @NotNull
    private String keyId;

    private String keyPassphrase;

    private String label;

    public void setLabel ( final String description )
    {
        this.label = description;
    }

    public String getLabel ()
    {
        return this.label;
    }

    public String getKeyring ()
    {
        return this.keyring;
    }

    public void setKeyring ( final String keyring )
    {
        this.keyring = keyring;
    }

    public String getKeyId ()
    {
        return this.keyId;
    }

    public void setKeyId ( final String keyId )
    {
        this.keyId = keyId;
    }

    public String getKeyPassphrase ()
    {
        return this.keyPassphrase;
    }

    public void setKeyPassphrase ( final String keyPassphrase )
    {
        this.keyPassphrase = keyPassphrase;
    }

}
