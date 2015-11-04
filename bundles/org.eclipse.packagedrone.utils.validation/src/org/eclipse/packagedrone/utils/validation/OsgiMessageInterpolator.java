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

package org.eclipse.packagedrone.utils.validation;

import java.util.LinkedList;
import java.util.Locale;
import java.util.Locale.Category;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;

import javax.validation.MessageInterpolator;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;

public class OsgiMessageInterpolator implements MessageInterpolator
{
    private static class Resolver
    {
        private final ClassLoader classLoader;

        public Resolver ( final Bundle bundle )
        {
            this.classLoader = bundle.adapt ( BundleWiring.class ).getClassLoader ();
        }

        public String resolve ( final String name, final Context context, final Locale locale )
        {
            try
            {
                final ResourceBundle resourceBundle = ResourceBundle.getBundle ( "META-INF/ValidationMessages", locale != null ? locale : Locale.getDefault (), this.classLoader );

                return resourceBundle.getString ( name );
            }
            catch ( final MissingResourceException e )
            {
                return null;
            }
        }

        public void dispose ()
        {
            ResourceBundle.clearCache ( this.classLoader );
        }
    }

    private MessageInterpolator fallback;

    private BundleTracker<Resolver> tracker;

    public OsgiMessageInterpolator ( final BundleContext context )
    {
        this.tracker = new BundleTracker<> ( context, Bundle.ACTIVE | Bundle.RESOLVED, new BundleTrackerCustomizer<Resolver> () {

            @Override
            public Resolver addingBundle ( final Bundle bundle, final BundleEvent event )
            {
                if ( bundle.getResource ( "META-INF/ValidationMessages.properties" ) != null )
                {
                    return new Resolver ( bundle );
                }
                return null;
            }

            @Override
            public void modifiedBundle ( final Bundle bundle, final BundleEvent event, final Resolver resolver )
            {
            }

            @Override
            public void removedBundle ( final Bundle bundle, final BundleEvent event, final Resolver resolver )
            {
                resolver.dispose ();
            }
        } );
        this.tracker.open ();
    }

    public void dispose ()
    {
        this.tracker.close ();
    }

    public void setFallback ( final MessageInterpolator fallback )
    {
        this.fallback = fallback;
    }

    @Override
    public String interpolate ( final String message, final Context context )
    {
        return interpolate ( message, context, Locale.getDefault ( Category.DISPLAY ) );
    }

    @Override
    public String interpolate ( final String message, final Context context, final Locale locale )
    {
        final StringBuilder sb = new StringBuilder ();

        boolean escaped = false;

        final LinkedList<StringBuilder> keyStack = new LinkedList<> ();

        for ( int i = 0; i < message.length (); i++ )
        {
            final char c = message.charAt ( i );
            if ( escaped )
            {
                escaped = false;
                sb.append ( c );
            }
            else
            {
                switch ( c )
                {
                    case '\\':
                        escaped = true;
                        break;
                    case '{':
                        keyStack.push ( new StringBuilder () );
                        break;
                    case '}':
                        try
                        {
                            final StringBuilder keySb = keyStack.pop ();
                            sb.append ( resolve ( keySb.toString (), context, locale ) );
                        }
                        catch ( final NoSuchElementException e )
                        {
                            sb.append ( '}' );
                        }
                        break;
                    default:
                        if ( keyStack.isEmpty () )
                        {
                            sb.append ( c );
                        }
                        else
                        {
                            keyStack.peek ().append ( c );
                        }
                        break;
                }
            }
        }

        return sb.toString ();
    }

    protected String resolve ( final String key, final Context context, final Locale locale )
    {
        for ( final Resolver resolver : this.tracker.getTracked ().values () )
        {
            final String result = resolver.resolve ( key, context, locale );
            if ( result != null )
            {
                return result;
            }
        }

        final MessageInterpolator fallback = this.fallback;

        if ( fallback == null )
        {
            return null;
        }
        return fallback.interpolate ( String.format ( "{%s}", key ), context, locale );
    }

}
