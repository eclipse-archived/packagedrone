/*******************************************************************************
 * Copyright (c) 2014, 2016 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.generator.p2;

import javax.validation.constraints.Pattern;

import org.eclipse.packagedrone.repo.MetaKeyBinding;
import org.eclipse.packagedrone.utils.validation.constraints.VersionString;
import org.hibernate.validator.constraints.URL;

public class FeatureData
{
    @Pattern ( regexp = "[a-z0-9]+(\\.[a-z0-9]+)*", message = "Must be a valid feature ID" )
    @MetaKeyBinding ( namespace = FeatureGenerator.ID, key = "id" )
    private String id;

    @VersionString
    @MetaKeyBinding ( namespace = FeatureGenerator.ID, key = "version" )
    private String version;

    @MetaKeyBinding ( namespace = FeatureGenerator.ID, key = "provider" )
    private String provider;

    @MetaKeyBinding ( namespace = FeatureGenerator.ID, key = "label" )
    private String label;

    @MetaKeyBinding ( namespace = FeatureGenerator.ID, key = "description" )
    private String description;

    @MetaKeyBinding ( namespace = FeatureGenerator.ID, key = "descriptionUrl" )
    @URL
    private String descriptionUrl;

    @MetaKeyBinding ( namespace = FeatureGenerator.ID, key = "copyright" )
    private String copyright;

    @MetaKeyBinding ( namespace = FeatureGenerator.ID, key = "copyrightUrl" )
    @URL
    private String copyrightUrl;

    @MetaKeyBinding ( namespace = FeatureGenerator.ID, key = "license" )
    private String license;

    @MetaKeyBinding ( namespace = FeatureGenerator.ID, key = "licenseUrl" )
    @URL
    private String licenseUrl;

    @MetaKeyBinding ( namespace = FeatureGenerator.ID, key = "artifactFilter" )
    private String symbolicNamePattern;

    public void setId ( final String id )
    {
        this.id = id;
    }

    public String getId ()
    {
        return this.id;
    }

    public void setVersion ( final String version )
    {
        this.version = version;
    }

    public String getVersion ()
    {
        return this.version;
    }

    public String getDescription ()
    {
        return this.description;
    }

    public void setDescription ( final String description )
    {
        this.description = description;
    }

    public String getProvider ()
    {
        return this.provider;
    }

    public void setProvider ( final String provider )
    {
        this.provider = provider;
    }

    public String getLabel ()
    {
        return this.label;
    }

    public void setLabel ( final String label )
    {
        this.label = label;
    }

    public String getDescriptionUrl ()
    {
        return this.descriptionUrl;
    }

    public void setDescriptionUrl ( final String descriptionUrl )
    {
        this.descriptionUrl = descriptionUrl;
    }

    public String getCopyright ()
    {
        return this.copyright;
    }

    public void setCopyright ( final String copyright )
    {
        this.copyright = copyright;
    }

    public String getCopyrightUrl ()
    {
        return this.copyrightUrl;
    }

    public void setCopyrightUrl ( final String copyrightUrl )
    {
        this.copyrightUrl = copyrightUrl;
    }

    public String getLicense ()
    {
        return this.license;
    }

    public void setLicense ( final String license )
    {
        this.license = license;
    }

    public String getLicenseUrl ()
    {
        return this.licenseUrl;
    }

    public void setLicenseUrl ( final String licenseUrl )
    {
        this.licenseUrl = licenseUrl;
    }

    public void setSymbolicNamePattern ( final String artifactFilter )
    {
        this.symbolicNamePattern = artifactFilter;
    }

    public String getSymbolicNamePattern ()
    {
        return this.symbolicNamePattern;
    }
}
