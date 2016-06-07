/*******************************************************************************
 * Copyright (c) 2015, 2016 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.utils.deb.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.packagedrone.utils.deb.FieldFormatter;

public final class BinarySectionPackagesFile
{
    public static final Map<String, FieldFormatter> FORMATTERS;

    static
    {
        final Map<String, FieldFormatter> formatters = new HashMap<> ();
        formatters.put ( "Description", FieldFormatter.MULTI );

        FORMATTERS = Collections.unmodifiableMap ( formatters );
    }

    private BinarySectionPackagesFile ()
    {
    }
}
