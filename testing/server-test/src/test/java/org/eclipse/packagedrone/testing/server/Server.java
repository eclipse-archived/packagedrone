/*******************************************************************************
 * Copyright (c) 2015 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.testing.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Server
{
    public static String[] loadAdminToken () throws IOException
    {
        final Properties p = new Properties ();
        try ( InputStream in = new FileInputStream ( System.getProperty ( "user.home" ) + "/.drone-admin-token" ) )
        {
            p.load ( in );
        }

        final String user = p.getProperty ( "user" );
        final String token = p.getProperty ( "password" );
        if ( user == null || token == null )
        {
            throw new IllegalStateException ( "Admin token not found" );
        }

        return new String[] { user, token };
    }
}
