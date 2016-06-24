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
package org.eclipse.packagedrone.addon.internal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import org.eclipse.packagedrone.addon.Addon;
import org.eclipse.packagedrone.addon.AddonInformation;
import org.eclipse.packagedrone.utils.Suppressed;

public class AddonImpl implements Addon
{
    private final Path path;

    private final AddonRegistration registration;

    public AddonImpl ( final AddonRegistration registration )
    {
        this.path = registration.getPath ();
        this.registration = registration;
    }

    @Override
    public String getId ()
    {
        return this.registration.getId ();
    }

    @Override
    public void enable ()
    {
        try
        {
            Files.write ( enabledFile (), Collections.singletonList ( "" ) );
        }
        catch ( final IOException e )
        {
            throw new RuntimeException ( "Failed to enable addon", e );
        }
    }

    @Override
    public void disable ()
    {
        try
        {
            Files.deleteIfExists ( enabledFile () );
        }
        catch ( final IOException e )
        {
            throw new RuntimeException ( "Failed to disable addon", e );
        }
    }

    private Path enabledFile ()
    {
        return this.path.getParent ().resolve ( this.path.getFileName () + ".enable" );
    }

    @Override
    public void uninstall ()
    {
        try ( Suppressed<RuntimeException> s = new Suppressed<> ( "Failed to uninstall addon", RuntimeException::new ) )
        {
            s.run ( () -> Files.deleteIfExists ( this.path ) );
            s.run ( () -> Files.deleteIfExists ( enabledFile () ) );
        }
    }

    @Override
    public AddonInformation getInformation ()
    {
        return this.registration.getInformation ();
    }

}
