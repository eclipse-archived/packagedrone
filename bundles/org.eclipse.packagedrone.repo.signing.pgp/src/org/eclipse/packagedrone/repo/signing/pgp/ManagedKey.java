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
package org.eclipse.packagedrone.repo.signing.pgp;

import java.util.List;

public class ManagedKey
{
    private final String id;

    private final List<String> userIds;

    private final boolean sub;

    private final int bits;

    public ManagedKey ( final String id, final List<String> userIds, final boolean sub, final int bits )
    {
        this.id = id;
        this.userIds = userIds;
        this.sub = sub;
        this.bits = bits;
    }

    public String getId ()
    {
        return this.id;
    }

    public String getShortId ()
    {
        return this.id.substring ( 8 );
    }

    public List<String> getUserIds ()
    {
        return this.userIds;
    }

    public boolean isSub ()
    {
        return this.sub;
    }

    public int getBits ()
    {
        return this.bits;
    }
}
