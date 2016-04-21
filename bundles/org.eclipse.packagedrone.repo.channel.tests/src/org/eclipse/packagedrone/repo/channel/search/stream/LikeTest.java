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
package org.eclipse.packagedrone.repo.channel.search.stream;

import static org.eclipse.packagedrone.repo.channel.search.Predicates.like;
import static org.eclipse.packagedrone.repo.channel.search.Predicates.literal;

import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.junit.Assert;
import org.junit.Test;

public class LikeTest
{
    @Test
    public void testLikeSimple ()
    {
        assertLike ( "", null, "", true );
        assertLike ( "foo", null, "", false );
        assertLike ( "", null, "foo", false );
        assertLike ( "foo", null, "foo", true );
    }

    @Test
    public void testLikeSingleMany ()
    {
        assertLike ( "%", null, "", true );
        assertLike ( "%", null, "foo", true );
    }

    @Test
    public void testLikeStart ()
    {
        assertLike ( "%foo", null, "", false );
        assertLike ( "%bar", null, "", false );
        assertLike ( "%foo", null, "foo", true );
        assertLike ( "%bar", null, "foo", false );
        assertLike ( "%foo", null, "foobar", false );
        assertLike ( "%foo", null, "barfoo", true );
        assertLike ( "%foo", null, "foobarfoo", true );
        assertLike ( "%bar", null, "foobarfoo", false );
    }

    @Test
    public void testLikeEnd ()
    {
        assertLike ( "foo%", null, "", false );
        assertLike ( "bar%", null, "", false );
        assertLike ( "foo%", null, "foo", true );
        assertLike ( "bar%", null, "foo", false );
        assertLike ( "foo%", null, "foobar", true );
        assertLike ( "foo%", null, "barfoo", false );
        assertLike ( "foo%", null, "foobarfoo", true );
        assertLike ( "bar%", null, "foobarfoo", false );
    }

    @Test
    public void testLikeMiddleOne ()
    {
        assertLike ( "%foo%", null, "", false );
        assertLike ( "%bar%", null, "", false );
        assertLike ( "%foo%", null, "foo", true );
        assertLike ( "%bar%", null, "foo", false );
        assertLike ( "%foo%", null, "foobar", true );
        assertLike ( "%foo%", null, "barfoo", true );

        assertLike ( "%foo%", null, "foobarfoo", true );
        assertLike ( "%bar%", null, "foobarfoo", true );
    }

    @Test
    public void testLikeMiddleMany ()
    {
        assertLike ( "%foo%bar%", null, "", false );
        assertLike ( "%foo%bar%", null, "foobar", true );
        assertLike ( "%foo%bar%", null, "abcfoobarabc", true );
        assertLike ( "%foo%bar%", null, "abcfoocabc", false );
    }

    @Test
    public void testLikeSingle ()
    {
        assertLike ( "_", null, "", false );
        assertLike ( "_", null, "f", true );

        assertLike ( "___", null, "foo", true );
        assertLike ( "___", null, "bar", true );
        assertLike ( "____", null, "foo", false );
        assertLike ( "__", null, "foo", false );

        assertLike ( "foo___foo", null, "foobarfoo", true );
        assertLike ( "foo___foo", null, "foofoofoo", true );
        assertLike ( "foo_foo", null, "foobarfoo", false );
        assertLike ( "foo_foo", null, "foofoofoo", false );

        assertLike ( "_f_", null, "foo", false );
        assertLike ( "_f_", null, "bfr", true );
    }

    @Test
    public void testLikeMixed ()
    {
        assertLike ( "_oo%bar", null, "", false );
        assertLike ( "_oo%bar", null, "foobar", true );
        assertLike ( "_oo%bar", null, "fooabcbar", true );
        assertLike ( "_oo%bar", null, "aooabcbar", true );
    }

    @Test
    public void testLikeEscapedLike ()
    {
        assertLike ( "\\%", null, "", false );
        assertLike ( "\\%", null, "%", true );

        assertLike ( "foo\\%", null, "foo%", true );
        assertLike ( "\\%foo", null, "%foo", true );
        assertLike ( "\\%foo\\%", null, "%foo%", true );

        assertLike ( "%\\%", null, "", false );
        assertLike ( "%\\%", null, "%", true );
        assertLike ( "%\\%", null, "%%", true );

        assertLike ( "\\_", null, "", false );
        assertLike ( "\\_", null, "_", true );

        assertLike ( "\\\\", null, "", false );
        assertLike ( "\\\\", null, "\\", true );

        assertLike ( "\\%\\\\\\_", null, "%\\_", true );
    }

    /**
     * One implementation of Like is based backed by using the {@link Pattern}
     * class. Test that all valid regexp patterns are escaped property to not
     * influence the pattern generation
     */
    @Test
    public void testLikeEscapedPattern ()
    {
        assertLike ( ".", null, "", false );
        assertLike ( ".", null, "f", false );
        assertLike ( ".", null, ".", true );

        assertLike ( ".*", null, "", false );
        assertLike ( ".*", null, "f", false );
        assertLike ( ".*", null, ".*", true );

        assertLike ( escape ( Pattern.quote ( "foo" ) ), null, "foo", false );
        assertLike ( escape ( Pattern.quote ( "foo" ) ), null, Pattern.quote ( "foo" ), true );
    }

    @Test
    public void testLikeCorner ()
    {
        assertLike ( "%%", null, "", true );
    }

    private static String escape ( final String string )
    {
        return string.replace ( "\\", "\\\\" ).replace ( "%", "\\%" ).replace ( "_", "\\_" );
    }

    private void assertLike ( final String pattern, final Boolean caseSensite, final String value, final boolean expectedResult )
    {
        final Predicate<ArtifactInformation> filter;
        if ( caseSensite == null )
        {
            filter = new PredicateFilterBuilder ( like ( literal ( value ), literal ( pattern ) ) ).build ();
        }
        else
        {
            filter = new PredicateFilterBuilder ( like ( literal ( value ), literal ( pattern ), caseSensite ) ).build ();
        }
        final boolean result = filter.test ( null );
        Assert.assertEquals ( expectedResult, result );
    }
}
