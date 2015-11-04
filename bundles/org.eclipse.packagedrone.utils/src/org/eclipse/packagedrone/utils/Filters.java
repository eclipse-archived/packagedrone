/*******************************************************************************
 * Copyright (c) 2014 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *     Julius Fingerle - fix a few repository generations issues
 *******************************************************************************/
package org.eclipse.packagedrone.utils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;

public class Filters
{
    public interface Node
    {
    }

    public static class Multi implements Node
    {
        private final String operator;

        private final List<Node> nodes;

        public Multi ( final String operator )
        {
            this.operator = operator;
            this.nodes = new LinkedList<> ();
        }

        private Multi ( final String operator, final List<Node> nodes )
        {
            this.operator = operator;
            this.nodes = nodes;
        }

        public void addNode ( final Node node )
        {
            this.nodes.add ( node );
        }

        @Override
        public String toString ()
        {
            return join ( this.nodes, this.operator );
        }

    }

    public static class Pair implements Node
    {
        private final String key;

        private final String value;

        private final String operator;

        public Pair ( final String key, final String value )
        {
            this.key = key;
            this.value = value;
            this.operator = "=";
        }

        public Pair ( final String key, final String value, final String operator )
        {
            this.key = key;
            this.value = value;
            this.operator = operator;
        }

        public String getKey ()
        {
            return this.key;
        }

        public String getValue ()
        {
            return this.value;
        }

        @Override
        public String toString ()
        {
            return "(" + this.key + this.operator + this.value + ")";
        }

    }

    public static class Negate implements Node
    {

        private final Node child;

        public Negate ( final Node node )
        {
            this.child = node;
        }

        public Node getChild ()
        {
            return this.child;
        }

        @Override
        public String toString ()
        {
            return "(!" + this.child.toString () + ")";
        }

    }

    public static String join ( final List<? extends Node> nodes, final String oper )
    {
        final List<String> s = new LinkedList<> ();
        for ( final Node node : nodes )
        {
            if ( node == null )
            {
                continue;
            }

            final String f = node.toString ();
            if ( !f.isEmpty () )
            {
                s.add ( f );
            }
        }

        if ( s == null || s.isEmpty () )
        {
            return "";
        }

        if ( s.size () == 1 )
        {
            return s.get ( 0 );
        }

        final StringBuilder builder = new StringBuilder ();
        builder.append ( '(' ).append ( oper );

        for ( final String tok : s )
        {
            builder.append ( tok );
        }

        builder.append ( ')' );

        return builder.toString ();
    }

    public static String or ( final List<? extends Node> nodes )
    {
        return join ( nodes, "|" );
    }

    public static String and ( final List<? extends Node> nodes )
    {
        return join ( nodes, "&" );
    }

    public static String and ( final Node... nodes )
    {
        return and ( Arrays.asList ( nodes ) );
    }

    public static String or ( final Node... nodes )
    {
        return or ( Arrays.asList ( nodes ) );
    }

    public static Node pair ( final String key, final String value )
    {
        if ( value == null )
        {
            return null;
        }
        return new Pair ( key, value );
    }

    public static Node pair ( final String key, final String value, final String operator )
    {
        if ( value == null )
        {
            return null;
        }
        return new Pair ( key, value, operator );
    }

    public static Node negate ( final Node node )
    {
        if ( node == null )
        {
            return null;
        }
        return new Negate ( node );
    }

    private static String version ( final Version version )
    {
        if ( version == null )
        {
            return null;
        }
        return version.toString ();
    }

    public static Node versionRange ( final String name, final VersionRange versionRange )
    {
        if ( versionRange == null )
        {
            return null;
        }

        final List<Node> nodes = new LinkedList<> ();

        if ( versionRange.getLeft () != null )
        {
            if ( versionRange.getLeftType () == VersionRange.LEFT_OPEN )
            {
                add ( nodes, pair ( name, version ( versionRange.getLeft () ), ">" ) );
            }
            else if ( versionRange.getLeftType () == VersionRange.LEFT_CLOSED )
            {
                add ( nodes, pair ( name, version ( versionRange.getLeft () ), ">=" ) );
            }
        }

        if ( versionRange.getRight () != null )
        {
            if ( versionRange.getRightType () == VersionRange.RIGHT_OPEN )
            {
                add ( nodes, negate ( pair ( name, version ( versionRange.getRight () ), ">=" ) ) );
            }
            else if ( versionRange.getRightType () == VersionRange.RIGHT_CLOSED )
            {
                add ( nodes, pair ( name, version ( versionRange.getRight () ), "<=" ) );
            }
        }

        if ( nodes.isEmpty () )
        {
            return null;
        }
        else
        {
            return new Multi ( "&", nodes );
        }
    }

    private static void add ( final List<Node> nodes, final Node node )
    {
        if ( node != null )
        {
            nodes.add ( node );
        }
    }
}
