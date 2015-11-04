/*******************************************************************************
 * Copyright (c) 2014 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.web.controller.routing;

import java.util.Collections;
import java.util.Map;

public class PlainPathMatcher implements PathMatcher
{
    private final String path;

    public PlainPathMatcher ( final String path )
    {
        this.path = path;
    }

    @Override
    public Map<String, String> matches ( final String path )
    {
        if ( this.path.equals ( path ) )
        {
            return Collections.emptyMap ();
        }
        return null;
    }
}
