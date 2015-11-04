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
package org.eclipse.packagedrone.repo.channel.apm.aspect;

import java.nio.file.Path;

import org.eclipse.packagedrone.repo.aspect.listener.PreAddContext;

public class PreAddContextImpl implements PreAddContext
{
    private final String name;

    private final Path file;

    private final boolean external;

    public PreAddContextImpl ( final String name, final Path file, final boolean external )
    {
        this.name = name;
        this.file = file;
        this.external = external;
    }

    private boolean veto;

    @Override
    public String getName ()
    {
        return this.name;
    }

    @Override
    public Path getFile ()
    {
        return this.file;
    }

    @Override
    public void vetoAdd ()
    {
        this.veto = true;
    }

    public boolean isVeto ()
    {
        return this.veto;
    }

    @Override
    public boolean isExternal ()
    {
        return this.external;
    }

}
