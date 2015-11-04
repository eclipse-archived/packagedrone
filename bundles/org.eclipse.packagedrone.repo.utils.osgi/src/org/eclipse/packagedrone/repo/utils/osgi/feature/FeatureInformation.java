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
package org.eclipse.packagedrone.repo.utils.osgi.feature;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.utils.osgi.ParserHelper;
import org.eclipse.packagedrone.repo.utils.osgi.TranslatedInformation;
import org.eclipse.packagedrone.utils.Filters.Multi;
import org.eclipse.packagedrone.utils.Filters.Pair;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;
import org.w3c.dom.Element;

public class FeatureInformation implements TranslatedInformation
{
    public static final MetaKey META_KEY = new MetaKey ( "osgi", "feature-information" );

    private String id;

    private Version version;

    private String label;

    private String provider;

    private String plugin;

    private Map<String, Properties> localization = new HashMap<> ();

    private String description;

    private String descriptionUrl;

    private String copyright;

    private String copyrightUrl;

    private String license;

    private String licenseUrl;

    public static class Qualifiers
    {
        private Set<String> operatingSystems = new TreeSet<> ();

        private Set<String> windowSystems = new TreeSet<> ();

        private Set<String> architectures = new TreeSet<> ();

        private Set<String> languages = new TreeSet<> ();

        public Set<String> getOperatingSystems ()
        {
            return this.operatingSystems;
        }

        public void setOperatingSystems ( final Set<String> operatingSystems )
        {
            this.operatingSystems = operatingSystems;
        }

        public Set<String> getWindowSystems ()
        {
            return this.windowSystems;
        }

        public void setWindowSystems ( final Set<String> windowSystems )
        {
            this.windowSystems = windowSystems;
        }

        public Set<String> getArchitectures ()
        {
            return this.architectures;
        }

        public void setArchitectures ( final Set<String> architectures )
        {
            this.architectures = architectures;
        }

        public Set<String> getLanguages ()
        {
            return this.languages;
        }

        public void setLanguages ( final Set<String> languages )
        {
            this.languages = languages;
        }

        public static Qualifiers parse ( final Element ele )
        {
            final String os = ele.getAttribute ( "os" );
            final String ws = ele.getAttribute ( "ws" );
            final String arch = ele.getAttribute ( "arch" );
            final String nl = ele.getAttribute ( "nl" );

            final Qualifiers q = new Qualifiers ();

            q.getOperatingSystems ().addAll ( makeList ( os ) );
            q.getWindowSystems ().addAll ( makeList ( ws ) );
            q.getArchitectures ().addAll ( makeList ( arch ) );
            q.getLanguages ().addAll ( makeList ( nl ) );

            return q;
        }

        private static List<String> makeList ( final String string )
        {
            if ( string == null || string.isEmpty () )
            {
                return Collections.emptyList ();
            }

            return Arrays.asList ( string.split ( "\\s*,\\s*" ) );
        }

        public String toFilterString ()
        {
            if ( isEmpty () )
            {
                return null;
            }

            final Multi and = new Multi ( "&" );

            and.addNode ( expand ( "|", "osgi.os", this.operatingSystems ) );
            and.addNode ( expand ( "|", "osgi.ws", this.windowSystems ) );
            and.addNode ( expand ( "|", "osgi.arch", this.architectures ) );
            and.addNode ( expand ( "|", "osgi.nl", this.languages ) );

            return and.toString ();
        }

        private Multi expand ( final String oper, final String key, final Set<String> values )
        {
            final Multi m = new Multi ( "|" );

            for ( final String v : values )
            {
                m.addNode ( new Pair ( key, v ) );
            }

            return m;
        }

        public boolean isEmpty ()
        {
            return this.operatingSystems.isEmpty () && this.windowSystems.isEmpty () && this.architectures.isEmpty () && this.languages.isEmpty ();
        }
    }

    public static class PluginInclude implements Comparable<PluginInclude>
    {
        private String id;

        private Version version;

        private boolean unpack;

        private Qualifiers qualifiers;

        public PluginInclude ( final String id, final Version version, final boolean unpack, final Qualifiers qualifiers )
        {
            this.id = id;
            this.version = version;
            this.unpack = unpack;
            this.qualifiers = qualifiers;
        }

        public String getId ()
        {
            return this.id;
        }

