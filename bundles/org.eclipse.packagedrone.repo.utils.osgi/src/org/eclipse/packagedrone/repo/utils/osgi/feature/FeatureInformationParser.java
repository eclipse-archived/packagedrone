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

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.packagedrone.repo.XmlHelper;
import org.eclipse.packagedrone.repo.utils.osgi.ParserHelper;
import org.eclipse.packagedrone.repo.utils.osgi.feature.FeatureInformation.FeatureInclude;
import org.eclipse.packagedrone.repo.utils.osgi.feature.FeatureInformation.PluginInclude;
import org.eclipse.packagedrone.repo.utils.osgi.feature.FeatureInformation.Qualifiers;
import org.eclipse.packagedrone.repo.utils.osgi.feature.FeatureInformation.Requirement;
import org.eclipse.packagedrone.repo.utils.osgi.feature.FeatureInformation.Requirement.MatchRule;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class FeatureInformationParser
{
    private final static Logger logger = LoggerFactory.getLogger ( FeatureInformationParser.class );

    private final ZipFile file;

    private final XmlHelper xml;

    public FeatureInformationParser ( final ZipFile file )
    {
        this.xml = new XmlHelper ();
        this.file = file;
    }

    public FeatureInformation parse () throws IOException
    {
        final ZipEntry ze = this.file.getEntry ( "feature.xml" );
        if ( ze == null )
        {
            logger.debug ( "There is no feature.xml in the archive" );
            return null;
        }

        Document doc;
        try ( InputStream is = this.file.getInputStream ( ze ) )
        {
            try
            {
                doc = this.xml.parse ( is );
            }
            catch ( final Exception e )
            {
                logger.info ( "Failed to parse feature.xml", e );
                return null;
            }
        }

        final Element root = doc.getDocumentElement ();

        final String id = makeNull ( root.getAttribute ( "id" ) );
        if ( id == null )
        {
            logger.info ( "Feature ID is not set" );
            return null;
        }

        final String version = makeNull ( root.getAttribute ( "version" ) );
        if ( version == null )
        {
            logger.info ( "Feature version is not set" );
            return null;
        }

        final FeatureInformation result = new FeatureInformation ();

        result.setId ( id );
        result.setVersion ( new Version ( version ) );

        result.setProvider ( makeNull ( root.getAttribute ( "provider-name" ) ) );
        result.setLabel ( makeNull ( root.getAttribute ( "label" ) ) );
        result.setPlugin ( makeNull ( root.getAttribute ( "plugin" ) ) );

        result.setQualifiers ( Qualifiers.parse ( root ) );

        for ( final Node node : XmlHelper.iter ( root.getChildNodes () ) )
        {
            if ( ! ( node instanceof Element ) )
            {
                continue;
            }

            final Element ele = (Element)node;

            if ( "description".equals ( node.getNodeName () ) )
            {
                result.setDescriptionUrl ( makeNull ( ele.getAttribute ( "url" ) ) );
                result.setDescription ( trim ( ele.getTextContent () ) );
            }

            if ( "copyright".equals ( node.getNodeName () ) )
            {
                result.setCopyrightUrl ( makeNull ( ele.getAttribute ( "url" ) ) );
                result.setCopyright ( trim ( ele.getTextContent () ) );
            }

            if ( "license".equals ( node.getNodeName () ) )
            {
                result.setLicenseUrl ( makeNull ( ele.getAttribute ( "url" ) ) );
                result.setLicense ( trim ( ele.getTextContent () ) );
            }

            if ( "includes".equals ( node.getNodeName () ) )
            {
                processFeatureInclude ( result, ele );
            }

            if ( "requires".equals ( node.getNodeName () ) )
            {
                processRequirements ( result, ele );
            }

            if ( "plugin".equals ( node.getNodeName () ) )
            {
                processPluginInclude ( result, ele );
            }
        }

        attachLocalization ( result );

        return result;
    }

    private String trim ( final String text )
    {
        if ( text == null )
        {
            return text;
        }
        return text.trim ();
    }

    private String makeNull ( final String value )
    {
        if ( value == null || value.isEmpty () )
        {
            return null;
        }
        return value;
    }

    private void processRequirements ( final FeatureInformation result, final Element ele )
    {
        final Set<Requirement> reqs = result.getRequirements ();

        for ( final Element im : XmlHelper.iterElement ( ele, "import" ) )
        {
            final String feature = im.getAttribute ( "feature" );
            final String plugin = im.getAttribute ( "plugin" );
            final String vs = makeNull ( im.getAttribute ( "version" ) );
            final MatchRule match = makeMatch ( im.getAttribute ( "match" ) );

            Version version = null;
            if ( vs != null )
            {
                version = new Version ( vs );
            }

            Requirement req;
            if ( feature != null && !feature.isEmpty () )
            {
                req = new Requirement ( Requirement.Type.FEATURE, feature, version, match );
            }
            else
            {
                req = new Requirement ( Requirement.Type.PLUGIN, plugin, version, match );
            }
            reqs.add ( req );
        }
    }

    private MatchRule makeMatch ( final String matchString )
    {
        if ( matchString == null )
        {
            return MatchRule.DEFAULT;
        }

        final MatchRule mr = MatchRule.findById ( matchString );
        if ( mr != null )
        {
            return mr;
        }
        else
        {
            return MatchRule.DEFAULT;
        }
    }

    private void processPluginInclude ( final FeatureInformation result, final Element ele )
    {
        final String id = ele.getAttribute ( "id" );
        final String vs = makeNull ( ele.getAttribute ( "version" ) );
        final Version version = new Version ( vs == null ? "0.0.0" : vs );

        final String unpackAttr = ele.getAttribute ( "unpack" );

        final boolean unpack = unpackAttr == null || unpackAttr.isEmpty () ? true : Boolean.parseBoolean ( unpackAttr );

        final Qualifiers q = Qualifiers.parse ( ele );

        result.getIncludedPlugins ().add ( new PluginInclude ( id, version, unpack, q ) );
    }

    private void processFeatureInclude ( final FeatureInformation result, final Element ele )
    {
        final String id = ele.getAttribute ( "id" );
        final String vs = makeNull ( ele.getAttribute ( "version" ) );
        final Version version = new Version ( vs == null ? "0.0.0" : vs );
        final String name = makeNull ( ele.getAttribute ( "name" ) );
        final boolean optional = Boolean.parseBoolean ( ele.getAttribute ( "optional" ) );

        final Qualifiers q = Qualifiers.parse ( ele );

        result.getIncludedFeatures ().add ( new FeatureInclude ( id, version, name, optional, q ) );
    }

    private void attachLocalization ( final FeatureInformation result ) throws IOException
    {
        result.setLocalization ( ParserHelper.loadLocalization ( this.file, "feature" ) );
    }
}
