/*******************************************************************************
 * Copyright (c) 2014, 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.aspect.listener;

import java.nio.file.Path;

public interface PreAddContext
{
    public String getName ();

    public Path getFile ();

    public void vetoAdd ();

    /**
     * A flag if this is an external or internal add operation
     */
    public boolean isExternal ();
}
