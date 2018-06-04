/*******************************************************************************
 * Copyright (c) 2016 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *     SMX Ltd. - support for additional RPM file flags
 *******************************************************************************/
package org.eclipse.packagedrone.utils.rpm;

import java.util.EnumSet;

public enum FileFlags
{
    CONFIGURATION ( 1 << 0 ), /*!< from %%config */
    DOC ( 1 << 1 ), /*!< from %%doc */
    ICON ( 1 << 2 ), /*!< from %%donotuse. */
    MISSINGOK ( 1 << 3 ), /*!< from %%config(missingok) */
    NOREPLACE ( 1 << 4 ), /*!< from %%config(noreplace) */
    GHOST ( 1 << 6 ), /*!< from %%ghost */
    LICENSE ( 1 << 7 ), /*!< from %%license */
    README ( 1 << 8 ), /*!< from %%readme */
    /* bits 9-10 unused */
    PUBKEY ( 1 << 11 ), /*!< from %%pubkey */
    ARTIFACT ( 1 << 12 ); /*!< from %%artifact */

    private int value;

    private FileFlags ( final int value )
    {
        this.value = value;
    }

    public int getValue ()
    {
        return this.value;
    }

    public static EnumSet<FileFlags> decode ( final int flagValue )
    {
        final EnumSet<FileFlags> fileFlags = EnumSet.noneOf ( FileFlags.class );
        if ( flagValue != 0 )
        {
            for ( final FileFlags fileFlag : FileFlags.values () )
            {
                if ( ( fileFlag.getValue () & flagValue ) == fileFlag.getValue () )
                {
                    fileFlags.add ( fileFlag );
                }
            }
        }
        return fileFlags;
    }

}
