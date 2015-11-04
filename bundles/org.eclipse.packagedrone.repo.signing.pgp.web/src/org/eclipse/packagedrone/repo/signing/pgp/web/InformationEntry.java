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

public class InformationEntry
{
    private String id;

    boolean servicePresent;

    private String keyring;

    private String keyId;

    private String label;

    public void setLabel ( final String description )
    {
        this.label = description;
    }

    public String getLabel ()
    {
        return this.label;
    }

    public void setId ( final String id )
    {
        this.id = id;
    }

    public String getId ()
    {
        return this.id;
    }

    public void setServicePresent ( final boolean servicePresent )
    {
        this.servicePresent = servicePresent;
    }

    public boolean isServicePresent ()
    {
        return this.servicePresent;
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

}
