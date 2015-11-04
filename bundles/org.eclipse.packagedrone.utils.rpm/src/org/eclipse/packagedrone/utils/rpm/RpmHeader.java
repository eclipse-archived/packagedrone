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
package org.eclipse.packagedrone.utils.rpm;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RpmHeader<T extends RpmBaseTag>
{
    private final Map<Integer, Object> tags;

    private final long start;

    private final long length;

    public RpmHeader ( final RpmEntry[] entries, final long start, final long length )
    {
        final Map<Integer, Object> tags = new HashMap<> ( entries.length );
        for ( final RpmEntry entry : entries )
        {
            tags.put ( entry.getTag (), entry.getValue () );
        }

        this.tags = Collections.unmodifiableMap ( tags );

        this.start = start;
        this.length = length;
    }

    /**
     * Get the start position of the header section in the stream
     *
     * @return the start position
     */
    public long getStart ()
    {
        return this.start;
    }

    /**
     * Get the length of header section in the stream
     *
     * @return the length of the header in bytes
     */
    public long getLength ()
    {
        return this.length;
    }

    public Object getTag ( final T tag )
    {
        return this.tags.get ( tag.getValue () );
    }

    public Object getTagOrDefault ( final T tag, final Object defaultValue )
    {
        return this.tags.getOrDefault ( tag, defaultValue );
    }

    public Map<Integer, Object> getRawTags ()
    {
        return this.tags;
    }

}
