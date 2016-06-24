/*******************************************************************************
 * Copyright (c) 2016 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.addon.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.packagedrone.addon.AddonDescription;
import org.eclipse.packagedrone.addon.AddonInformation;
import org.eclipse.packagedrone.utils.io.CloseShieldInputStream;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.GsonBuilder;

public class DefaultAddonRegistration implements AddonRegistration
{
    private final static Logger logger = LoggerFactory.getLogger ( DefaultAddonRegistration.class );

    private static class StartFailedException extends RuntimeException
    {
        private static final long serialVersionUID = 1L;

        private final String stateInformation;

        public StartFailedException ( final String message, final Throwable cause, final String stateInformation )
        {
            super ( message, cause );
            this.stateInformation = stateInformation;
        }

        public String getStateInformation ()
        {
            return this.stateInformation;
        }
    }

    private interface State
    {
        public AddonInformation getInformation ();

        public State start ();

        public State stop ();
    }

    public static class Stopped implements State
    {
        private final Path path;

        private final AddonDescription description;

        private final String stateInformation;

        public Stopped ( final Path path, final AddonDescription description )
        {
            this.path = path;
            this.description = description;
            this.stateInformation = null;
        }

        public Stopped ( final Path path, final AddonDescription description, final String stateInformation )
        {
            this.path = path;
            this.description = description;
            this.stateInformation = stateInformation;
        }

        @Override
        public AddonInformation getInformation ()
        {
            return new AddonInformation ( org.eclipse.packagedrone.addon.State.INSTALLED, this.description, null, this.stateInformation );
        }

        @Override
        public State start ()
        {
            return tryStart ( this.path, this.description );
        }

        @Override
        public State stop ()
        {
            return this;
        }

    }

    public static class Started implements State
    {
        private final Path path;

        private final AddonDescription description;

        private final List<Bundle> bundles;

        private final String stateInformation;

        public Started ( final Path path, final AddonDescription description ) throws IOException
        {
            this.path = path;
            this.description = description;

            final StringWriter sw = new StringWriter ();
            final PrintWriter pw = new PrintWriter ( sw );

            final List<Bundle> bundles = new LinkedList<> ();
            final BundleContext context = FrameworkUtil.getBundle ( Started.class ).getBundleContext ();
            try ( ZipInputStream zis = new ZipInputStream ( Files.newInputStream ( path ) ) )
            {
                ZipEntry ze;
                while ( ( ze = zis.getNextEntry () ) != null )
                {
                    final String name = ze.getName ();
                    if ( name.endsWith ( ".jar" ) )
                    {
                        try
                        {
                            final Bundle bundle = context.installBundle ( path.toAbsolutePath ().toString () + "/" + name, new CloseShieldInputStream ( zis ) );
                            pw.format ( "Installed bundle: %s -> %s - %s/%s, state: %s%n", name, bundle.getBundleId (), bundle.getSymbolicName (), bundle.getVersion (), makeState ( bundle.getState () ) );
                            bundles.add ( bundle );
                        }
                        catch ( final BundleException e )
                        {
                            pw.format ( "Failed to install bundle: %s%n", name );
                            uninstall ( pw, bundles );
                            throw new StartFailedException ( "Failed to start addon - installing", e, sw.toString () );
                        }
                    }
                }
            }

            final List<Bundle> started = new LinkedList<> ();
            for ( final Bundle bundle : bundles )
            {
                try
                {
                    bundle.start ();
                    started.add ( bundle );
                }
                catch ( final BundleException e )
                {
                    logger.warn ( "Failed to start bundle", e );
                    pw.format ( "Failed to start bundle: %s%n", bundle.getBundleId () );
                    e.printStackTrace ( pw );

                    uninstall ( pw, bundles );

                    throw new StartFailedException ( "Failed to start addon - starting", e, sw.toString () );
                }
            }

            this.bundles = bundles;
            this.stateInformation = sw.toString ();
        }

        private static void uninstall ( final PrintWriter pw, final List<Bundle> bundles )
        {
            for ( final Bundle b : bundles )
            {
                try
                {
                    pw.format ( "Uninstalling bundle: %s%n", b.getBundleId () );
                    b.uninstall ();
                }
                catch ( final BundleException e1 )
                {
                    pw.format ( "Failed uninstalling bundle: %s%n", b.getBundleId () );
                    e1.printStackTrace ( pw );

                    // ignore
                }
            }
            bundles.clear ();
        }

        private String makeState ( final int state )
        {
            switch ( state )
            {
                case Bundle.ACTIVE:
                    return "ACTIVE";
                case Bundle.INSTALLED:
                    return "INSTALLED";
                case Bundle.RESOLVED:
                    return "RESOLVED";
                case Bundle.STARTING:
                    return "STARTING";
                case Bundle.STOPPING:
                    return "STOPPING";
                case Bundle.UNINSTALLED:
                    return "UNINSTALLED";
                default:
                    return Integer.toString ( state );
            }
        }

        @Override
        public AddonInformation getInformation ()
        {
            return new AddonInformation ( org.eclipse.packagedrone.addon.State.ACTIVE, this.description, null, this.stateInformation );
        }

        @Override
        public State start ()
        {
            return this;
        }

        @Override
        public State stop ()
        {
            final StringWriter sw = new StringWriter ();
            final PrintWriter pw = new PrintWriter ( sw );
            uninstall ( pw, this.bundles );
            return new Stopped ( this.path, this.description, sw.toString () );
        }
    }

    public static class Failed implements State
    {
        private final Throwable error;

        private final AddonDescription description;

        private final Path path;

        private final String stateInformation;

        public Failed ( final Path path, final Throwable e, final AddonDescription description, final String stateInformation )
        {
            this.path = path;
            this.error = e;
            this.description = description;
            this.stateInformation = stateInformation;
        }

        @Override
        public AddonInformation getInformation ()
        {
            return new AddonInformation ( org.eclipse.packagedrone.addon.State.FAILED, this.description, this.error, this.stateInformation );
        }

        @Override
        public State start ()
        {
            return tryStart ( this.path, this.description );
        }

        @Override
        public State stop ()
        {
            return this;
        }
    }

    public static class Invalid implements State
    {
        private final Throwable error;

        private final AddonDescription description;

        private final Path path;

        public Invalid ( final Path path, final Throwable e )
        {
            this.path = path;
            this.error = e;
            this.description = new AddonDescription ( "unknwown", Version.emptyVersion, "n/a" );
        }

        @Override
        public AddonInformation getInformation ()
        {
            return new AddonInformation ( org.eclipse.packagedrone.addon.State.FAILED, this.description, this.error, null );
        }

        @Override
        public State start ()
        {
            // try reloading
            return load ( this.path );
        }

        @Override
        public State stop ()
        {
            return this;
        }
    }

    private final Path path;

    private final String id;

    private volatile State state;

    public DefaultAddonRegistration ( final Path path )
    {
        this.path = path;
        this.id = path.getFileName ().toString ();
        this.state = load ( path );
    }

    private static State load ( final Path path )
    {
        try
        {
            return new Stopped ( path, loadDescription ( path ) );
        }
        catch ( final Throwable e )
        {
            return new Invalid ( path, e );
        }
    }

    private static AddonDescription loadDescription ( final Path path ) throws IOException
    {
        try ( ZipInputStream zis = new ZipInputStream ( Files.newInputStream ( path ) ) )
        {
            ZipEntry ze;
            while ( ( ze = zis.getNextEntry () ) != null )
            {
                String name = ze.getName ();
                while ( name.startsWith ( "/" ) )
                {
                    // remove leading slash
                    name = name.substring ( 1 );
                }
                if ( name.equals ( "addon.json" ) )
                {
                    return parseDescription ( zis );
                }
            }
        }

        throw new RuntimeException ( "Addon is missing the 'addon.json' descriptor" );
    }

    private static AddonDescription parseDescription ( final InputStream stream )
    {
        final GsonBuilder builder = new GsonBuilder ();

        final AddonDescriptor desc = builder.create ().fromJson ( new InputStreamReader ( stream, StandardCharsets.UTF_8 ), AddonDescriptor.class );
        if ( desc == null )
        {
            throw new RuntimeException ( "Failed to read descriptor" );
        }

        if ( desc.getId () == null || desc.getId ().isEmpty () )
        {
            throw new RuntimeException ( "Descriptor is missing the field 'name'" );
        }
        if ( desc.getVersion () == null || desc.getVersion ().isEmpty () )
        {
            throw new RuntimeException ( "Descriptor is missing the field 'version'" );
        }

        Version version;
        try
        {
            version = Version.valueOf ( desc.getVersion () );
        }
        catch ( final Exception e )
        {
            throw new RuntimeException ( "Invalid version: " + desc.getVersion (), e );
        }

        String label = desc.getLabel ();
        if ( label == null || label.isEmpty () )
        {
            label = desc.getId () + ":" + desc.getVersion ();
        }

        return new AddonDescription ( desc.getId (), version, label );
    }

    private static State tryStart ( final Path path, final AddonDescription description )
    {
        try
        {
            return new Started ( path, description );
        }
        catch ( final StartFailedException e )
        {
            return new Failed ( path, e, description, e.getStateInformation () );
        }
        catch ( final IOException e )
        {
            return new Invalid ( path, e );
        }
    }

    @Override
    public String getId ()
    {
        return this.id;
    }

    @Override
    public AddonInformation getInformation ()
    {
        return this.state.getInformation ();
    }

    @Override
    public Path getPath ()
    {
        return this.path;
    }

    @Override
    public void start ()
    {
        this.state = this.state.start ();
    }

    @Override
    public void stop ()
    {
        this.state = this.state.stop ();
    }
}
