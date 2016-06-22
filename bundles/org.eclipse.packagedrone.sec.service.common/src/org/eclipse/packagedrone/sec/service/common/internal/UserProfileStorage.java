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
package org.eclipse.packagedrone.sec.service.common.internal;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.eclipse.packagedrone.sec.service.AccessToken;

public interface UserProfileStorage
{
    public Optional<Principal> getPrincipalFromAccessToken ( String accessToken );

    /**
     * Get a list of all access tokens sorted by ID
     *
     * @return a unmodifiable list of all access tokens
     */
    public List<AccessToken> list ();

    public Optional<AccessToken> getToken ( String id );
}
