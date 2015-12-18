/*******************************************************************************
 * Copyright (c) 2015 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.signing.pgp.internal.managed;

public class Configuration
{
    private String id;

    private String label;

    private String secretKey;

    private String passphrase;

    public void setId ( final String id )
    {
        this.id = id;
    }

    public String getId ()
    {
        return this.id;
    }

    public void setLabel ( final String label )
    {
        this.label = label;
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
