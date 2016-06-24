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
package org.eclipse.packagedrone.addon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.felix.service.command.Descriptor;
import org.eclipse.scada.utils.ExceptionHelper;
import org.eclipse.scada.utils.str.Tables;

public class Console
{
    private AddonManager addonManager;

    public void setAddonManager ( final AddonManager addonManager )
    {
        this.addonManager = addonManager;
    }

    @Descriptor ( "List installed addons" )
    public void list ()
    {
        final List<Addon> result = this.addonManager.list ();

        final List<String> header = Arrays.asList ( "ID", "Name", "Version", "State", "Label", "Error" );
        final List<List<String>> data = new ArrayList<> ( result.size () );

        for ( final Addon addon : result )
        {
            final List<String> row = new ArrayList<> ( 6 );
            data.add ( row );

            row.add ( addon.getId () );
            row.add ( addon.getInformation ().getDescription ().getId () );
            row.add ( addon.getInformation ().getDescription ().getVersion ().toString () );
            row.add ( addon.getInformation ().getState ().toString () );
            row.add ( addon.getInformation ().getDescription ().getLabel () );
            if ( addon.getInformation ().getError () != null )
            {
                row.add ( ExceptionHelper.getMessage ( addon.getInformation ().getError () ).replace ( "\n", " " ).replace ( "\r", "" ) );
            }
        }

        Tables.showTable ( System.out, header, data, 2 );
    }

    @Descriptor ( "Information about an addon" )
    public void info ( @Descriptor ( "The addon registration id" ) final String addonId )
    {
        withAddon ( addonId, addon -> {
            final AddonDescription desc = addon.getInformation ().getDescription ();
            System.out.format ( "%s (%s:%s)%n", desc.getLabel (), desc.getId (), desc.getVersion () );
            System.out.println ( "=======================================================" );
            System.out.format ( "\tAddon registration: %s%n", addon.getId () );
            System.out.format ( "\tState: %s%n", addon.getInformation ().getState () );
            if ( addon.getInformation ().getStateInformation () != null )
            {
                System.out.println ( "-------------------------------------------------------" );
                String s = addon.getInformation ().getStateInformation ();
                while ( s.endsWith ( "\n" ) || s.endsWith ( "\r" ) )
                {
                    s = s.substring ( 0, s.length () - 1 );
                }
                System.out.println ( s );
            }
            if ( addon.getInformation ().getError () != null )
            {
                System.out.println ( "-------------------------------------------------------" );
                addon.getInformation ().getError ().printStackTrace ( System.out );
            }
            System.out.println ( "-------------------------------------------------------" );
        } );
    }

    public void install ()
    {

    }

    @Descriptor ( "Remove an addon from the system" )
    public void uninstall ( @Descriptor ( "The addon registration id" ) final String addonId )
    {
        withAddon ( addonId, Addon::uninstall );
    }

    @Descriptor ( "Enable an addon" )
    public void enable ( @Descriptor ( "The addon registration id" ) final String addonId )
    {
        withAddon ( addonId, Addon::enable );
    }

    @Descriptor ( "Disable an addon" )
    public void disable ( @Descriptor ( "The addon registration id" ) final String addonId )
    {
        withAddon ( addonId, Addon::disable );
    }

    protected void withAddon ( final String addonId, final Consumer<Addon> consumer )
    {
        final Optional<Addon> addon = this.addonManager.getAddon ( addonId );
        if ( addon.isPresent () )
        {
            consumer.accept ( addon.get () );
        }
        else
        {
            System.out.format ( "Unable to find addon: %s%n", addonId );
        }
    }
}
