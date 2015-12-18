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

public interface ManagedPgpFactory
{
    public ManagedPgpConfiguration createService ( String label, String secretKey, String passphrase );

    public void deleteService ( String id );

    public List<ManagedPgpConfiguration> list ( int start, int amount );
}
