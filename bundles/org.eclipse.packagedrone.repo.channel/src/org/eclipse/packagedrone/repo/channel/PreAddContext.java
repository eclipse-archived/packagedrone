/*******************************************************************************
 * Copyright (c) 2014, 2016 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.channel;

import java.nio.file.Path;

public interface PreAddContext
{
    public Path getFile ();

    public String getName ();

    public default void vetoAdd ()
    {
        vetoAdd ( VetoPolicy.REJECT );
    }

    public void vetoAdd ( VetoPolicy policy );

    /**
     * A flag if this is an external or internal add operation
     */
    public boolean isExternal ();
}
