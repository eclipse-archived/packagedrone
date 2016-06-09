/*******************************************************************************
 * Copyright (c) 2015, 2016 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.utils.console;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.scada.utils.str.Tables;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

public class Console
{
    private ConfigurationAdmin configAdmin;

    public void setConfigAdmin ( final ConfigurationAdmin configAdmin )
    {
        this.configAdmin = configAdmin;
    }

    public void unsetConfigAdmin ( final ConfigurationAdmin configAdmin )
    {
        this.configAdmin = null;
    }

    public void allConfigs () throws Exception
    {
        dump ( this.configAdmin.listConfigurations ( null ) );
    }

    public void listConfigs ( final String filter ) throws Exception
    {
        dump ( this.configAdmin.listConfigurations ( filter ) );
    }

    public void getConfig ( final String pid ) throws IOException
    {
        final Configuration cfg = this.configAdmin.getConfiguration ( pid );
        if ( cfg == null )
        {
            System.out.println ( "Not found" );
            return;
        }

        System.out.format ( "Location: %s%n", cfg.getBundleLocation () );
        System.out.format ( "Factory PID: %s%n", cfg.getFactoryPid () );
        System.out.format ( "PID: %s%n", cfg.getPid () );

        if ( cfg.getProperties () != null )
        {
            final List<List<String>> data = new LinkedList<> ();

            final Enumeration<String> en = cfg.getProperties ().keys ();
            while ( en.hasMoreElements () )
            {
                final String key = en.nextElement ();
                final Object value = cfg.getProperties ().get ( key );

                data.add ( Arrays.asList ( key, "" + value ) );
            }

            Tables.showTable ( System.out, Arrays.asList ( "Key", "Value" ), data, 2 );
        }
    }

    private void dump ( final Configuration[] cfgs )
    {
        if ( cfgs == null || cfgs.length <= 0 )
        {
            System.out.println ( "No configurations found!" );
            return;
        }

        final List<List<String>> data = new LinkedList<> ();

        for ( final Configuration cfg : cfgs )
        {
            final LinkedList<String> row = new LinkedList<> ();

            row.add ( cfg.getFactoryPid () );
            row.add ( cfg.getPid () );
            row.add ( "" + cfg.getChangeCount () );
            row.add ( cfg.getBundleLocation () );

            data.add ( row );
        }

        Collections.sort ( data, new Comparator<List<String>> () {

            @Override
            public int compare ( final List<String> o1, final List<String> o2 )
            {
                for ( int i = 0; i < Math.min ( o1.size (), o2.size () ); i++ )
                {
                    String v1 = o1.get ( i );
                    if ( v1 == null )
                    {
                        v1 = "";
                    }
                    String v2 = o2.get ( i );
                    if ( v2 == null )
                    {
                        v2 = "";
                    }
                    final int rc = v1.compareTo ( v2 );
                    if ( rc != 0 )
                    {
                        return rc;
                    }
                }

                return 0;
            }
        } );

        Tables.showTable ( System.out, Arrays.asList ( "Factory", "PID", "Changes", "Location" ), data, 2 );
    }
}
