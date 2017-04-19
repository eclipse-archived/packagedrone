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
package org.eclipse.packagedrone.web.common.tags;

import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.web.common.Modifier;

public class Functions
{
    /**
     * Convert a modifier value to a bootstrap type modifier
     *
     * @param prefix
     *            an optional prefix to add
     * @param modifier
     *            the modifier to convert
     * @return the bootstrap type string, optionally with a prefix attached,
     *         never <code>null</code>
     */
    public static String modifier ( final String prefix, final Modifier modifier )
    {
        if ( modifier == null )
        {
            return "";
        }

        String value = null;
        switch ( modifier )
        {
            case DEFAULT:
                value = "default";
                break;
            case PRIMARY:
                value = "primary";
                break;
            case SUCCESS:
                value = "success";
                break;
            case INFO:
                value = "info";
                break;
            case WARNING:
                value = "warning";
                break;
            case DANGER:
                value = "danger";
                break;
            case LINK:
                value = "link";
                break;
            default:
                break;
        }

        if ( value != null && prefix != null )
        {
            return prefix + value;
        }
        else
        {
            return value != null ? value : "";
        }
    }

    /**
     * Get all meta data values which match namespace and key
     *
     * @param metadata
     *            the map of meta data to filter
     * @param namespace
     *            an optional namespace filter
     * @param key
     *            an optional key filter
     * @return the result set
     */
    public static SortedSet<String> metadata ( final Map<MetaKey, String> metadata, String namespace, String key )
    {
        final SortedSet<String> result = new TreeSet<> ();

        if ( namespace.isEmpty () )
        {
            namespace = null;
        }

        if ( key.isEmpty () )
        {
            key = null;
        }

        for ( final Map.Entry<MetaKey, String> entry : metadata.entrySet () )
        {
            if ( namespace != null && !namespace.equals ( entry.getKey ().getNamespace () ) )
            {
                continue;
            }

            if ( key != null && !key.equals ( entry.getKey ().getKey () ) )
            {
                continue;
            }

            result.add ( entry.getValue () );
        }

        return result;
    }
}
