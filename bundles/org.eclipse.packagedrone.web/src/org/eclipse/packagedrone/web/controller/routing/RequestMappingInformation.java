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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.packagedrone.web.util.Requests;

public class RequestMappingInformation
{
    private final PathMatcher[] paths;

    private final Set<String> methods;

    private final Set<String> rawPaths;

    public class Match
    {
        private final Map<String, String> attributes;

        public Match ( final Map<String, String> attributes )
        {
            this.attributes = attributes;
        }

        public Map<String, String> getAttributes ()
        {
            return this.attributes;
        }
    }

    public RequestMappingInformation ( final String path, final String... methods )
    {
        this ( Collections.singleton ( path ), new HashSet<> ( Arrays.asList ( methods ) ) );
    }

    public RequestMappingInformation ( final Set<String> paths, final Set<String> methods )
    {
        this.methods = methods;
        this.paths = new PathMatcher[paths.size ()];

        this.rawPaths = paths;

        int i = 0;
        for ( final String path : paths )
        {
            this.paths[i] = convert ( path );
            i++;
        }
    }

    public Set<String> getRawPaths ()
    {
        return this.rawPaths;
    }

    private PathMatcher convert ( final String path )
    {
        if ( path.contains ( "{" ) )
        {
            return createPatternMatcher ( path );
        }
        else
        {
            return new PlainPathMatcher ( path );
        }
    }

    private static final Pattern pp = Pattern.compile ( "\\{([^\\/]+)\\}" );

    private PathMatcher createPatternMatcher ( final String path )
    {
        final List<String> names = new LinkedList<String> ();

        final Matcher m = pp.matcher ( path );

        final StringBuffer sb = new StringBuffer ();
        while ( m.find () )
        {
            final String name = m.group ( 1 );
            names.add ( name );
            m.appendReplacement ( sb, "([^\\/]+)" );
        }
        m.appendTail ( sb );

        final Pattern pattern = Pattern.compile ( sb.toString () );
        return new PatternPathMatcher ( pattern, names.toArray ( new String[names.size ()] ) );
    }

    public Match matches ( final HttpServletRequest request )
    {
        final String rp = Requests.getRequestPath ( request ); // get the simple path
        final String method = request.getMethod ().toUpperCase ();

        return matches ( rp, method );
    }

    public Match matches ( final String path, final String method )
    {
        if ( !this.methods.isEmpty () && !this.methods.contains ( method ) )
        {
            return null;
        }

        for ( final PathMatcher pathMatcher : this.paths )
        {
            final Map<String, String> result = pathMatcher.matches ( path );
            if ( result != null )
            {
                return new Match ( result );
            }
        }
        return null;
    }
}
