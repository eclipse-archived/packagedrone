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
package org.eclipse.packagedrone.sec.service.common.internal;

import java.util.Collection;

import org.eclipse.packagedrone.sec.UserInformation;
import org.eclipse.packagedrone.sec.service.LoginException;
import org.eclipse.packagedrone.sec.service.SecurityService;
import org.eclipse.packagedrone.sec.service.UserService;
import org.eclipse.scada.utils.ExceptionHelper;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecurityServiceImpl implements SecurityService
{
    private final static Logger logger = LoggerFactory.getLogger ( SecurityServiceImpl.class );

    private final ServiceTracker<UserService, UserService> userServiceTracker;

    public SecurityServiceImpl ()
    {
        this.userServiceTracker = new ServiceTracker<> ( FrameworkUtil.getBundle ( SecurityServiceImpl.class ).getBundleContext (), UserService.class, null );
    }

    public void activate ()
    {
        this.userServiceTracker.open ();
    }

    public void deactivate ()
    {
        this.userServiceTracker.close ();
    }

    @Override
    public UserInformation login ( final String username, final String password ) throws LoginException
    {
        return login ( username, password, false );
    }

    @Override
    public UserInformation login ( final String username, final String password, final boolean rememberMe ) throws LoginException
    {
        final Collection<UserService> services = this.userServiceTracker.getTracked ().values ();

        if ( services == null || services.isEmpty () )
        {
            throw new LoginException ( "No login service available" );
        }

        for ( final UserService service : services )
        {
            try
            {
                // pass on to the next user service
                final UserInformation user = service.checkCredentials ( username, password, rememberMe );
                if ( user != null )
                {
                    // got a login
                    return user;
                }
            }
            catch ( final Exception e )
            {
                if ( ExceptionHelper.getRootCause ( e ) instanceof LoginException )
                {
                    // this failure is ok
                    throw e;
                }
                // this one it not and so we continue afterwards
                logger.warn ( "UserService failed", e );
            }
        }

        // end of the service list, nobody knowns this user
        throw new LoginException ( "Login error!", "Invalid username or password." );
    }

    @Override
    public boolean hasUserBase ()
    {
        final Collection<UserService> services = this.userServiceTracker.getTracked ().values ();

        for ( final UserService service : services )
        {
            if ( service.hasUserBase () )
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public UserInformation refresh ( final UserInformation user )
    {
        final Collection<UserService> services = this.userServiceTracker.getTracked ().values ();

        if ( services == null || services.isEmpty () )
        {
            return user;
        }

        for ( final UserService service : services )
        {
            final UserInformation refreshedUser = service.refresh ( user );
            if ( refreshedUser != null )
            {
                return refreshedUser;
            }
        }

        return user;
    }
}
