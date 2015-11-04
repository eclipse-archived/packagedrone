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
package org.eclipse.packagedrone.utils.console;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
        dump ( Console.this.configAdmin.listConfigurations ( null ) );
    }

    public void listConfigs ( final String filter ) throws Exception
    {
        dump ( Console.this.configAdmin.listConfigurations ( filter ) );
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
