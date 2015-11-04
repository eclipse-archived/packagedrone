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
package org.eclipse.packagedrone.repo.channel.apm.internal;

import java.util.LinkedList;
import java.util.List;

public class Finally
{
    private final List<Runnable> runnables = new LinkedList<> ();

    public Finally ()
    {
    }

    public void add ( final Runnable r )
    {
        this.runnables.add ( r );
    }

    public void runAll ()
    {
        LinkedList<Throwable> errors = null;

        for ( final Runnable r : this.runnables )
        {
            try
            {
                r.run ();
            }
            catch ( final Throwable e )
            {
                if ( errors == null )
                {
                    errors = new LinkedList<> ();
                }
                errors.add ( e );
            }
        }

        if ( errors != null )
        {
            final RuntimeException e = new RuntimeException ( "Failed to process finally", errors.pollFirst () );
            errors.stream ().forEach ( e::addSuppressed );
            throw e;
        }
    }
}