        public void setId ( final String id )
        {
            this.id = id;
        }

        public Version getVersion ()
        {
            return this.version;
        }

        public void setVersion ( final Version version )
        {
            this.version = version;
        }

        public boolean isUnpack ()
        {
            return this.unpack;
        }

        public void setUnpack ( final boolean unpack )
        {
            this.unpack = unpack;
        }

        public Qualifiers getQualifiers ()
        {
            return this.qualifiers;
        }

        public void setQualifiers ( final Qualifiers qualifiers )
        {
            this.qualifiers = qualifiers;
        }

        public VersionRange makeVersionRange ()
        {
            if ( this.version == null )
            {
                return new VersionRange ( "0.0.0" );
            }
            else
            {
                return new VersionRange ( VersionRange.LEFT_CLOSED, this.version, this.version, VersionRange.RIGHT_CLOSED );
            }
        }

        @Override
        public int compareTo ( final PluginInclude o )
        {
            int rc;

            rc = this.id.compareTo ( o.id );
            if ( rc != 0 )
            {
                return rc;
            }

            return this.version.compareTo ( o.version );

        }

        @Override
        public int hashCode ()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ( this.id == null ? 0 : this.id.hashCode () );
            result = prime * result + ( this.version == null ? 0 : this.version.hashCode () );
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
            if ( ! ( obj instanceof PluginInclude ) )
            {
                return false;
            }
            final PluginInclude other = (PluginInclude)obj;
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
            if ( this.version == null )
            {
                if ( other.version != null )
                {
                    return false;
                }
            }
            else if ( !this.version.equals ( other.version ) )
            {
                return false;
            }
            return true;
        }

    }

    public static class FeatureInclude implements Comparable<FeatureInclude>
    {
        private final String id;

        private final Version version;

        private final String name;

        private final boolean optional;

        private final Qualifiers qualifiers;

        public FeatureInclude ( final String id, final Version version, final String name, final boolean optional, final Qualifiers qualifiers )
        {
            this.id = id;
            this.version = version;
            this.name = name;
            this.optional = optional;
            this.qualifiers = qualifiers;
        }

        public String getId ()
        {
            return this.id;
        }

        public Version getVersion ()
        {
            return this.version;
        }

        public String getName ()
        {
            return this.name;
        }

        public boolean isOptional ()
        {
            return this.optional;
        }

        public Qualifiers getQualifiers ()
        {
            return this.qualifiers;
        }

        public VersionRange makeVersionRange ()
        {
            if ( this.version == null )
            {
                return new VersionRange ( "0.0.0" );
            }
            else
            {
                return new VersionRange ( VersionRange.LEFT_CLOSED, this.version, this.version, VersionRange.RIGHT_CLOSED );
            }
        }

        @Override
        public int compareTo ( final FeatureInclude o )
        {
            int rc;

            rc = this.id.compareTo ( o.id );
            if ( rc != 0 )
            {
                return rc;
            }

            return this.version.compareTo ( o.version );
        }

        @Override
        public int hashCode ()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ( this.id == null ? 0 : this.id.hashCode () );
            result = prime * result + ( this.version == null ? 0 : this.version.hashCode () );
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
            if ( ! ( obj instanceof FeatureInclude ) )
            {
                return false;
            }
            final FeatureInclude other = (FeatureInclude)obj;
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
            if ( this.version == null )
            {
                if ( other.version != null )
                {
                    return false;
                }
            }
            else if ( !this.version.equals ( other.version ) )
            {
                return false;
            }
            return true;
        }
    }

    public static class Requirement implements Comparable<Requirement>
    {
        public static enum Type
        {
            FEATURE,
            PLUGIN
        }

        public static enum MatchRule
        {
            DEFAULT ( "default" )
            {
                @Override
                public VersionRange makeRange ( final Version version )
                {
                    return new VersionRange ( "0.0.0" );
                }
            },
            EQUIVALENT ( "equivalent" )
            {
                @Override
                public VersionRange makeRange ( final Version version )
                {
                    final Version endVersion = new Version ( version.getMajor (), version.getMinor () + 1, 0 );
                    return new VersionRange ( VersionRange.LEFT_CLOSED, version, endVersion, VersionRange.RIGHT_OPEN );
                }
            },
            COMPATIBLE ( "compatible" )
            {
                @Override
                public VersionRange makeRange ( final Version version )
                {
                    final Version endVersion = new Version ( version.getMajor () + 1, 0, 0 );
                    return new VersionRange ( VersionRange.LEFT_CLOSED, version, endVersion, VersionRange.RIGHT_OPEN );
                }
            },
            PERFECT ( "perfect" )
            {
                @Override
                public VersionRange makeRange ( final Version version )
                {
                    return new VersionRange ( VersionRange.LEFT_CLOSED, version, version, VersionRange.RIGHT_CLOSED );
                }
            },
            GREATER_OR_EQUAL ( "greaterOrEqual" )
            {
                @Override
                public VersionRange makeRange ( final Version version )
                {
                    return new VersionRange ( version.toString () );
                }
            };

            private String id;

            private MatchRule ( final String id )
            {
                this.id = id;
            }

            public String getId ()
            {
                return this.id;
            }

            public static MatchRule findById ( final String id )
            {
                for ( final MatchRule mr : values () )
                {
                    if ( mr.getId ().equals ( id ) )
                    {
                        return mr;
                    }
                }
                return null;
            }

            public abstract VersionRange makeRange ( Version version );
        }

        private final Type type;

        private final String id;

        private final Version version;

        private final MatchRule matchRule;

        public Requirement ( final Type type, final String id, final Version version, final MatchRule matchRule )
        {
            this.type = type;
            this.id = id;
            this.version = version;
            this.matchRule = matchRule;
        }

        public Type getType ()
        {
            return this.type;
        }

        public String getId ()
        {
            return this.id;
        }

        public Version getVersion ()
        {
            return this.version;
        }

        public MatchRule getMatchRule ()
        {
            return this.matchRule;
        }

        @Override
        public int compareTo ( final Requirement o )
        {
            int rc;

            rc = this.id.compareTo ( o.id );

            return rc;
        }
    }

    private Qualifiers qualifiers;

    private Set<Requirement> requirements = new HashSet<> ();

    private Set<FeatureInclude> includedFeatures = new HashSet<> ();

    private Set<PluginInclude> includedPlugins = new HashSet<> ();

    public void setQualifiers ( final Qualifiers qualifiers )
    {
        this.qualifiers = qualifiers;
    }

    public Qualifiers getQualifiers ()
    {
        return this.qualifiers;
    }

    public void setIncludedPlugins ( final Set<PluginInclude> includedPlugins )
    {
        this.includedPlugins = includedPlugins;
    }

    public Set<PluginInclude> getIncludedPlugins ()
    {
        return this.includedPlugins;
    }

    public void setRequirements ( final Set<Requirement> requirements )
    {
        this.requirements = requirements;
    }

    public Set<Requirement> getRequirements ()
    {
        return this.requirements;
    }

    public void setIncludedFeatures ( final Set<FeatureInclude> includedFeatures )
    {
        this.includedFeatures = includedFeatures;
    }

    public Set<FeatureInclude> getIncludedFeatures ()
    {
        return this.includedFeatures;
    }

    public void setLocalization ( final Map<String, Properties> localization )
    {
        this.localization = localization;
    }

    @Override
    public Map<String, Properties> getLocalization ()
    {
        return this.localization;
    }

    public void setPlugin ( final String plugin )
    {
        this.plugin = plugin;
    }

    public String getPlugin ()
    {
        return this.plugin;
    }

    public void setId ( final String id )
    {
        this.id = id;
    }

    public String getId ()
    {
        return this.id;
    }

    public Version getVersion ()
    {
        return this.version;
    }

    public void setVersion ( final Version version )
    {
        this.version = version;
    }

    public String getLabel ()
    {
        return this.label;
    }

    public void setLabel ( final String label )
    {
        this.label = label;
    }

    public String getProvider ()
    {
        return this.provider;
    }

    public void setProvider ( final String provider )
    {
        this.provider = provider;
    }

    public String getDescription ()
    {
        return this.description;
    }

    public void setDescription ( final String description )
    {
        this.description = description;
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

    @Override
    public String toString ()
    {
        return String.format ( "[Feature: %s]", this.id );
    }

    public static FeatureInformation fromJson ( final String string )
    {
        return fromJson ( string, FeatureInformation.class );
    }

    public static <T extends FeatureInformation> T fromJson ( final String string, final Class<T> clazz )
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
