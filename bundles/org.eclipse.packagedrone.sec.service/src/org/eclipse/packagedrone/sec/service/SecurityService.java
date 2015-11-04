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
package org.eclipse.packagedrone.sec.service;

import org.eclipse.packagedrone.sec.UserInformation;

/**
 * A service dispatching authentication and authorization requests to backend
 * services
 */
public interface SecurityService
{
    public UserInformation login ( final String username, final String password ) throws LoginException;

    public UserInformation login ( final String username, final String password, boolean rememberMe ) throws LoginException;

    /**
     * If possible it refreshes the user details from its source <br>
     * If this is not possible it simply returns the inbound value
     *
     * @param user
     *            the user details to refresh
     * @return the, possibly, refreshed user details
     */
    public UserInformation refresh ( UserInformation user );

    /**
     * Check if there is a user base available
     * <p>
     * This method will check if there is <em>at least one</em> working user
     * base, not counting the
     * <q>god mode</q>. In other words, it only the
     * <q>god mode user</q> is available, this method must return
     * <code>false</code>.
     * </p>
     *
     * @return <code>true</code> if there is a user base, <code>false</code>
     *         otherwise
     */
    public boolean hasUserBase ();
}
