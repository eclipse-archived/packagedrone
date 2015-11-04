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
package org.eclipse.packagedrone.sec.web.ui;

import javax.servlet.http.HttpSession;

public final class Sessions
{
    private Sessions ()
    {
    }

    private final static String ATTR_FAIL_COUNTER = Sessions.class.getName () + ".failCounter";

    public static long incrementLoginFailCounter ( final HttpSession session )
    {
        final Object val = session.getAttribute ( ATTR_FAIL_COUNTER );
        if ( val instanceof Number )
        {
            long nv = ( (Number)val ).longValue ();
            if ( nv != Long.MAX_VALUE )
            {
                // just in case
                nv++;
                session.setAttribute ( ATTR_FAIL_COUNTER, nv );
            }
            return nv;
        }
        else
        {
            session.setAttribute ( ATTR_FAIL_COUNTER, 1L );
            return 1;
        }
    }

    public static void resetLoginFailCounter ( final HttpSession session )
    {
        session.removeAttribute ( ATTR_FAIL_COUNTER );
    }
}
