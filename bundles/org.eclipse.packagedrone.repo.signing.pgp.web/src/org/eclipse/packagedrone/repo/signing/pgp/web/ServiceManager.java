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
package org.eclipse.packagedrone.repo.signing.pgp.web;

import java.io.IOException;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.packagedrone.repo.signing.SigningService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

public class ServiceManager
{
    private static final String ID = "pgp.signer";

    public static final Object ACTION_TAG_PGP = new Object ();

    private ConfigurationAdmin configAdmin;

    private final BundleContext context;

    public ServiceManager ()
    {
        this.context = FrameworkUtil.getBundle ( ServiceManager.class ).getBundleContext ();
    }

    public void setConfigAdmin ( final ConfigurationAdmin configAdmin )
    {
        this.configAdmin = configAdmin;
    }

    public List<InformationEntry> list () throws IOException, InvalidSyntaxException
    {
        final List<InformationEntry> result = new LinkedList<> ();

        final Configuration[] list = this.configAdmin.listConfigurations ( String.format ( "(%s=%s)", ConfigurationAdmin.SERVICE_FACTORYPID, ID ) );
        if ( list != null )
        {
            for ( final Configuration cfg : list )
            {
                final InformationEntry info = new InformationEntry ();
                info.setId ( cfg.getPid () );

                final Collection<ServiceReference<SigningService>> refs = this.context.getServiceReferences ( SigningService.class, String.format ( "(%s=%s)", Constants.SERVICE_PID, cfg.getPid () ) );
                info.setServicePresent ( !refs.isEmpty () );

                info.setKeyring ( makeString ( cfg.getProperties ().get ( "keyring" ) ) );
                info.setKeyId ( makeString ( cfg.getProperties ().get ( "key.id" ) ) );
                info.setLabel ( makeString ( cfg.getProperties ().get ( "description" ) ) );

                result.add ( info );
            }
        }

        return result;
    }

    private String makeString ( final Object object )
    {
        if ( object instanceof String )
        {
            return (String)object;
        }
        return null;
    }

    public void add ( final AddEntry data ) throws IOException
    {
        final Configuration cfg = this.configAdmin.createFactoryConfiguration ( ID, null );
        final Dictionary<String, Object> properties = new Hashtable<> ();

        properties.put ( "keyring", data.getKeyring () );
        properties.put ( "key.id", data.getKeyId () );
        properties.put ( "key.passphrase", data.getKeyPassphrase () );
        properties.put ( "description", data.getLabel () );

        cfg.update ( properties );
    }

    public void delete ( final String pid ) throws IOException
    {
        final Configuration cfg = this.configAdmin.getConfiguration ( pid );
        if ( cfg != null )
        {
            cfg.delete ();
        }
    }
}
