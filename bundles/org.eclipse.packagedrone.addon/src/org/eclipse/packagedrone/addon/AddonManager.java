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
package org.eclipse.packagedrone.addon;

import java.net.URI;
import java.util.List;
import java.util.Optional;

public interface AddonManager
{
    public Addon install ( URI uri );

    public List<Addon> list ();

    public Optional<Addon> getAddon ( String id );
}
