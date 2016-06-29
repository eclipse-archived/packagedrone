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
package org.eclipse.packagedrone.repo.adapter.rpm;

import org.eclipse.packagedrone.utils.rpm.info.RpmInformation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class RpmInformationsJson
{
    private static Gson makeGson ()
    {
        final GsonBuilder gb = new GsonBuilder ();
        return gb.create ();
    }

    public static RpmInformation fromJson ( final String json )
    {
        if ( json == null )
        {
            return null;
        }

        return makeGson ().fromJson ( json, RpmInformation.class );
    }

    public static String toJson ( final RpmInformation info )
    {
        return makeGson ().toJson ( info );
    }
}
