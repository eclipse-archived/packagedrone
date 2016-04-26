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
package org.eclipse.packagedrone.web.forms.tags;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtils;

final class Helper
{
    private Helper ()
    {
    }

    public static String makeString ( final Object o, final String path, final String defaultValue )
    {
        if ( path != null && !path.isEmpty () )
        {
            try
            {
                return BeanUtils.getProperty ( o, path );
            }
            catch ( IllegalAccessException | InvocationTargetException | NoSuchMethodException e )
            {
                throw new RuntimeException ( e );
            }
        }

        if ( o == null )
        {
            return defaultValue;
        }

        return o.toString ();
    }
}
