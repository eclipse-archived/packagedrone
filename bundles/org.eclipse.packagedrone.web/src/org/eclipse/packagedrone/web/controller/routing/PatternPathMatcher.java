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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternPathMatcher implements PathMatcher
{
    private final Pattern pattern;

    private final String[] names;

    public PatternPathMatcher ( final Pattern pattern, final String[] names )
    {
        this.pattern = pattern;
        this.names = names;
    }

    @Override
    public Map<String, String> matches ( final String path )
    {
        final Matcher m = this.pattern.matcher ( path );
        if ( !m.matches () )
        {
            return null;
        }

        final Map<String, String> result = new HashMap<> ( this.names.length );

        for ( int i = 0; i < this.names.length; i++ )
        {
            result.put ( this.names[i], m.group ( i + 1 ) );
        }

        return result;
    }

}
