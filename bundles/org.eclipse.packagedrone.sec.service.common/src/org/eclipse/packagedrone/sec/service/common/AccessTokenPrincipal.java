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
package org.eclipse.packagedrone.sec.service.common;

import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class AccessTokenPrincipal implements Principal
{
    private final String id;

    private final Set<String> roles;

    public AccessTokenPrincipal ( final String id, final Collection<String> roles )
    {
        this.id = id;
        if ( roles != null )
        {
            this.roles = Collections.unmodifiableSet ( new HashSet<> ( roles ) );
        }
        else
        {
            this.roles = Collections.emptySet ();
        }
    }

    @Override
    public String getName ()
    {
        return this.id;
    }

    public Set<String> getRoles ()
    {
        return this.roles;
    }
}
