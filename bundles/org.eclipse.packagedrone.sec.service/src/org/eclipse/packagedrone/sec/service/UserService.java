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

public interface UserService
{
    public UserInformation checkCredentials ( String username, String credentials, boolean rememberMe ) throws LoginException;

    public UserInformation refresh ( UserInformation user );

    /**
     * Check if this user service is a working user base
     *
     * @return <code>true</code> if this service is ready for use and has at
     *         least one user configured, <code>false</code> otherwise.
     */
    public boolean hasUserBase ();
}
