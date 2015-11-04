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
package org.eclipse.packagedrone.utils;

import java.util.Collections;
import java.util.Map;

public class AttributedValue
{
    private final String value;

    private final Map<String, String> attributes;

    public AttributedValue ( final String value, final Map<String, String> attributes )
    {
        this.value = value;
        this.attributes = attributes == null ? Collections.<String, String> emptyMap () : Collections.unmodifiableMap ( attributes );
    }

    public AttributedValue ( final String value )
    {
        this.value = value;
        this.attributes = Collections.emptyMap ();
    }

    public Map<String, String> getAttributes ()
    {
        return this.attributes;
    }

    public String getValue ()
    {
        return this.value;
    }
}
