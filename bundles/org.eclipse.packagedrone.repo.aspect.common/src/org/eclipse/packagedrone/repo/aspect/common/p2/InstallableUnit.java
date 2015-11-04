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
package org.eclipse.packagedrone.repo.aspect.common.p2;

import static org.eclipse.packagedrone.repo.XmlHelper.addElement;
import static org.eclipse.packagedrone.repo.XmlHelper.fixSize;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.eclipse.packagedrone.repo.XmlHelper;
import org.eclipse.packagedrone.repo.utils.osgi.bundle.BundleInformation;
import org.eclipse.packagedrone.repo.utils.osgi.bundle.BundleInformation.BundleRequirement;
import org.eclipse.packagedrone.repo.utils.osgi.bundle.BundleInformation.PackageExport;
import org.eclipse.packagedrone.repo.utils.osgi.bundle.BundleInformation.PackageImport;
import org.eclipse.packagedrone.repo.utils.osgi.feature.FeatureInformation;
import org.eclipse.packagedrone.repo.utils.osgi.feature.FeatureInformation.FeatureInclude;
import org.eclipse.packagedrone.repo.utils.osgi.feature.FeatureInformation.PluginInclude;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

public class InstallableUnit
{
    private final static Logger logger = LoggerFactory.getLogger ( InstallableUnit.class );

    private static final String DEFAULT_SYSTEM_BUNDLE_ALIAS = "org.eclipse.osgi";

    private String id;

    private Version version;

    private boolean singleton;

    private Map<String, String> properties = new HashMap<> ();

    private Element additionalNodes;

    private String filter;

    private String copyright;

    private String copyrightUrl;

    public static class Artifact
    {
        private final String id;

        private final String classifer;

        private final Version version;

        public Artifact ( final String id, final String classifer, final Version version )
        {
            this.id = id;
            this.classifer = classifer;
            this.version = version;
        }

        public String getId ()
        {
            return this.id;
        }

        public String getClassifer ()
        {
            return this.classifer;
        }

        public Version getVersion ()
        {
            return this.version;
        }
    }

    public static class Key
    {
        private final String namespace;

        private final String key;

        public Key ( final String namespace, final String key )
        {
            this.namespace = namespace;
            this.key = key;
        }

        public String getKey ()
        {
            return this.key;
        }

        public String getNamespace ()
        {
            return this.namespace;
        }

