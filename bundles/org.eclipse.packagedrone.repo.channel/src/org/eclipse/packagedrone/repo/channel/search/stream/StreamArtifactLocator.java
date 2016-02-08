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
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.eclipse.packagedrone.repo.channel.search.ArtifactLocator;
import org.eclipse.packagedrone.repo.channel.search.Predicate;
import org.eclipse.packagedrone.repo.channel.search.SearchOptions;

public class StreamArtifactLocator implements ArtifactLocator
{
    private final Supplier<Stream<ArtifactInformation>> informationSupplier;

    public StreamArtifactLocator ( final Supplier<Stream<ArtifactInformation>> informationSupplier )
    {
        this.informationSupplier = informationSupplier;
    }

    @Override
    public void search ( final Predicate predicate, final SearchOptions options, final Consumer<Stream<ArtifactInformation>> consumer )
    {
        try ( Stream<ArtifactInformation> stream = this.informationSupplier.get () )
        {
            consumer.accept ( search ( stream, predicate, options ) );
        }
    }

    public static Stream<ArtifactInformation> search ( Stream<ArtifactInformation> stream, final Predicate predicate, SearchOptions options )
    {
        Objects.requireNonNull ( stream );

        options = options != null ? options : SearchOptions.DEFAULT_OPTIONS;

        // apply optional predicate

        if ( predicate != null )
        {
            final PredicateFilterBuilder fb = new PredicateFilterBuilder ( predicate );
            stream = stream.filter ( fb.build () );
        }

        // apply options

        return stream;
    }

}
