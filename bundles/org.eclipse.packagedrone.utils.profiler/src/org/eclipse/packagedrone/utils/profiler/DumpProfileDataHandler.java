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
package org.eclipse.packagedrone.utils.profiler;

import java.io.PrintStream;
import java.util.List;

import org.eclipse.packagedrone.utils.profiler.Profile.DurationEntry;

public class DumpProfileDataHandler implements ProfileDataHandler
{
    public static final ProfileDataHandler INSTANCE = new DumpProfileDataHandler ();

    private final PrintStream output;

    public DumpProfileDataHandler ()
    {
        this.output = System.out;
    }

    @Override
    public void handle ( final DurationEntry entry )
    {
        dumpEntry ( entry, 0 );
    }

    private void dumpEntry ( final DurationEntry entry, final int level )
    {
        this.output.format ( "%s%s: %s ms%n", makeIndent ( level ), entry.getOperation (), entry.getDuration ().toMillis () );
        dumpEntries ( entry.getEntries (), level + 1 );
    }

    private void dumpEntries ( final List<DurationEntry> entries, final int level )
    {
        for ( final DurationEntry entry : entries )
        {
            dumpEntry ( entry, level );
        }
    }

    static String makeIndent ( final int level )
    {
        final StringBuilder sb = new StringBuilder ( level * 2 );
        for ( int i = 0; i < level; i++ )
        {
            sb.append ( "  " );
        }
        return sb.toString ();
    }

}
