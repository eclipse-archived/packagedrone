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
package org.eclipse.packagedrone.sec.service;

import java.util.List;
import java.util.Optional;

public interface AccessTokenService
{
    /**
     * List access tokens
     *
     * @param start
     *            start position
     * @param amount
     *            number of entries
     * @return the list of access tokens
     */
    public List<AccessToken> list ( int start, int amount );

    /**
     * Create a new access token
     *
     * @param description
     *            an optional description
     * @return the new access token
     */
    public AccessToken createAccessToken ( String description );

    /**
     * Delete an access token
     *
     * @param id
     *            the access token id
     */
    public void deleteAccessToken ( String id );

    /**
     * Get an access token
     *
     * @param id
     *            the access token id
     * @return the result
     */
    public Optional<AccessToken> getToken ( String id );

    /**
     * Edit an access token
     * 
     * @param id
     *            the access token id to edit
     * @param description
     *            the new description
     */
    public void editAccessToken ( String id, String description );
}
