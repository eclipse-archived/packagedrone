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
package org.eclipse.packagedrone.web.dispatcher.internal;

import java.net.URL;
import java.util.Set;

public interface ResourceProvider
{
    public URL getResource ( String name );

    public Set<String> getPaths ( String path );

    public default void dispose ()
    {
    }
}
