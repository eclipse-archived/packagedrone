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

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.eclipse.packagedrone.repo.channel.search.And;
import org.eclipse.packagedrone.repo.channel.search.Equal;
import org.eclipse.packagedrone.repo.channel.search.IsNull;
import org.eclipse.packagedrone.repo.channel.search.Like;
import org.eclipse.packagedrone.repo.channel.search.Literal;
import org.eclipse.packagedrone.repo.channel.search.MetaKeyValue;
import org.eclipse.packagedrone.repo.channel.search.Not;
import org.eclipse.packagedrone.repo.channel.search.Or;
import org.eclipse.packagedrone.repo.channel.search.Predicate;
import org.eclipse.packagedrone.repo.channel.search.Value;

/**
 * Build a {@link java.util.function.Predicate} for {@link ArtifactInformation}
 * based on the provided {@link Predicate}
 */
public class PredicateFilterBuilder
{
    private final static class AndPredicate<T> implements java.util.function.Predicate<T>
    {
        private final List<java.util.function.Predicate<T>> predicates;

        public AndPredicate ( final List<java.util.function.Predicate<T>> predicates )
        {
            this.predicates = predicates;
        }

        @Override
        public boolean test ( final T t )
        {
            for ( final java.util.function.Predicate<T> p : this.predicates )
            {
                if ( !p.test ( t ) )
                {
                    return false;
                }
            }
            return true;
        }

    }

    private final static class OrPredicate<T> implements java.util.function.Predicate<T>
    {
        private final List<java.util.function.Predicate<T>> predicates;

        public OrPredicate ( final List<java.util.function.Predicate<T>> predicates )
        {
            this.predicates = predicates;
        }

        @Override
        public boolean test ( final T t )
        {
            for ( final java.util.function.Predicate<T> p : this.predicates )
            {
                if ( p.test ( t ) )
                {
                    return true;
                }
            }
            return false;
        }
    }

    private final static class EqualPredicate<T, V> implements java.util.function.Predicate<T>
    {
        private final Function<T, V> valueFunction1;

        private final Function<T, V> valueFunction2;

        public EqualPredicate ( final Function<T, V> valueFunction1, final Function<T, V> valueFunction2 )
        {
            this.valueFunction1 = valueFunction1;
            this.valueFunction2 = valueFunction2;
        }

        @Override
        public boolean test ( final T t )
        {
            final V v1 = this.valueFunction1.apply ( t );
            final V v2 = this.valueFunction2.apply ( t );

            if ( v1 == v2 )
            {
                // could mean both null
                return true;
            }

            if ( v1 == null || v2 == null )
            {
                // now only either one can be null
                return false;
            }

            return v1.equals ( v2 );
        }
    }

    private final static class LikePredicate<T, V> implements java.util.function.Predicate<T>
    {
        private final Function<T, V> valueFunction;

        private final Pattern pattern;

        public LikePredicate ( final Function<T, V> valueFunction, final String pattern, final boolean caseSensitive )
        {
            this.valueFunction = valueFunction;
            this.pattern = makePattern ( pattern, caseSensitive );
        }

        @Override
        public boolean test ( final T t )
        {
            final V v1 = this.valueFunction.apply ( t );

            if ( v1 == null )
            {
                return false;
            }

            final String sv1 = v1.toString ();

            return this.pattern.matcher ( sv1 ).matches ();
        }

        private static Pattern makePattern ( final String pattern, final boolean caseSensitive )
        {
            final StringBuilder patternBuilder = new StringBuilder ( pattern.length () );

            boolean escaped = false;
            final StringBuilder sb = new StringBuilder ( pattern.length () );
            for ( int i = 0; i < pattern.length (); i++ )
            {
                final char c = pattern.charAt ( i );
                if ( escaped )
                {
                    sb.append ( c );
                    escaped = false;
                }
                else
                {
                    switch ( c )
                    {
                        case '%':
                            if ( sb.length () > 0 )
                            {
                                patternBuilder.append ( Pattern.quote ( sb.toString () ) );
                                sb.setLength ( 0 ); // clear
                            }
                            patternBuilder.append ( ".*" );
                            break;

                        case '_':
                            if ( sb.length () > 0 )
                            {
                                patternBuilder.append ( Pattern.quote ( sb.toString () ) );
                                sb.setLength ( 0 ); // clear
                            }
                            patternBuilder.append ( "." );
                            break;

                        case '\\':
                            escaped = true;
                            break;

                        default:
                            sb.append ( c );
                            break;
                    }
                }
            }

            if ( sb.length () > 0 )
            {
                patternBuilder.append ( Pattern.quote ( sb.toString () ) );
            }

            return Pattern.compile ( patternBuilder.toString (), caseSensitive ? 0 : Pattern.CASE_INSENSITIVE );
        }
    }

