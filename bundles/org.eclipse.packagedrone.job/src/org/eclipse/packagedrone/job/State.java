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
package org.eclipse.packagedrone.job;

public enum State
{
    SCHEDULED ( 0 ),
    RUNNING ( 1 ),
    COMPLETE ( 2 );

    private int id;

    State ( final int id )
    {
        this.id = id;
    }

    public int getId ()
    {
        return this.id;
    }

    public static State fromId ( final int id )
    {
        switch ( id )
        {
            case 0:
                return SCHEDULED;
            case 1:
                return RUNNING;
            case 2:
                return COMPLETE;
        }
        throw new IllegalArgumentException ( String.format ( "State %s is unknown", id ) );
    }
}
