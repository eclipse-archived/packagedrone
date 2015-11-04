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
package org.eclipse.packagedrone.repo.manage.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.packagedrone.repo.MetaKey;

public interface CoreService
{
    public static final String DEFAULT_CORE_NS = "core";

    @Deprecated
    public default String getCoreProperty ( final String key )
    {
        return getCoreProperty ( new MetaKey ( DEFAULT_CORE_NS, key ) );
    }

    @Deprecated
    public default String getCoreProperty ( final String key, final String defaultValue )
    {
        return getCoreProperty ( new MetaKey ( DEFAULT_CORE_NS, key ), defaultValue );
    }

    /**
     * Get a core property
     *
     * @param key
     *            the key of the property
     * @return the value, or <code>null</code> if the property was not found
     */
    public default String getCoreProperty ( final MetaKey key )
    {
        return getCoreProperty ( key, null );
    }

    /**
     * Get a core property, or a default value instead <br/>
     * If the property entry is found but has a value of <code>null</code> the
     * value <code>null</code> is still being returned. However
     * the method {@link #setCoreProperty(String, String)} will automatically
     * delete entries when the value <code>null</code> is set.
     *
     * @param key
     *            the key of the property
     * @param defaultValue
     *            the default value
     * @return the value or the default value in case the property entry was not
     *         found.
     */
    public String getCoreProperty ( MetaKey key, String defaultValue );

    /**
     * Get multiple core properties
     * <p>
     * <em>Note:</em> The keys in the requested key collection should be unique.
     * The result map will contain keys exactly once.
     * </p>
     *
     * @param keys
     *            the collection of unique keys to read
     * @return the map, containing a mapping from the key to the value (may be
     *         <code>null</code>). Never returns null.
     */
    public Map<MetaKey, String> getCoreProperties ( Collection<MetaKey> keys );

    /**
     * Get multiple core properties with the same namespace
     * <p>
     * The method is a convenience method to
     * {@link #getCoreProperties(Collection)} and will behave as is if {@link
     * <code>getCoreProperties(Arrays.asList(keys))</code> would have been
     * called with all keys having the same namespace
     * </p>
     *
     * @param namespace
     *            the namespace common to all keys following
     * @param keys
     *            the array of unique keys to read
     * @return the map, containing a mapping from the key to the value (value
     *         may be <code>null</code>). Never returns <code>null</code>.
     */
    public default Map<String, String> getCoreNamespacePlainProperties ( final String namespace, final String... keys )
    {
        // get

        final Map<MetaKey, String> values = getCoreNamespaceProperties ( namespace, keys );

        // convert

        final Map<String, String> result = new HashMap<> ( values.size () );

        for ( final Map.Entry<MetaKey, String> entry : values.entrySet () )
        {
            result.put ( entry.getKey ().getKey (), entry.getValue () );
        }

        return result;
    }

    /**
     * Get multiple core properties with the same namespace
     * <p>
     * The method is a convenience method to
     * {@link #getCoreProperties(Collection)} and will behave as is if {@link
     * <code>getCoreProperties(Arrays.asList(keys))</code> would have been
     * called with all keys having the same namespace
     * </p>
     *
     * @param namespace
     *            the namespace common to all keys following
     * @param keys
     *            the array of unique keys to read
     * @return the map, containing a mapping from the key to the value (value
     *         may be <code>null</code>). Never returns <code>null</code>.
     */
    public default Map<MetaKey, String> getCoreNamespaceProperties ( final String namespace, final String... keys )
    {
        // convert

        final ArrayList<MetaKey> fullKeys = new ArrayList<> ( keys.length );

        for ( final String key : keys )
        {
            fullKeys.add ( new MetaKey ( namespace, key ) );
        }

        // get

        return getCoreProperties ( fullKeys );
    }

    @Deprecated
    public default void setCoreProperty ( final String key, final String value )
    {
        setCoreProperty ( DEFAULT_CORE_NS, key, value );
    }

    /**
     * Set a system global property
     *
     * @param namespace
     *            the namespace
     * @param key
     *            the key
     * @param value
     *            the value
     */
    public default void setCoreProperty ( final String namespace, final String key, final String value )
    {
        setCoreProperty ( new MetaKey ( namespace, key ), value );
    }

    /**
     * Set a system global property
     *
     * @param key
     *            the key
     * @param value
     *            the value
     */
    public void setCoreProperty ( MetaKey key, String value );

    public Map<MetaKey, String> list ();

    @Deprecated
    public default void setProperties ( final Map<String, String> properties )
    {
        setCoreProperties ( DEFAULT_CORE_NS, properties );
    }

    public default void setCoreProperties ( final String namespace, final Map<String, String> properties )
    {
        if ( properties == null )
        {
            return;
        }

        final Map<MetaKey, String> newProperties = new HashMap<> ( properties.size () );
        for ( final Map.Entry<String, String> entry : properties.entrySet () )
        {
            newProperties.put ( new MetaKey ( namespace, entry.getKey () ), entry.getValue () );
        }

        setCoreProperties ( newProperties );
    }

    /**
     * Set multiple properties at once
     *
     * @param properties
     *            the properties to set
     */
    public void setCoreProperties ( Map<MetaKey, String> properties );
}
