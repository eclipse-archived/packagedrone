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

import java.nio.file.Path;

import org.eclipse.packagedrone.addon.AddonInformation;

public interface AddonRegistration
{
    public String getId ();

    public void start ();

    public void stop ();

    public Path getPath ();

    public AddonInformation getInformation ();
}
