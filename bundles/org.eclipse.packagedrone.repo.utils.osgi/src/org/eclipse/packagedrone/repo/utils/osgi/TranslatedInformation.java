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
package org.eclipse.packagedrone.repo.utils.osgi;

import java.util.Map;
import java.util.Properties;

public interface TranslatedInformation
{
    public Map<String, Properties> getLocalization ();

    public default Object translate ( final Object value )
    {
        if ( ! ( value instanceof String ) )
        {
            return value;
        }

        final String str = (String)value;
        if ( !str.startsWith ( "%" ) || str.length () < 2 )
        {
            return value;
        }

        try
        {
            return getLocalization ().get ( "df_LT" ).get ( str.substring ( 1 ) );
        }
        catch ( final Exception e )
        {
            return value;
        }
    }
}
