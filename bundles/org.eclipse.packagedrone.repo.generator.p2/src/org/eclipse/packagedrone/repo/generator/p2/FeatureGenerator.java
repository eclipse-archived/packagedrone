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

import static org.eclipse.packagedrone.repo.MetaKeys.getString;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.XmlHelper;
import org.eclipse.packagedrone.repo.aspect.common.osgi.OsgiExtractor;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.eclipse.packagedrone.repo.channel.ChannelArtifactInformation;
import org.eclipse.packagedrone.repo.channel.search.Predicates;
import org.eclipse.packagedrone.repo.generator.ArtifactGenerator;
import org.eclipse.packagedrone.repo.generator.GenerationContext;
import org.eclipse.packagedrone.repo.utils.osgi.bundle.BundleInformation;
import org.eclipse.packagedrone.web.LinkTarget;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class FeatureGenerator implements ArtifactGenerator
{

    private static final String QUALIFIER_SUFFIX = ".qualifier";

    private static final DateFormat QUALIFIER_DATE_FORMAT = new SimpleDateFormat ( "yyyyMMddHHmm" );

    static
    {
        QUALIFIER_DATE_FORMAT.setTimeZone ( TimeZone.getTimeZone ( "UTC" ) );
    }

    public static final String ID = "p2.feature";

    private final XmlHelper xml;

    public FeatureGenerator ()
    {
        this.xml = new XmlHelper ();
    }

    @Override
    public LinkTarget getAddTarget ()
    {
        return LinkTarget.createFromController ( GeneratorController.class, "createFeature" );
    }

    @Override
    public LinkTarget getEditTarget ( final ChannelArtifactInformation artifact )
    {
        final Map<String, String> model = new HashMap<> ( 2 );

        model.put ( "channelId", artifact.getChannelId ().getId () );
        model.put ( "artifactId", artifact.getId () );

        final String url = LinkTarget.createFromController ( GeneratorController.class, "editFeature" ).render ( model );
        return new LinkTarget ( url );
    }

    @Override
    public void generate ( final GenerationContext context ) throws Exception
    {
        final String id = getString ( context.getArtifactInformation ().getMetaData (), ID, "id" );
        final String version = getString ( context.getArtifactInformation ().getMetaData (), ID, "version" );

        context.createVirtualArtifact ( String.format ( "%s-%s.jar", id, version ), out -> {
            try ( final ZipOutputStream jar = new ZipOutputStream ( out ) )
            {
                final ZipEntry ze = new ZipEntry ( "feature.xml" );
                jar.putNextEntry ( ze );
                createFeatureXml ( jar, context.getArtifactInformation ().getMetaData (), context );
            }
        }, null );
    }

    private void createFeatureXml ( final OutputStream out, final Map<MetaKey, String> map, final GenerationContext context ) throws IOException
    {
        final String id = getString ( map, ID, "id" );
        final String version = makeVersion ( getString ( map, ID, "version" ) );
        final String label = getString ( map, ID, "label" );

        final String description = getString ( map, ID, "description" );
        final String copyright = getString ( map, ID, "copyright" );
        final String license = getString ( map, ID, "license" );

        final String descriptionUrl = getString ( map, ID, "descriptionUrl" );
        final String copyrightUrl = getString ( map, ID, "copyrightUrl" );
        final String licenseUrl = getString ( map, ID, "licenseUrl" );

        final String provider = getString ( map, ID, "provider" );

        final String bsnPattern = getString ( map, ID, "artifactFilter" );

        final Document doc = this.xml.create ();
        final Element root = doc.createElement ( "feature" );
        doc.appendChild ( root );

        root.setAttribute ( "id", id );
        root.setAttribute ( "version", version );
        root.setAttribute ( "label", label );

        if ( provider != null )
        {
            root.setAttribute ( "provider-name", provider );
        }

        createLegalEntry ( root, "description", description, descriptionUrl );
        createLegalEntry ( root, "copyright", copyright, copyrightUrl );
        createLegalEntry ( root, "license", license, licenseUrl );

        if ( bsnPattern == null || bsnPattern.isEmpty () )
        {
            for ( final ArtifactInformation a : context.getChannelArtifacts () )
            {
                processPlugin ( root, a );
            }
        }
        else
        {
            for ( final ArtifactInformation a : context.getArtifactLocator ().search ( Predicates.like ( OsgiExtractor.KEY_NAME, bsnPattern ) ) )
            {
                processPlugin ( root, a );
            }
        }

        try
        {
            this.xml.write ( doc, out );
        }
        catch ( final IOException e )
        {
            throw e;
        }
        catch ( final Exception e )
        {
            throw new IOException ( e );
        }
    }

    public void createLegalEntry ( final Element root, final String type, final String text, final String url )
    {
        if ( text == null && url == null )
        {
            return;
        }

        if ( text != null && text.isEmpty () && url.isEmpty () )
        {
            return;
        }

        final Element ele = XmlHelper.addElement ( root, type );
        if ( text != null )
        {
            ele.setTextContent ( text );
        }
        else
        {
            ele.setTextContent ( "" );
        }

        if ( url != null && !url.isEmpty () )
        {
            ele.setAttribute ( "url", url );
        }
    }

    private String makeVersion ( String version )
    {
        if ( version == null )
        {
            return "0.0.0";
        }

        if ( !version.endsWith ( QUALIFIER_SUFFIX ) )
        {
            return version;
        }

        version = version.substring ( 0, version.length () - QUALIFIER_SUFFIX.length () );

        return version + "." + makeTimestamp ( System.currentTimeMillis () );
    }

    private String makeTimestamp ( final long time )
    {
        return FeatureGenerator.QUALIFIER_DATE_FORMAT.format ( new Date ( time ) );
    }

    private void processPlugin ( final Element root, final ArtifactInformation a )
    {
        if ( !Helper.isBundle ( a.getMetaData () ) )
        {
            return;
        }

        final String id = a.getMetaData ().get ( OsgiExtractor.KEY_NAME );
        final String version = a.getMetaData ().get ( OsgiExtractor.KEY_VERSION );
        if ( id == null || version == null )
        {
            return;
        }

        boolean unpack = false;

        try
        {
            final String biString = a.getMetaData ().get ( OsgiExtractor.KEY_BUNDLE_INFORMATION );
            final BundleInformation bi = BundleInformation.fromJson ( biString );
            unpack = "dir".equals ( bi.getEclipseBundleShape () );
        }
        catch ( final Exception e )
        {
        }

        final Element p = root.getOwnerDocument ().createElement ( "plugin" );
        root.appendChild ( p );

        p.setAttribute ( "id", id );
        p.setAttribute ( "version", version );
        p.setAttribute ( "unpack", "" + unpack );
    }

    @Override
    public boolean shouldRegenerate ( final Object event )
    {
        return Helper.shouldRegenerateFeature ( event );
    }

}