        @Override
        public int hashCode ()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ( this.key == null ? 0 : this.key.hashCode () );
            result = prime * result + ( this.namespace == null ? 0 : this.namespace.hashCode () );
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
            if ( ! ( obj instanceof Key ) )
            {
                return false;
            }
            final Key other = (Key)obj;
            if ( this.key == null )
            {
                if ( other.key != null )
                {
                    return false;
                }
            }
            else if ( !this.key.equals ( other.key ) )
            {
                return false;
            }
            if ( this.namespace == null )
            {
                if ( other.namespace != null )
                {
                    return false;
                }
            }
            else if ( !this.namespace.equals ( other.namespace ) )
            {
                return false;
            }
            return true;
        }
    }

    public static class Requirement
    {
        private final VersionRange range;

        private final boolean optional;

        private final Boolean greedy;

        private final String filter;

        public Requirement ( final VersionRange range, final boolean optional, final Boolean greedy, final String filter )
        {
            this.range = range;
            this.optional = optional;
            this.greedy = greedy;
            this.filter = filter;
        }

        public String getFilter ()
        {
            return this.filter;
        }

        public VersionRange getRange ()
        {
            return this.range;
        }

        public boolean isOptional ()
        {
            return this.optional;
        }

        public Boolean getGreedy ()
        {
            return this.greedy;
        }
    }

    public static class License
    {
        private final String text;

        private final String uri;

        public License ( final String text, final String uri )
        {
            this.text = text;
            this.uri = uri;
        }

        public String getText ()
        {
            return this.text;
        }

        public String getUri ()
        {
            return this.uri;
        }
    }

    private List<License> licenses = new LinkedList<> ();

    private Map<Key, Requirement> requires = new HashMap<> ();

    private Map<Key, String> provides = new HashMap<> ();

    private Set<Artifact> artifacts = new HashSet<> ();

    public void setLicenses ( final List<License> licenses )
    {
        this.licenses = licenses;
    }

    public List<License> getLicenses ()
    {
        return this.licenses;
    }

    public void setCopyright ( final String copyright )
    {
        this.copyright = copyright;
    }

    public void setCopyright ( final String copyright, final String copyrightUrl )
    {
        this.copyright = copyright;
        this.copyrightUrl = copyrightUrl;
    }

    public void setCopyrightUrl ( final String copyrightUrl )
    {
        this.copyrightUrl = copyrightUrl;
    }

    public void setArtifacts ( final Set<Artifact> artifacts )
    {
        this.artifacts = artifacts;
    }

    public Set<Artifact> getArtifacts ()
    {
        return this.artifacts;
    }

    public void setFilter ( final String filter )
    {
        this.filter = filter;
    }

    public String getFilter ()
    {
        return this.filter;
    }

    public void setAdditionalNodes ( final Element additionalNodes )
    {
        this.additionalNodes = additionalNodes;
    }

    public Element getAdditionalNodes ()
    {
        return this.additionalNodes;
    }

    public void setRequires ( final Map<Key, Requirement> requires )
    {
        this.requires = requires;
    }

    public Map<Key, Requirement> getRequires ()
    {
        return this.requires;
    }

    public void setProvides ( final Map<Key, String> provides )
    {
        this.provides = provides;
    }

    public Map<Key, String> getProvides ()
    {
        return this.provides;
    }

    public void setProperties ( final Map<String, String> properties )
    {
        this.properties = properties;
    }

    public Map<String, String> getProperties ()
    {
        return this.properties;
    }

    public void setSingleton ( final boolean singleton )
    {
        this.singleton = singleton;
    }

    public boolean isSingleton ()
    {
        return this.singleton;
    }

    public void setId ( final String id )
    {
        this.id = id;
    }

    public String getId ()
    {
        return this.id;
    }

    public void setVersion ( final Version version )
    {
        this.version = version;
    }

    public Version getVersion ()
    {
        return this.version;
    }

    public static List<InstallableUnit> fromFeature ( final FeatureInformation feature )
    {
        final List<InstallableUnit> result = new ArrayList<> ( 2 );

        result.add ( createFeatureGroup ( feature ) );
        result.add ( createFeatureJar ( feature ) );

        return result;
    }

    private static InstallableUnit createFeatureJar ( final FeatureInformation feature )
    {
        final InstallableUnit result = new InstallableUnit ();

        // core

        result.setId ( feature.getId () + ".feature.jar" );
        result.setVersion ( feature.getVersion () );

        // properties

        final Map<String, String> props = result.getProperties ();
        props.put ( "org.eclipse.equinox.p2.name", feature.getLabel () );
        addProperty ( props, "org.eclipse.equinox.p2.provider", feature.getProvider () );
        addProperty ( props, "org.eclipse.equinox.p2.description", feature.getDescription () );
        addProperty ( props, "org.eclipse.equinox.p2.description.url", feature.getDescriptionUrl () );
        addProperty ( props, "org.eclipse.update.feature.plugin", feature.getPlugin () );

        // localization

        addLocalization ( props, result.getProvides (), feature.getLocalization () );

        // provides

        result.getProvides ().put ( new Key ( "org.eclipse.equinox.p2.iu", feature.getId () + ".feature.jar" ), "" + feature.getVersion () );
        result.getProvides ().put ( new Key ( "org.eclipse.update.feature", feature.getId () ), "" + feature.getVersion () );
        result.getProvides ().put ( new Key ( "org.eclipse.equinox.p2.eclipse.type", "feature" ), "1.0.0" );

        // filter

        result.setFilter ( "(org.eclipse.update.install.features=true)" );

        // artifacts

        result.getArtifacts ().add ( new Artifact ( feature.getId (), "org.eclipse.update.feature", feature.getVersion () ) );

        // legal

        result.setCopyright ( feature.getCopyright (), feature.getCopyrightUrl () );
        result.getLicenses ().add ( new License ( feature.getLicense (), feature.getLicenseUrl () ) );

        // touchpoint

        final XmlHelper xml = new XmlHelper ();
        final Document doc = xml.create ();
        final Element root = doc.createElement ( "root" );
        final Map<String, String> td = new HashMap<> ();
        td.put ( "zipped", "true" );
        addTouchpoint ( root, "org.eclipse.equinox.p2.osgi", "1.0.0", td );
        result.setAdditionalNodes ( root );

        return result;
    }

    private static InstallableUnit createFeatureGroup ( final FeatureInformation feature )
    {
        logger.debug ( "Processing: {}", feature );

        final InstallableUnit result = new InstallableUnit ();

        // version

        final Version version = feature.getVersion ();
        if ( version == null )
        {
            return null;
        }

        // core

        result.setId ( feature.getId () + ".feature.group" );
        result.setVersion ( version );

        // provides

        result.getProvides ().put ( new Key ( "org.eclipse.equinox.p2.iu", feature.getId () + ".feature.group" ), "" + feature.getVersion () );

        // properties

        final Map<String, String> props = result.getProperties ();
        addProperty ( props, "org.eclipse.equinox.p2.name", feature.getLabel () );
        props.put ( "org.eclipse.equinox.p2.type.group", "true" );
        addProperty ( props, "org.eclipse.equinox.p2.provider", feature.getProvider () );
        addProperty ( props, "org.eclipse.equinox.p2.description", feature.getDescription () );
        addProperty ( props, "org.eclipse.equinox.p2.description.url", feature.getDescriptionUrl () );

        // localization

        addLocalization ( props, result.getProvides (), feature.getLocalization () );

        // requirements

        final Map<Key, Requirement> reqs = result.getRequires ();

        final String versionRange = String.format ( "[%1$s,%1$s]", version );
        logger.debug ( "VersionRange: {}", versionRange );

        reqs.put ( new Key ( "org.eclipse.equinox.p2.iu", feature.getId () + ".feature.jar" ), new Requirement ( new VersionRange ( versionRange ), false, null, "(org.eclipse.update.install.features=true)" ) );

        for ( final FeatureInclude fi : feature.getIncludedFeatures () )
        {
            final String filter = fi.getQualifiers ().toFilterString ();
            final VersionRange range = fi.makeVersionRange ();
            reqs.put ( new Key ( "org.eclipse.equinox.p2.iu", fi.getId () + ".feature.group" ), new Requirement ( range, fi.isOptional (), null, filter ) );
        }

        for ( final PluginInclude pi : feature.getIncludedPlugins () )
        {
            final String filter = pi.getQualifiers ().toFilterString ();
            final VersionRange range = pi.makeVersionRange ();
            reqs.put ( new Key ( "org.eclipse.equinox.p2.iu", pi.getId () ), new Requirement ( range, false, null, filter ) );
        }

        for ( final org.eclipse.packagedrone.repo.utils.osgi.feature.FeatureInformation.Requirement ri : feature.getRequirements () )
        {
            final String id;
            switch ( ri.getType () )
            {
                case FEATURE:
                    id = ri.getId () + ".feature.group";
                    break;
                case PLUGIN:
                    id = ri.getId ();
                    break;
                default:
                    throw new IllegalStateException ( String.format ( "Feature requirement type %s is unknown", ri.getType () ) );
            }

            Version v = ri.getVersion ();
            if ( v == null )
            {
                // fall back to empty version
                v = Version.emptyVersion;
            }

            reqs.put ( new Key ( "org.eclipse.equinox.p2.iu", id ), new Requirement ( ri.getMatchRule ().makeRange ( v ), false, null, null ) );
        }

        // filter

        result.setFilter ( feature.getQualifiers ().toFilterString () );

        // legal

        result.setCopyright ( feature.getCopyright (), feature.getCopyrightUrl () );
        result.getLicenses ().add ( new License ( feature.getLicense (), feature.getLicenseUrl () ) );

        // touchpoint

        final XmlHelper xml = new XmlHelper ();
        final Document doc = xml.create ();
        final Element root = doc.createElement ( "root" );
        addTouchpoint ( root, "null", "0.0.0", Collections.emptyMap () );
        result.setAdditionalNodes ( root );

        // return result

        return result;
    }

    public static InstallableUnit fromBundle ( final BundleInformation bundle )
    {
        return fromBundle ( bundle, null );
    }

    public static InstallableUnit fromBundle ( final BundleInformation bundle, final P2MetaDataInformation info )
    {
        final InstallableUnit result = new InstallableUnit ();

        // core

        result.setId ( bundle.getId () );
        result.setVersion ( bundle.getVersion () );
        result.setSingleton ( bundle.isSingleton () );

        // properties

        final Map<String, String> props = result.getProperties ();
        addProperty ( props, "org.eclipse.equinox.p2.name", bundle.getName () );
        addProperty ( props, "org.eclipse.equinox.p2.provider", bundle.getVendor () );
        addProperty ( props, "org.eclipse.equinox.p2.bundle.localization", bundle.getBundleLocalization () );

        // provides

        result.getProvides ().put ( new Key ( "osgi.bundle", bundle.getId () ), "" + bundle.getVersion () );
        result.getProvides ().put ( new Key ( "org.eclipse.equinox.p2.iu", bundle.getId () ), "" + bundle.getVersion () );
        result.getProvides ().put ( new Key ( "org.eclipse.equinox.p2.eclipse.type", "bundle" ), "1.0.0" );

        for ( final PackageExport pe : bundle.getPackageExports () )
        {
            result.getProvides ().put ( new Key ( "java.package", pe.getName () ), makeVersion ( pe.getVersion () ) );
        }

        // localization

        addLocalization ( props, result.getProvides (), bundle.getLocalization () );

        // requirements

        for ( final PackageImport pi : bundle.getPackageImports () )
        {
            result.getRequires ().put ( new Key ( "java.package", pi.getName () ), new Requirement ( pi.getVersionRange (), pi.isOptional (), pi.isOptional () ? false : null, null ) );
        }

        final String systemBundleAlias = getSystemBundleAlias ( info );

        for ( final BundleRequirement br : bundle.getBundleRequirements () )
        {
            result.getRequires ().put ( new Key ( "osgi.bundle", transformBundleName ( systemBundleAlias, br.getId () ) ), new Requirement ( br.getVersionRange (), br.isOptional (), br.isOptional () ? false : null, null ) );
        }

        // artifacts

        result.getArtifacts ().add ( new Artifact ( bundle.getId (), "osgi.bundle", bundle.getVersion () ) );

        // touchpoints

        final XmlHelper xml = new XmlHelper ();
        final Document doc = xml.create ();
        final Element root = doc.createElement ( "root" );

        try
        {
            final Map<String, String> td = new HashMap<String, String> ( 1 );
            td.put ( "manifest", makeManifest ( bundle.getId (), bundle.getVersion () ) );
            addTouchpoint ( root, "org.eclipse.equinox.p2.osgi", "1.0.0", td );
        }
        catch ( final Exception e )
        {
            logger.warn ( "Failed to generate manifest", e );
        }

        result.setAdditionalNodes ( root );

        return result;
    }

    private static String getSystemBundleAlias ( final P2MetaDataInformation info )
    {
        if ( info.getSystemBundleAlias () != null && !info.getSystemBundleAlias ().isEmpty () )
        {
            return info.getSystemBundleAlias ();
        }
        else
        {
            return DEFAULT_SYSTEM_BUNDLE_ALIAS;
        }
    }

    private static String transformBundleName ( final String systemBundleAlias, final String id )
    {
        if ( id.equals ( Constants.SYSTEM_BUNDLE_SYMBOLICNAME ) )
        {
            return systemBundleAlias;
        }
        else
        {
            return id;
        }
    }

    /**
     * Add property entry only of value is not null
     *
     * @param props
     *            properties
     * @param key
     *            to add
     * @param value
     *            to add
     */
    private static void addProperty ( final Map<String, String> props, final String key, final String value )
    {
        if ( value == null )
        {
            return;
        }

        props.put ( key, value );
    }

    private static void addLocalization ( final Map<String, String> properties, final Map<Key, String> provides, final Map<String, Properties> localization )
    {
        for ( final String loc : localization.keySet () )
        {
            provides.put ( new Key ( "org.eclipse.equinox.p2.localization", loc ), "1.0.0" );
        }

        for ( final Map.Entry<String, Properties> le : localization.entrySet () )
        {
            final String locale = le.getKey ();
            for ( final Map.Entry<Object, Object> pe : le.getValue ().entrySet () )
            {
                properties.put ( locale + "." + pe.getKey (), "" + pe.getValue () );
            }
        }
    }

    private static String makeVersion ( final Version version )
    {
        if ( version == null )
        {
            return "0.0.0";
        }
        return version.toString ();
    }

    public static Document toXml ( final List<InstallableUnit> ius )
    {
        final XmlHelper xml = new XmlHelper ();

        final Document doc = xml.create ();
        final Element units = doc.createElement ( "units" );
        doc.appendChild ( units );

        for ( final InstallableUnit iu : ius )
        {
            if ( iu != null )
            {
                iu.writeXmlForUnit ( units );
            }
        }

        return doc;
    }

    public Document toXml ()
    {
        final XmlHelper xml = new XmlHelper ();

        final Document doc = xml.create ();
        final Element units = doc.createElement ( "units" );
        doc.appendChild ( units );

        writeXmlForUnit ( units );

        return doc;
    }

    private void writeXmlForUnit ( final Element units )
    {
        final Document doc = units.getOwnerDocument ();

        final Element unit = addElement ( units, "unit" );
        unit.setAttribute ( "id", this.id );
        unit.setAttribute ( "version", "" + this.version );
        unit.setAttribute ( "singleton", "" + this.singleton );

        final Element update = addElement ( unit, "update" );
        update.setAttribute ( "id", this.id );
        update.setAttribute ( "range", "[0.0.0," + this.version + ")" );
        update.setAttribute ( "severity", "0" );

        final Element properties = addElement ( unit, "properties" );
        for ( final Map.Entry<String, String> entry : this.properties.entrySet () )
        {
            addProperty ( properties, entry.getKey (), entry.getValue () );
        }

        final Element provides = addElement ( unit, "provides" );
        for ( final Map.Entry<Key, String> entry : this.provides.entrySet () )
        {
            addProvided ( provides, entry.getKey ().getNamespace (), entry.getKey ().getKey (), entry.getValue () );
        }

        final Element requires = addElement ( unit, "requires" );
        for ( final Map.Entry<Key, Requirement> entry : this.requires.entrySet () )
        {
            final Element p = addElement ( requires, "required" );
            p.setAttribute ( "namespace", entry.getKey ().getNamespace () );
            p.setAttribute ( "name", entry.getKey ().getKey () );
            p.setAttribute ( "range", makeString ( entry.getValue ().getRange () ) );
            if ( entry.getValue ().isOptional () )
            {
                p.setAttribute ( "optional", "true" );

            }
            if ( entry.getValue ().getGreedy () != null )
            {
                p.setAttribute ( "greedy", "" + entry.getValue ().getGreedy () );
            }
            final String filterString = entry.getValue ().getFilter ();
            if ( filterString != null && !filterString.isEmpty () )
            {
                final Element filter = addElement ( p, "filter" );
                filter.setTextContent ( entry.getValue ().getFilter () );
            }
        }

        if ( this.filter != null && !this.filter.isEmpty () )
        {
            final Element filter = addElement ( unit, "filter" );
            filter.setTextContent ( this.filter );
        }

        final Element artifacts = addElement ( unit, "artifacts" );

        for ( final Artifact artifact : this.artifacts )
        {
            final Element a = addElement ( artifacts, "artifact" );
            a.setAttribute ( "classifier", artifact.getClassifer () );
            a.setAttribute ( "id", artifact.getId () );
            a.setAttribute ( "version", "" + artifact.getVersion () );
        }

        if ( this.additionalNodes != null )
        {
            for ( final Node node : XmlHelper.iter ( this.additionalNodes.getChildNodes () ) )
            {
                if ( node instanceof Element )
                {
                    unit.appendChild ( doc.adoptNode ( node.cloneNode ( true ) ) );
                }
            }
        }

        // add legal

        final Element licenses = addElement ( unit, "licenses" );
        for ( final License licenseEntry : this.licenses )
        {
            final Element license = addElement ( licenses, "license" );
            license.setAttribute ( "url", licenseEntry.getUri () );
            license.setAttribute ( "uri", licenseEntry.getUri () );
            if ( licenseEntry.getText () != null )
            {
                license.appendChild ( doc.createTextNode ( licenseEntry.getText () ) );
            }
        }

        if ( this.copyright != null || this.copyrightUrl != null )
        {
            final Element copyright = addElement ( unit, "copyright" );
            if ( this.copyrightUrl != null )
            {
                copyright.setAttribute ( "url", this.copyrightUrl );
                copyright.setAttribute ( "uri", this.copyrightUrl );
            }
            if ( this.copyright != null )
            {
                copyright.appendChild ( doc.createTextNode ( this.copyright ) );
            }
        }

        fixSize ( licenses );
        fixSize ( requires );
        fixSize ( properties );
        fixSize ( artifacts );
        fixSize ( provides );
        fixSize ( units );
    }

    private String makeString ( final VersionRange range )
    {
        if ( range == null )
        {
            return "0.0.0";
        }
        else
        {
            return range.toString ();
        }
    }

    private static void addProperty ( final Element properties, final String key, final String value )
    {
        final Element p = addElement ( properties, "property" );
        p.setAttribute ( "name", key );
        p.setAttribute ( "value", value );
    }

    private static String makeManifest ( final String id, final Version version ) throws IOException
    {
        final Manifest mf = new Manifest ();
        mf.getMainAttributes ().put ( Attributes.Name.MANIFEST_VERSION, "1.0" );
        mf.getMainAttributes ().putValue ( Constants.BUNDLE_SYMBOLICNAME, id );
        mf.getMainAttributes ().putValue ( Constants.BUNDLE_VERSION, "" + version );

        final ByteArrayOutputStream out = new ByteArrayOutputStream ();
        mf.write ( out );
        out.close ();
        return out.toString ( "UTF-8" );
    }

    private static void addTouchpoint ( final Element unit, final String id, final String version, final Map<String, String> td )
    {
        final Element touchpoint = addElement ( unit, "touchpoint" );
        touchpoint.setAttribute ( "id", id );
        touchpoint.setAttribute ( "version", version );

        final Element touchpointData = addElement ( unit, "touchpointData" );
        final Element is = addElement ( touchpointData, "instructions" );
        for ( final Map.Entry<String, String> entry : td.entrySet () )
        {
            final Element i = addElement ( is, "instruction" );
            i.setAttribute ( "key", entry.getKey () );
            final Text v = i.getOwnerDocument ().createTextNode ( entry.getValue () );
            i.appendChild ( v );
        }

        fixSize ( is );
        fixSize ( touchpointData );
    }

    private static void addProvided ( final Element provides, final String namespace, final String name, final String version )
    {
        final Element p = addElement ( provides, "provided" );
        p.setAttribute ( "namespace", namespace );
        p.setAttribute ( "name", name );
        p.setAttribute ( "version", version );
    }
}
