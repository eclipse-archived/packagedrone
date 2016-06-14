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
package org.eclipse.packagedrone.repo.generator.p2;

import java.io.IOException;
import java.io.OutputStream;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.eclipse.packagedrone.repo.event.AddedEvent;
import org.eclipse.packagedrone.repo.event.RemovedEvent;
import org.eclipse.packagedrone.repo.utils.osgi.feature.FeatureInformation;
import org.eclipse.packagedrone.repo.xml.XmlHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public final class Helper
{
    private final static Logger logger = LoggerFactory.getLogger ( Helper.class );

    private Helper ()
    {
    }

    public static boolean changeOf ( final Object event, final Type first, final Type... rest )
    {
        return changeOf ( event, EnumSet.of ( first, rest ) );
    }

    public static boolean changeOf ( final Object event, final Set<Type> types )
    {
        logger.debug ( "Check if we need to generate: {}", event );

        if ( event instanceof AddedEvent )
        {
            final AddedEvent context = (AddedEvent)event;
            if ( isChange ( context.getMetaData (), types ) )
            {
                return true;
            }

        }
        else if ( event instanceof RemovedEvent )
        {
            final RemovedEvent context = (RemovedEvent)event;
            if ( isChange ( context.getMetaData (), types ) )
            {
                return true;
            }
        }

        return false;
    }

    private static boolean isChange ( final Map<MetaKey, String> metaData, final Set<Type> types )
    {
        for ( final Type type : types )
        {
            switch ( type )
            {
                case BUNDLE:
                    if ( isBundle ( metaData ) )
                    {
                        return true;
                    }
                    break;
                case FEATURE:
                    if ( isFeature ( metaData ) )
                    {
                        return true;
                    }
                    break;
            }
        }
        return false;
    }

    public static boolean shouldRegenerateCategory ( final Object event )
    {
        logger.debug ( "Check if we need to generate: {}", event );
        final boolean result = changeOf ( event, Type.FEATURE );
        logger.debug ( "Result: {}", result );
        return result;
    }

    public static boolean shouldRegenerateFeature ( final Object event )
    {
        logger.debug ( "Check if we need to generate: {}", event );
        final boolean result = changeOf ( event, Type.BUNDLE );
        logger.debug ( "Result: {}", result );
        return result;
    }

    public static boolean isFeature ( final Map<MetaKey, String> metaData )
    {
        final String classifier = metaData.get ( new MetaKey ( "osgi", "classifier" ) );
        logger.debug ( "Artifact OSGi classifier: {}", classifier );
        return "eclipse.feature".equals ( classifier );
    }

    public static boolean isBundle ( final Map<MetaKey, String> metaData )
    {
        final String classifier = metaData.get ( new MetaKey ( "osgi", "classifier" ) );
        logger.debug ( "Artifact OSGi classifier: {}", classifier );
        return "bundle".equals ( classifier );
    }

    public static void addFeatureRequirement ( final Set<String> context, final Element requires, final ArtifactInformation a )
    {
        final String id = a.getMetaData ().get ( new MetaKey ( "osgi", "name" ) );
        if ( id == null )
        {
            return;
        }

        final String version = a.getMetaData ().get ( new MetaKey ( "osgi", "version" ) );

        final String key = String.format ( "%s.feature.group-%s", id, version );
        if ( !context.add ( key ) )
        {
            return;
        }

        final Element p = XmlHelper.addElement ( requires, "required" );

        final FeatureInformation fi = FeatureInformation.fromJson ( a.getMetaData ().get ( FeatureInformation.META_KEY ) );
        if ( fi != null )
        {
            final String filter = fi.getQualifiers ().toFilterString ();
            if ( filter != null )
            {
                XmlHelper.addElement ( p, "filter" ).setTextContent ( filter );
            }
        }

        p.setAttribute ( "namespace", "org.eclipse.equinox.p2.iu" );
        p.setAttribute ( "name", id + ".feature.group" );
        p.setAttribute ( "range", String.format ( "[%1$s,%1$s]", version ) );
    }

    public static void addBundleRequirement ( final Set<String> context, final Element requires, final ArtifactInformation a )
    {
        final String id = a.getMetaData ().get ( new MetaKey ( "osgi", "name" ) );
        if ( id == null )
        {
            return;
        }

        final String version = a.getMetaData ().get ( new MetaKey ( "osgi", "version" ) );

        final String key = String.format ( "%s.feature.group-%s", id, version );
        if ( !context.add ( key ) )
        {
            return;
        }

        final Element p = requires.getOwnerDocument ().createElement ( "required" );
        requires.appendChild ( p );

        p.setAttribute ( "namespace", "org.eclipse.equinox.p2.iu" );
        p.setAttribute ( "name", id );
        p.setAttribute ( "range", String.format ( "[%1$s,%1$s]", version ) );
    }

    public static void createFragmentFile ( final OutputStream out, final Consumer<Element> unitsConsumer ) throws IOException
    {
        final XmlHelper xml = new XmlHelper ();

        final Document doc = xml.create ();
        final Element units = doc.createElement ( "units" );
        doc.appendChild ( units );

        unitsConsumer.accept ( units );

        XmlHelper.fixSize ( units );
        try
        {
            xml.write ( doc, out );
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

    public static void addProperty ( final Element parent, final String key, final String value )
    {
        if ( value == null )
        {
            return;
        }

        final Element p = XmlHelper.addElement ( parent, "property" );
        p.setAttribute ( "name", key );
        p.setAttribute ( "value", value );
    }

    public static void createCategoryUnit ( final Element units, final String id, final String label, final String description, final String version, final Consumer<Element> unitConsumer )
    {
        final Element unit = XmlHelper.addElement ( units, "unit" );

        unit.setAttribute ( "id", id );
        unit.setAttribute ( "version", version );

        final Element props = XmlHelper.addElement ( unit, "properties" );
        addProperty ( props, "org.eclipse.equinox.p2.name", label );
        addProperty ( props, "org.eclipse.equinox.p2.description", description );
        addProperty ( props, "org.eclipse.equinox.p2.type.category", "true" );
        XmlHelper.fixSize ( props );

        {
            final Element provs = XmlHelper.addElement ( unit, "provides" );
            final Element p = XmlHelper.addElement ( provs, "provided" );
            p.setAttribute ( "namespace", "org.eclipse.equinox.p2.iu" );
            p.setAttribute ( "name", id );
            p.setAttribute ( "version", version );
            XmlHelper.fixSize ( provs );
        }

        unitConsumer.accept ( unit );

        {
            final Element t = XmlHelper.addElement ( unit, "touchpoint" );
            t.setAttribute ( "id", "null" );
            t.setAttribute ( "version", "0.0.0" );
        }
    }

    public static String makeDefaultVersion ()
    {
        return String.format ( "0.0.0.0--%x", System.currentTimeMillis () );
    }
}
