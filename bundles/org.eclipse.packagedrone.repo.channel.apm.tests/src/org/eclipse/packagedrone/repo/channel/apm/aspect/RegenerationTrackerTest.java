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
package org.eclipse.packagedrone.repo.channel.apm.aspect;

import static java.util.Arrays.sort;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.packagedrone.repo.channel.apm.aspect.RegenerationTracker;
import org.junit.Before;
import org.junit.Test;

public class RegenerationTrackerTest
{
    private List<Set<String>> collector;

    private RegenerationTracker tracker;

    @Before
    public void setup ()
    {
        this.collector = new LinkedList<> ();
        this.tracker = new RegenerationTracker ( this.collector::add );
    }

    @Test
    public void test1 ()
    {
        this.tracker.run ( () -> doWork ( "a", "b" ) );

        assertRegeneration ( new String[] { "a", "b" } );
    }

    @Test
    public void test2 ()
    {
        this.tracker.run ( () -> {
            doWork ( "a", "b" );
            this.tracker.run ( () -> doWork ( "c", "d" ) );
        } );
        assertRegeneration ( new String[] { "a", "b", "c", "d" } );
    }

    @Test
    public void test3 ()
    {
        this.tracker.run ( () -> {
            doWork ( "a", "b" );
            this.tracker.run ( () -> doWork ( "c", "d" ) );
        } );
        this.tracker.run ( () -> {
            doWork ( "a", "b" );
            this.tracker.run ( () -> doWork ( "c", "d" ) );
        } );
        assertRegeneration ( new String[] { "a", "b", "c", "d" }, new String[] { "a", "b", "c", "d" } );
    }

    @Test
    public void test4 ()
    {
        this.tracker.run ( () -> {
            doWork ( "a", "b" );
            try
            {
                this.tracker.run ( () -> {
                    doWork ( "c", "d" );
                    throw new RuntimeException ();
                } );
            }
            catch ( final Exception e )
            {
            }
        } );
        assertRegeneration ( new String[] { "a", "b" } );
    }

    @Test
    public void test5 ()
    {
        this.tracker.run ( () -> {
            doWork ( "a", "b" );
            try
            {
                this.tracker.run ( () -> {
                    doWork ( "c", "d" );
                    throw new RuntimeException ();
                } );
            }
            catch ( final Exception e )
            {
            }
            doWork ( "e", "f" );
        } );
        assertRegeneration ( new String[] { "a", "b", "e", "f" } );
    }

    private void assertRegeneration ( final String[]... groups )
    {
        assertEquals ( "Wrong number of groups", groups.length, this.collector.size () );

        for ( int i = 0; i < groups.length; i++ )
        {
            final String[] group = groups[i];
            final String[] collectorGroup = this.collector.get ( i ).toArray ( new String[0] );

            sort ( group );
            sort ( collectorGroup );
            assertArrayEquals ( group, collectorGroup );
        }

        this.collector.clear ();
    }

    private void doWork ( final String... ids )
    {
        for ( final String id : ids )
        {
            this.tracker.mark ( id );
        }
    }
}
