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
package org.eclipse.packagedrone.repo.signing.pgp.internal.managed;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.eclipse.packagedrone.repo.signing.pgp.internal.AbstractSecretKeySigningService;

public class ManagedSigningService extends AbstractSecretKeySigningService
{
    public ManagedSigningService ( final PGPSecretKey key, final String passphrase ) throws PGPException
    {
        super ( key, passphrase );
    }
}
