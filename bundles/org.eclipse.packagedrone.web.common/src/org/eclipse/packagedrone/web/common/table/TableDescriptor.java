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
package org.eclipse.packagedrone.web.common.table;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class TableDescriptor
{
    private final String id;

    private final Set<String> tags;

    private final Map<String, Object> context;

    public TableDescriptor ( final String id, final Set<String> tags, final Map<String, Object> context )
    {
        this.id = id;
        this.tags = tags == null ? Collections.emptySet () : Collections.unmodifiableSet ( tags );
        this.context = context == null ? Collections.emptyMap () : Collections.unmodifiableMap ( context );
    }

    public String getId ()
    {
        return this.id;
    }

    public Set<String> getTags ()
    {
        return this.tags;
    }

    public Map<String, Object> getContext ()
    {
        return this.context;
    }

    public boolean hasTag ( final String tag )
    {
        return this.tags.contains ( tag );
    }
}
