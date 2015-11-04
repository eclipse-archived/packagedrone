/*******************************************************************************
 * Copyright (c) 2014, 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.utils.osgi.bundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.utils.osgi.ParserHelper;
import org.eclipse.packagedrone.repo.utils.osgi.TranslatedInformation;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;

public class BundleInformation implements TranslatedInformation
{
    public static final MetaKey META_KEY = new MetaKey ( "osgi", "bundle-information" );

    public static class PackageImport
    {
        private final String name;

        private final VersionRange versionRange;

        private final boolean optional;

        public PackageImport ( final String name, final VersionRange versionRange, final boolean optional )
        {
            this.name = name;
            this.versionRange = versionRange;
            this.optional = optional;
        }

        public String getName ()
        {
            return this.name;
        }

        public VersionRange getVersionRange ()
        {
            return this.versionRange;
        }

        public boolean isOptional ()
        {
            return this.optional;
        }

        @Override
        public int hashCode ()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ( this.name == null ? 0 : this.name.hashCode () );
            return result;
        }

        @Override
        public boolean equals ( final Object obj )
        {
            if ( this == obj )
            {
                return true;
            }
            if ( obj == null )
            {
                return false;
            }
            if ( ! ( obj instanceof PackageImport ) )
            {
                return false;
            }
            final PackageImport other = (PackageImport)obj;
            if ( this.name == null )
            {
                if ( other.name != null )
                {
                    return false;
                }
            }
            else if ( !this.name.equals ( other.name ) )
            {
                return false;
            }
            return true;
        }

        @Override
        public String toString ()
        {
            return String.format ( "[Import: %s - %s - %s", this.name, this.versionRange, this.optional ? "optional" : "required" );
        }

    }

    public static class PackageExport
    {
        private final String name;

        private final Version version;

        private final String uses;

        public PackageExport ( final String name, final Version version, final String uses )
        {
            this.name = name;
            this.version = version;
            this.uses = uses;
        }

        public String getName ()
        {
            return this.name;
        }

        public Version getVersion ()
        {
            return this.version;
        }

        public String getUses ()
        {
            return this.uses;
        }

        @Override
        public int hashCode ()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ( this.name == null ? 0 : this.name.hashCode () );
            return result;
        }

        @Override
        public boolean equals ( final Object obj )
        {
            if ( this == obj )
            {
                return true;
            }
            if ( obj == null )
            {
                return false;
            }
            if ( ! ( obj instanceof PackageExport ) )
            {
                return false;
            }
            final PackageExport other = (PackageExport)obj;
            if ( this.name == null )
            {
                if ( other.name != null )
                {
                    return false;
                }
            }
            else if ( !this.name.equals ( other.name ) )
            {
                return false;
            }
            return true;
        }

        @Override
        public String toString ()
        {
            return String.format ( "[Export: %s - %s]", this.name, this.version );
        }

    }

    public static class BundleRequirement
    {
        private final String id;

        private final VersionRange versionRange;

        private final boolean optional;

        private final boolean reexport;

        public BundleRequirement ( final String id, final VersionRange versionRange, final boolean optional, final boolean reexport )
        {
            this.id = id;
            this.versionRange = versionRange;
            this.optional = optional;
            this.reexport = reexport;
        }

        public String getId ()
        {
            return this.id;
        }

        public VersionRange getVersionRange ()
        {
            return this.versionRange;
        }

        public boolean isOptional ()
        {
            return this.optional;
        }

        public boolean isReexport ()
        {
            return this.reexport;
        }

        @Override
        public int hashCode ()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ( this.id == null ? 0 : this.id.hashCode () );
            return result;
        }

        @Override
        public boolean equals ( final Object obj )
        {
            if ( this == obj )
            {
                return true;
            }
            if ( obj == null )
            {
                return false;
            }
            if ( ! ( obj instanceof BundleRequirement ) )
            {
                return false;
            }
            final BundleRequirement other = (BundleRequirement)obj;
            if ( this.id == null )
            {
                if ( other.id != null )
                {
                    return false;
                }
            }
            else if ( !this.id.equals ( other.id ) )
            {
                return false;
            }
            return true;
        }

    }

    private String id;

    private Version version;

    private String name;

    private String vendor;

    private String docUrl;

    private String license;

    private String description;

    private boolean singleton;

    private String bundleLocalization;

    private Map<String, Properties> localization = new HashMap<> ();

    private Set<PackageImport> packageImports = new LinkedHashSet<> ();

    private Set<PackageExport> packageExports = new LinkedHashSet<> ();

    private Set<BundleRequirement> bundleRequirements = new LinkedHashSet<> ();

    private List<String> requiredExecutionEnvironments = new LinkedList<> ();

    private String eclipseBundleShape;

    public void setEclipseBundleShape ( final String eclipseBundleShape )
    {
        this.eclipseBundleShape = eclipseBundleShape;
    }

    public String getEclipseBundleShape ()
    {
        return this.eclipseBundleShape;
    }

    public void setRequiredExecutionEnvironments ( final List<String> requiredExecutionEnvironments )
    {
        if ( requiredExecutionEnvironments != null )
        {
            this.requiredExecutionEnvironments = new ArrayList<> ( requiredExecutionEnvironments );
        }
        else
        {
            this.requiredExecutionEnvironments = new LinkedList<> ();
        }
    }

    public void setDescription ( final String description )
    {
        this.description = description;
    }

    public String getDescription ()
    {
        return this.description;
    }

    public void setLicense ( final String license )
    {
        this.license = license;
    }

    public String getLicense ()
    {
        return this.license;
    }

    public void setDocUrl ( final String docUrl )
    {
        this.docUrl = docUrl;
    }

    public String getDocUrl ()
    {
        return this.docUrl;
    }

    public List<String> getRequiredExecutionEnvironments ()
    {
        return this.requiredExecutionEnvironments;
    }

    public void setBundleLocalization ( final String bundleLocalization )
    {
        this.bundleLocalization = bundleLocalization;
    }

    public String getBundleLocalization ()
    {
        return this.bundleLocalization;
    }

    public void setPackageExports ( final Set<PackageExport> packageExports )
    {
        this.packageExports = packageExports;
    }

    public Set<PackageExport> getPackageExports ()
    {
        return this.packageExports;
    }

    public Set<BundleRequirement> getBundleRequirements ()
    {
        return this.bundleRequirements;
    }

    public void setBundleRequirements ( final Set<BundleRequirement> bundleImports )
    {
        this.bundleRequirements = bundleImports;
    }

    public void setPackageImports ( final Set<PackageImport> packageImports )
    {
        this.packageImports = packageImports;
    }

    public Set<PackageImport> getPackageImports ()
    {
        return this.packageImports;
    }

    public void setSingleton ( final boolean singleton )
    {
        this.singleton = singleton;
    }

    public boolean isSingleton ()
    {
        return this.singleton;
    }

    @Override
    public Map<String, Properties> getLocalization ()
    {
        return this.localization;
    }

    public void setLocalization ( final Map<String, Properties> localization )
    {
        this.localization = localization;
    }

    public void setVendor ( final String vendor )
    {
        this.vendor = vendor;
    }

    public String getVendor ()
    {
        return this.vendor;
    }

    public String getName ()
    {
        return this.name;
    }

    public void setName ( final String name )
    {
        this.name = name;
    }

    public Version getVersion ()
    {
        return this.version;
    }

    public void setVersion ( final Version version )
    {
        this.version = version;
    }

    public void setId ( final String id )
    {
        this.id = id;
    }

    public String getId ()
    {
        return this.id;
    }

    public static BundleInformation fromJson ( final String string )
    {
        return fromJson ( string, BundleInformation.class );
    }

    public static <T extends BundleInformation> T fromJson ( final String string, final Class<T> clazz )
    {
        if ( string == null )
        {
            return null;
        }
        return ParserHelper.newGson ().fromJson ( string, clazz );
    }

    public String toJson ()
    {
        return ParserHelper.newGson ().toJson ( this );
    }
}
