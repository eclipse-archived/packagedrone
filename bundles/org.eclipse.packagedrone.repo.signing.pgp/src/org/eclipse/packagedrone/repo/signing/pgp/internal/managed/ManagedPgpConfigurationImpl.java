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

import java.util.List;

import org.eclipse.packagedrone.repo.signing.pgp.ManagedKey;
import org.eclipse.packagedrone.repo.signing.pgp.ManagedPgpConfiguration;
import org.eclipse.scada.utils.ExceptionHelper;

public class ManagedPgpConfigurationImpl implements ManagedPgpConfiguration
{
    private final Configuration cfg;

    private final Exception error;

    private final List<ManagedKey> keys;

    public ManagedPgpConfigurationImpl ( final Configuration cfg, final Exception error, final List<ManagedKey> keys )
    {
        this.cfg = cfg;
        this.error = error;
        this.keys = keys;
    }

    @Override
    public String getId ()
    {
        return this.cfg.getId ();
    }

    @Override
    public String getLabel ()
    {
        return this.cfg.getLabel ();
    }

    @Override
    public List<ManagedKey> getKeys ()
    {
        return this.keys;
    }

    @Override
    public Exception getError ()
    {
        return this.error;
    }

    @Override
    public String getErrorMessage ()
    {
        if ( this.error == null )
        {
            return null;
        }

        return ExceptionHelper.getRootCause ( this.error ).getClass ().getSimpleName () + ": " + ExceptionHelper.getMessage ( getError () );
    }
}
