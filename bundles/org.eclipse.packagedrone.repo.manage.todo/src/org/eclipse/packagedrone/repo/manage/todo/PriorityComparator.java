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
package org.eclipse.packagedrone.repo.manage.todo;

import java.util.Comparator;

public class PriorityComparator implements Comparator<Task>
{
    public static final Comparator<Task> INSTANCE = new PriorityComparator ();

    private PriorityComparator ()
    {
    }

    @Override
    public int compare ( final Task o1, final Task o2 )
    {
        return Integer.compare ( o1.getPriority (), o2.getPriority () );
    }
}
