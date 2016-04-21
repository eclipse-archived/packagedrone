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

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.eclipse.packagedrone.repo.channel.search.ArtifactLocator;
import org.eclipse.packagedrone.repo.channel.search.Predicate;
import org.eclipse.packagedrone.repo.channel.search.SearchOptions;

public class StreamArtifactLocator implements ArtifactLocator
{
    private final Supplier<Stream<? extends ArtifactInformation>> informationSupplier;

    public StreamArtifactLocator ( final Supplier<Stream<? extends ArtifactInformation>> informationSupplier )
    {
        this.informationSupplier = informationSupplier;
    }

    @Override
    public <R> R process ( final Predicate predicate, final SearchOptions options, final Function<Stream<? extends ArtifactInformation>, R> function )
    {
        try ( Stream<? extends ArtifactInformation> stream = this.informationSupplier.get () )
        {
            return function.apply ( search ( stream, predicate, options ) );
        }
    }

    public static <T extends ArtifactInformation> Stream<T> search ( Stream<T> stream, final Predicate predicate, SearchOptions options )
    {
        Objects.requireNonNull ( stream );

        options = options != null ? options : SearchOptions.DEFAULT_OPTIONS;

        // apply optional predicate

        if ( predicate != null )
        {
            stream = stream.filter ( compilePredicate ( predicate ) );
        }

        // apply options

        stream = applyOptions ( stream, options );

        // return

        return stream;
    }

    public static java.util.function.Predicate<ArtifactInformation> compilePredicate ( final Predicate predicate )
    {
        Objects.requireNonNull ( predicate );
        return new PredicateFilterBuilder ( predicate ).build ();
    }

    private static <T extends ArtifactInformation> Stream<T> applyOptions ( Stream<T> stream, final SearchOptions options )
    {
        if ( options.getSkip () > 0 )
        {
            stream = stream.skip ( options.getSkip () );
        }
        if ( options.getLimit () > 0 )
        {
            stream = stream.limit ( options.getLimit () );
        }
        return stream;
    }

}
