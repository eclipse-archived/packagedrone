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
package org.eclipse.packagedrone.repo.adapter.deb.aspect;

import java.util.SortedSet;
import java.util.TreeSet;

public class DistributionInformation
{
    private String origin;

    private String label;

    private String version;

    private String suite;

    private String codename;

    private String description;

    private SortedSet<String> components = new TreeSet<> ();

    private SortedSet<String> architectures = new TreeSet<> ();

    public void setSuite ( final String suite )
    {
        this.suite = suite;
    }

    public String getSuite ()
    {
        return this.suite;
    }

    public String getOrigin ()
    {
        return this.origin;
    }

    public void setOrigin ( final String origin )
    {
        this.origin = origin;
    }

    public String getLabel ()
    {
        return this.label;
    }

    public void setLabel ( final String label )
    {
        this.label = label;
    }

    public String getVersion ()
    {
        return this.version;
    }

    public void setVersion ( final String version )
    {
        this.version = version;
    }

    public String getCodename ()
    {
        return this.codename;
    }

    public void setCodename ( final String codename )
    {
        this.codename = codename;
    }

    public String getDescription ()
    {
        return this.description;
    }

    public void setDescription ( final String description )
    {
        this.description = description;
    }

    public void setArchitectures ( final SortedSet<String> architectures )
    {
        this.architectures = architectures;
    }

    public SortedSet<String> getArchitectures ()
    {
        return this.architectures;
    }

    public void setComponents ( final SortedSet<String> components )
    {
        this.components = components;
    }

    public SortedSet<String> getComponents ()
    {
        return this.components;
    }
}
