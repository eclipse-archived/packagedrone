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
package org.eclipse.packagedrone.repo.channel;

import static org.eclipse.packagedrone.repo.channel.search.Predicates.and;
import static org.eclipse.packagedrone.repo.channel.search.Predicates.equal;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.channel.search.Predicates;
import org.eclipse.packagedrone.repo.channel.search.Predicate;
import org.junit.Assert;
import org.junit.Test;

public class FilterTest
{
    private static final MetaKey KEY = new MetaKey ( "", "" );

    @Test
    public void testEqual1 ()
    {
        final Predicate p1 = Predicates.and ( Predicates.equal ( KEY, "foo" ) );
        final Predicate p2 = and ( equal ( KEY, "foo" ) );

        Assert.assertEquals ( p1, p2 );
        Assert.assertEquals ( p2, p1 );
    }

    @Test
    public void testNotEqual1 ()
    {
        final Predicate p1 = Predicates.and ( Predicates.equal ( KEY, "foo" ) );
        final Predicate p2 = Predicates.or ( equal ( KEY, "foo" ) );

        Assert.assertNotEquals ( p1, p2 );
        Assert.assertNotEquals ( p2, p1 );
    }
}
