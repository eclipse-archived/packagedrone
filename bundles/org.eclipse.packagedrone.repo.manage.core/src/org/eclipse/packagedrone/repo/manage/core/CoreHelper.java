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

import org.eclipse.packagedrone.repo.MetaKey;

public final class CoreHelper
{
    private CoreHelper ()
    {
    }

    public static int getInteger ( final CoreService service, final MetaKey key, final int defaultValue )
    {
        final String result = service.getCoreProperty ( key, null );
        if ( result == null )
        {
            return defaultValue;
        }
        try
        {
            return Integer.parseInt ( result );
        }
        catch ( final NumberFormatException e )
        {
            return defaultValue;
        }
    }
}