    private final Predicate predicate;

    /**
     * Create a new builder
     *
     * @param predicate
     *            the predicate to work on, must not be {@code null}
     */
    public PredicateFilterBuilder ( final Predicate predicate )
    {
        Objects.requireNonNull ( predicate );

        this.predicate = predicate;
    }

    /**
     * Build the predicate
     *
     * @return the actual {@link java.util.function.Predicate}, never returns
     *         {@code null}
     * @throws IllegalArgumentException
     *             if the original predicate contains an unknown predicate
     */
    public java.util.function.Predicate<ArtifactInformation> build ()
    {
        return buildFrom ( this.predicate );
    }

    protected java.util.function.Predicate<ArtifactInformation> buildFrom ( final Predicate current )
    {
        if ( current instanceof And )
        {
            return buildAnd ( (And)current );
        }
        else if ( current instanceof Or )
        {
            return buildOr ( (Or)current );
        }
        else if ( current instanceof Equal )
        {
            return buildEqual ( (Equal)current );
        }
        else if ( current instanceof Like )
        {
            return buildLike ( (Like)current );
        }
        else if ( current instanceof Not )
        {
            return buildNot ( (Not)current );
        }
        else if ( current instanceof IsNull )
        {
            return buildIsNull ( (IsNull)current );
        }

        throw new IllegalArgumentException ( String.format ( "Predicate type '%s' (%s) is unknown", current.getClass ().getSimpleName (), current.getClass ().getName () ) );
    }

    protected Function<ArtifactInformation, String> buildFrom ( final Value current )
    {
        if ( current instanceof Literal )
        {
            return buildLiteral ( (Literal)current );
        }
        else if ( current instanceof MetaKeyValue )
        {
            return buildMetaKeyValue ( (MetaKeyValue)current );
        }

        throw new IllegalArgumentException ( String.format ( "Value type '%s' (%s) is unknown", current.getClass ().getSimpleName (), current.getClass ().getName () ) );
    }

    protected Function<ArtifactInformation, String> buildLiteral ( final Literal current )
    {
        return t -> current.getValue ();
    }

    protected Function<ArtifactInformation, String> buildMetaKeyValue ( final MetaKeyValue current )
    {
        return t -> t.getMetaData ().get ( current.getMetaKey () );
    }

    protected java.util.function.Predicate<ArtifactInformation> buildAnd ( final And current )
    {
        return new AndPredicate<> ( current.getPredicates ().stream ().map ( this::buildFrom ).collect ( toList () ) );
    }

    protected java.util.function.Predicate<ArtifactInformation> buildOr ( final Or current )
    {
        return new OrPredicate<> ( current.getPredicates ().stream ().map ( this::buildFrom ).collect ( toList () ) );
    }

    protected java.util.function.Predicate<ArtifactInformation> buildEqual ( final Equal current )
    {
        return new EqualPredicate<> ( buildFrom ( current.getValue1 () ), buildFrom ( current.getValue2 () ) );
    }

    protected java.util.function.Predicate<ArtifactInformation> buildLike ( final Like current )
    {
        return new LikePredicate<> ( buildFrom ( current.getValue () ), current.getPattern ().getValue (), current.isCaseSensitive () );
    }

    protected java.util.function.Predicate<ArtifactInformation> buildNot ( final Not current )
    {
        // create the predicate first

        final java.util.function.Predicate<ArtifactInformation> p = buildFrom ( current.getPredicate () );

        /* putting the buildFrom call inside the lambda expression would be shorter, but would also
         * cause the conversion process to be called for each item later on, which is not what we want */

        return t -> !p.test ( t );
    }

    protected java.util.function.Predicate<ArtifactInformation> buildIsNull ( final IsNull current )
    {
        // create the value first

        final Function<ArtifactInformation, String> v1 = buildFrom ( current.getValue () );

        /* putting the buildFrom call inside the lambda expression would be shorter, but would also
         * cause the conversion process to be called for each item later on, which is not what we want */

        return t -> v1.apply ( t ) == null;
    }

}
