/*******************************************************************************
 * Copyright (c) 2014, 2016 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *     M-Ezzat - code cleanup - squid:S2131
 *******************************************************************************/
package org.eclipse.packagedrone.repo.aspect.common.p2;

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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

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

public class InstallableUnit
{
    private final static Logger logger = LoggerFactory.getLogger ( InstallableUnit.class );

    private static final String DEFAULT_SYSTEM_BUNDLE_ALIAS = "org.eclipse.osgi";

    private String id;

    private Version version;

    private boolean singleton;

    private Map<String, String> properties = new HashMap<> ();

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

    public static class Entry<T>
    {
        private final String namespace;

        private final String key;

        private final T value;

        public Entry ( final String namespace, final String key, final T value )
        {
            this.namespace = namespace;
            this.key = key;
            this.value = value;
        }

        public String getKey ()
        {
            return this.key;
        }

        public String getNamespace ()
        {
            return this.namespace;
        }

        public T getValue ()
        {
            return this.value;
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

    public static class Touchpoint
    {
        private final String id;

        private final String version;

        private final Map<String, String> instructions;

        public Touchpoint ( final String id, final String version, final Map<String, String> instructions )
        {
            this.id = id;
            this.version = version;
            this.instructions = instructions;
        }

        public String getId ()
        {
            return this.id;
        }

        public String getVersion ()
        {
            return this.version;
        }

        public Map<String, String> getInstructions ()
        {
            return Collections.unmodifiableMap ( this.instructions );
        }
    }

    private List<License> licenses = new LinkedList<> ();

    private List<Entry<Requirement>> requires = new LinkedList<> ();

    private List<Entry<String>> provides = new LinkedList<> ();

    private Set<Artifact> artifacts = new HashSet<> ();

    private List<Touchpoint> touchpoints = new LinkedList<> ();

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

    public void setTouchpoints ( final List<Touchpoint> touchpoints )
    {
        this.touchpoints = touchpoints;
    }

    public List<Touchpoint> getTouchpoints ()
    {
        return this.touchpoints;
    }

    public void setFilter ( final String filter )
    {
        this.filter = filter;
    }

    public String getFilter ()
    {
        return this.filter;
    }

    public void setRequires ( final List<Entry<Requirement>> requires )
    {
        this.requires = requires;
    }

    public List<Entry<Requirement>> getRequires ()
    {
        return this.requires;
    }

    public void setProvides ( final List<Entry<String>> provides )
    {
        this.provides = provides;
    }

    public List<Entry<String>> getProvides ()
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

        addProvides ( result, "org.eclipse.equinox.p2.iu", feature.getId () + ".feature.jar", "" + feature.getVersion () );
        addProvides ( result, "org.eclipse.update.feature", feature.getId (), "" + feature.getVersion () );
        addProvides ( result, "org.eclipse.equinox.p2.eclipse.type", "feature", "1.0.0" );

        // filter

        result.setFilter ( "(org.eclipse.update.install.features=true)" );

        // artifacts

        result.getArtifacts ().add ( new Artifact ( feature.getId (), "org.eclipse.update.feature", feature.getVersion () ) );

        // legal

        result.setCopyright ( feature.getCopyright (), feature.getCopyrightUrl () );
        result.getLicenses ().add ( new License ( feature.getLicense (), feature.getLicenseUrl () ) );

        // touchpoint

        final Map<String, String> td = new HashMap<> ();
        td.put ( "zipped", "true" );
        result.getTouchpoints ().add ( new Touchpoint ( "org.eclipse.equinox.p2.osgi", "1.0.0", td ) );

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

        addProvides ( result, "org.eclipse.equinox.p2.iu", feature.getId () + ".feature.group", "" + feature.getVersion () );

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

        final List<Entry<Requirement>> reqs = result.getRequires ();

        final String versionRange = String.format ( "[%1$s,%1$s]", version );
        logger.debug ( "VersionRange: {}", versionRange );

        addRequires ( reqs, "org.eclipse.equinox.p2.iu", feature.getId () + ".feature.jar", new Requirement ( new VersionRange ( versionRange ), false, null, "(org.eclipse.update.install.features=true)" ) );

        for ( final FeatureInclude fi : feature.getIncludedFeatures () )
        {
            final String filter = fi.getQualifiers ().toFilterString ();
            final VersionRange range = fi.makeVersionRange ();
            addRequires ( reqs, "org.eclipse.equinox.p2.iu", fi.getId () + ".feature.group", new Requirement ( range, fi.isOptional (), null, filter ) );
        }

        for ( final PluginInclude pi : feature.getIncludedPlugins () )
        {
            final String filter = pi.getQualifiers ().toFilterString ();
            final VersionRange range = pi.makeVersionRange ();
            addRequires ( reqs, "org.eclipse.equinox.p2.iu", pi.getId (), new Requirement ( range, false, null, filter ) );
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

            addRequires ( reqs, "org.eclipse.equinox.p2.iu", id, new Requirement ( ri.getMatchRule ().makeRange ( v ), false, null, null ) );
        }

        // filter

        result.setFilter ( feature.getQualifiers ().toFilterString () );

        // legal

        result.setCopyright ( feature.getCopyright (), feature.getCopyrightUrl () );
        result.getLicenses ().add ( new License ( feature.getLicense (), feature.getLicenseUrl () ) );

        // touchpoint - no touchpoints

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

        addProvides ( result, "osgi.bundle", bundle.getId (), "" + bundle.getVersion () );
        addProvides ( result, "org.eclipse.equinox.p2.iu", bundle.getId (), "" + bundle.getVersion () );
        addProvides ( result, "org.eclipse.equinox.p2.eclipse.type", "bundle", "1.0.0" );

        for ( final PackageExport pe : bundle.getPackageExports () )
        {
            addProvides ( result, "java.package", pe.getName (), makeVersion ( pe.getVersion () ) );
        }

        // localization

        addLocalization ( props, result.getProvides (), bundle.getLocalization () );

        // requirements

        for ( final PackageImport pi : bundle.getPackageImports () )
        {
            addRequires ( result, "java.package", pi.getName (), new Requirement ( pi.getVersionRange (), pi.isOptional (), pi.isOptional () ? false : null, null ) );
        }

        final String systemBundleAlias = getSystemBundleAlias ( info );

        for ( final BundleRequirement br : bundle.getBundleRequirements () )
        {
            addRequires ( result, "osgi.bundle", transformBundleName ( systemBundleAlias, br.getId () ), new Requirement ( br.getVersionRange (), br.isOptional (), br.isOptional () ? false : null, null ) );
        }

        // artifacts

        result.getArtifacts ().add ( new Artifact ( bundle.getId (), "osgi.bundle", bundle.getVersion () ) );

        // touchpoints

        try
        {
            final Map<String, String> td = new HashMap<String, String> ( 1 );
            td.put ( "manifest", makeManifest ( bundle.getId (), bundle.getVersion () ) );
            result.getTouchpoints ().add ( new Touchpoint ( "org.eclipse.equinox.p2.osgi", "1.0.0", td ) );
        }
        catch ( final Exception e )
        {
            logger.warn ( "Failed to generate manifest", e );
        }

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

    private static void addProvides ( final InstallableUnit result, final String namespace, final String key, final String value )
    {
        addProvides ( result.getProvides (), namespace, key, value );
    }

    private static void addProvides ( final List<Entry<String>> provides, final String namespace, final String key, final String value )
    {
        provides.add ( new Entry<> ( namespace, key, value ) );
    }

    private static void addRequires ( final InstallableUnit result, final String namespace, final String key, final Requirement value )
    {
        addRequires ( result.getRequires (), namespace, key, value );
    }

    private static void addRequires ( final List<Entry<Requirement>> requires, final String namespace, final String key, final Requirement value )
    {
        requires.add ( new Entry<> ( namespace, key, value ) );
    }

    private static void addLocalization ( final Map<String, String> properties, final List<Entry<String>> provides, final Map<String, Properties> localization )
    {
        for ( final String loc : localization.keySet () )
        {
            addProvides ( provides, "org.eclipse.equinox.p2.localization", loc, "1.0.0" );
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

    /**
     * Write a list of units inside a {@code units} element
     *
     * @param xsw
     *            the stream to write to
     * @param ius
     *            The units to write
     * @throws XMLStreamException
     *             if the underlying throws it
     */
    public static void writeXml ( final XMLStreamWriter xsw, final List<InstallableUnit> ius ) throws XMLStreamException
    {
        xsw.writeStartElement ( "units" );
        xsw.writeAttribute ( "size", Integer.toString(ius.size ()) );
        for ( final InstallableUnit iu : ius )
        {
            iu.writeXmlForUnit ( xsw );
        }
        xsw.writeEndElement ();
    }

    /**
     * Write a single unit inside a {@code units} element
     *
     * @param xsw
     *            the stream to write to
     * @throws XMLStreamException
     *             if the underlying throws it
     */
    public void writeXml ( final XMLStreamWriter xsw ) throws XMLStreamException
    {
        writeXml ( xsw, Collections.singletonList ( this ) );
    }

    /**
     * Write the unit as XML fragment
     *
     * @param xsw
     *            the XMLStreamWriter to write to
     * @throws XMLStreamException
     *             if the underlying throws it
     */
    public void writeXmlForUnit ( final XMLStreamWriter xsw ) throws XMLStreamException
    {
        xsw.writeStartElement ( "unit" );
        xsw.writeAttribute ( "id", this.id );
        xsw.writeAttribute ( "version", "" + this.version );
        xsw.writeAttribute ( "singleton", Boolean.toString(this.singleton) );

        {
            xsw.writeEmptyElement ( "update" );
            xsw.writeAttribute ( "id", this.id );
            xsw.writeAttribute ( "range", "[0.0.0," + this.version + ")" );
            xsw.writeAttribute ( "severity", "0" );
        }

        {
            xsw.writeStartElement ( "properties" );
            xsw.writeAttribute ( "size", Integer.toString(this.properties.size ()) );

            for ( final Map.Entry<String, String> entry : this.properties.entrySet () )
            {
                xsw.writeStartElement ( "property" );
                xsw.writeAttribute ( "name", entry.getKey () );
                if ( entry.getValue () != null )
                {
                    xsw.writeAttribute ( "value", entry.getValue () );
                }
                xsw.writeEndElement ();
            }

            xsw.writeEndElement (); // properties
        }

        {
            xsw.writeStartElement ( "provides" );
            xsw.writeAttribute ( "size", Integer.toString(this.provides.size ()) );

            for ( final Entry<String> entry : this.provides )
            {
                xsw.writeStartElement ( "provided" );
                xsw.writeAttribute ( "namespace", entry.getNamespace () );
                xsw.writeAttribute ( "name", entry.getKey () );
                xsw.writeAttribute ( "version", entry.getValue () );
                xsw.writeEndElement ();
            }

            xsw.writeEndElement (); // provides
        }

        {
            xsw.writeStartElement ( "requires" );
            xsw.writeAttribute ( "size", Integer.toString(this.requires.size ()) );

            for ( final Entry<Requirement> entry : this.requires )
            {
                xsw.writeStartElement ( "required" );

                xsw.writeAttribute ( "namespace", entry.getNamespace () );
                xsw.writeAttribute ( "name", entry.getKey () );
                xsw.writeAttribute ( "range", makeString ( entry.getValue ().getRange () ) );

                if ( entry.getValue ().isOptional () )
                {
                    xsw.writeAttribute ( "optional", "true" );

                }
                if ( entry.getValue ().getGreedy () != null )
                {
                    xsw.writeAttribute ( "greedy", "" + entry.getValue ().getGreedy () );
                }

                final String filterString = entry.getValue ().getFilter ();
                if ( filterString != null && !filterString.isEmpty () )
                {
                    xsw.writeStartElement ( "filter" );
                    xsw.writeCharacters ( entry.getValue ().getFilter () );
                    xsw.writeEndElement (); // filter
                }

                xsw.writeEndElement (); // required
            }

            xsw.writeEndElement (); // requires
        }

        {
            if ( this.filter != null && !this.filter.isEmpty () )
            {
                xsw.writeStartElement ( "filter" );
                xsw.writeCharacters ( this.filter );
                xsw.writeEndElement (); // filter
            }
        }

        if ( !this.artifacts.isEmpty () )
        {
            xsw.writeStartElement ( "artifacts" );
            xsw.writeAttribute ( "size", Integer.toString(this.artifacts.size ()) );

            for ( final Artifact artifact : this.artifacts )
            {
                xsw.writeEmptyElement ( "artifact" );
                xsw.writeAttribute ( "classifier", artifact.getClassifer () );
                xsw.writeAttribute ( "id", artifact.getId () );
                xsw.writeAttribute ( "version", "" + artifact.getVersion () );
            }

            xsw.writeEndElement (); // artifacts
        }

        {
            if ( this.touchpoints.isEmpty () )
            {
                xsw.writeEmptyElement ( "touchpoint" );
                xsw.writeAttribute ( "id", "null" );
                xsw.writeAttribute ( "version", "0.0.0" );
            }
            else
            {
                for ( final Touchpoint tp : this.touchpoints )
                {
                    xsw.writeEmptyElement ( "touchpoint" );
                    xsw.writeAttribute ( "id", tp.getId () );
                    xsw.writeAttribute ( "version", tp.getVersion () );

                    if ( !tp.getInstructions ().isEmpty () )
                    {
                        xsw.writeStartElement ( "touchpointData" );
                        xsw.writeAttribute ( "size", "1" );

                        xsw.writeStartElement ( "instructions" );
                        xsw.writeAttribute ( "size", Integer.toString(tp.getInstructions ().size ()) );

                        for ( final Map.Entry<String, String> entry : tp.getInstructions ().entrySet () )
                        {
                            xsw.writeStartElement ( "instruction" );
                            xsw.writeAttribute ( "key", entry.getKey () );
                            xsw.writeCharacters ( entry.getValue () );
                            xsw.writeEndElement (); // instruction
                        }

                        xsw.writeEndElement (); // instructions
                        xsw.writeEndElement (); // touchpointData
                    }
                }
            }
        }

        {
            xsw.writeStartElement ( "licenses" );
            xsw.writeAttribute ( "size", Integer.toString(this.licenses.size ()) );

            for ( final License licenseEntry : this.licenses )
            {
                xsw.writeStartElement ( "license" );

                if ( licenseEntry.getUri () != null )
                {
                    xsw.writeAttribute ( "url", licenseEntry.getUri () );
                    xsw.writeAttribute ( "uri", licenseEntry.getUri () );
                }

                if ( licenseEntry.getText () != null )
                {
                    xsw.writeCData ( licenseEntry.getText () );
                }

                xsw.writeEndElement (); // license
            }

            xsw.writeEndElement (); // licenses
        }

        if ( this.copyright != null || this.copyrightUrl != null )
        {
            xsw.writeStartElement ( "copyright" );

            if ( this.copyrightUrl != null )
            {
                xsw.writeAttribute ( "url", this.copyrightUrl );
                xsw.writeAttribute ( "uri", this.copyrightUrl );
            }
            if ( this.copyright != null )
            {
                xsw.writeCData ( this.copyright );
            }
            xsw.writeEndElement ();
        }

        xsw.writeEndElement (); // unit
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

}
