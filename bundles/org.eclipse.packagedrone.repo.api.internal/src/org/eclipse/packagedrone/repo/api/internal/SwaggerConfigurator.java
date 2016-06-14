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
package org.eclipse.packagedrone.repo.api.internal;

import java.io.IOException;
import java.util.Hashtable;

import org.eclipse.packagedrone.repo.api.Version;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

public class SwaggerConfigurator
{
    private ConfigurationAdmin configurationAdmin;

    public void setConfigurationAdmin ( final ConfigurationAdmin configurationAdmin )
    {
        this.configurationAdmin = configurationAdmin;
    }

    public void start () throws IOException
    {
        final Configuration cfg = this.configurationAdmin.getConfiguration ( "com.eclipsesource.jaxrs.swagger.config", "?" );

        final Hashtable<String, Object> data = new Hashtable<> ();

        data.put ( "swagger.info.title", "Eclipse Package Drone API" );
        data.put ( "swagger.info.version", Version.API_VERSION );
        data.put ( "swagger.info.description", "Description" );
        data.put ( "swagger.info.license.name", "EPL" );
        data.put ( "swagger.info.license.url", "https://www.eclipse.org/legal/epl-v10.html" );

        cfg.update ( data );
    }
}
