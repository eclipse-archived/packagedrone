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
package org.eclipse.packagedrone.repo.adapter.deb;

import java.util.HashSet;
import java.util.Set;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.eclipse.packagedrone.repo.MetaKeyBinding;
import org.hibernate.validator.constraints.NotBlank;

public class ChannelConfiguration
{
    @MetaKeyBinding ( namespace = "apt", key = "origin" )
    @Pattern ( regexp = "[\\p{Alnum}\\p{Space}]*" )
    private String origin;

    @MetaKeyBinding ( namespace = "apt", key = "label" )
    private String label;

    @MetaKeyBinding ( namespace = "apt", key = "suite" )
    @Pattern ( regexp = "\\p{Alnum}*" )
    private String suite;

    @MetaKeyBinding ( namespace = "apt", key = "version" )
    @Pattern ( regexp = "[\\p{Alnum}\\.]*" )
    private String version;

    @MetaKeyBinding ( namespace = "apt", key = "codename" )
    @Pattern ( regexp = "\\p{Alnum}*" )
    private String codename;

    @MetaKeyBinding ( namespace = "apt", key = "description" )
    @Pattern ( regexp = "[^\\n\\r]*" )
    private String description;

    @MetaKeyBinding ( namespace = "apt", key = "architectures", converterClass = SpaceJoiner.class )
    @Size ( min = 1 )
    private Set<String> architectures = new HashSet<> ();

    @MetaKeyBinding ( namespace = "apt", key = "distribution" )
    @Pattern ( regexp = "\\p{Alnum}*" )
    @NotBlank
    private String distribution;

    @MetaKeyBinding ( namespace = "apt", key = "signingService" )
    private String signingService;

    @MetaKeyBinding ( namespace = "apt", key = "defaultComponent" )
    @NotBlank
    @Pattern ( regexp = "\\p{Alnum}*" )
    private String defaultComponent;

    public void setDefaultComponent ( final String defaultComponent )
    {
        this.defaultComponent = defaultComponent;
    }

    public String getDefaultComponent ()
    {
        return this.defaultComponent;
    }

    public void setSigningService ( final String signingService )
    {
        this.signingService = signingService;
    }

    public String getSigningService ()
    {
        return this.signingService;
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

    public String getLabel ( final String defaultValue )
    {
        if ( this.label == null )
        {
            return defaultValue;
        }
        return this.label;
    }

    public void setLabel ( final String label )
    {
        this.label = label;
    }

    public String getSuite ()
    {
        return this.suite;
    }

    public void setSuite ( final String suite )
    {
        this.suite = suite;
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

    public Set<String> getArchitectures ()
    {
        return this.architectures;
    }

    public void setArchitectures ( final Set<String> architectures )
    {
        this.architectures = architectures;
    }

    public void setDistribution ( final String distribution )
    {
        this.distribution = distribution;
    }

    public String getDistribution ()
    {
        return this.distribution;
    }

    public boolean isValid ()
    {
        if ( this.defaultComponent == null || this.defaultComponent.isEmpty () )
        {
            return false;
        }
        if ( this.distribution == null || this.distribution.isEmpty () )
        {
            return false;
        }
        return true;
    }
}
