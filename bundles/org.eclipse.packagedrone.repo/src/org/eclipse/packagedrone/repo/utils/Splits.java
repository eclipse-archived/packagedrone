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
package org.eclipse.packagedrone.repo.utils;

import java.util.Collections;
import java.util.List;

public final class Splits
{
    private Splits ()
    {
    }

    public static <T> List<T> split ( final List<T> list, final int position, final int count )
    {
        final int size = list.size ();

        if ( position >= size )
        {
            return Collections.emptyList ();
        }

        final int rem = size - position;

        int end;

        if ( count < 0 )
        {
            end = position + rem;
        }
        else
        {
            end = Math.min ( position + rem, position + count );
        }

        return list.subList ( position, end );
    }

}
