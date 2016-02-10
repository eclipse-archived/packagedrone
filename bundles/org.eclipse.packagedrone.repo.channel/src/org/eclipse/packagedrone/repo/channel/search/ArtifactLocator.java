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
package org.eclipse.packagedrone.repo.channel.search;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.packagedrone.repo.channel.ArtifactInformation;

public interface ArtifactLocator
{
    public default List<ArtifactInformation> search ( final Predicate predicate )
    {
        return search ( predicate, SearchOptions.DEFAULT_OPTIONS );
    }

    public default List<ArtifactInformation> search ( final Predicate predicate, final SearchOptions options )
    {
        final List<ArtifactInformation> result = new LinkedList<> ();
        search ( predicate, options, stream -> stream.forEach ( result::add ) );
        return result;
    }

    public default void search ( final Predicate predicate, final Consumer<Stream<ArtifactInformation>> consumer )
    {
        search ( predicate, SearchOptions.DEFAULT_OPTIONS, consumer );
    }

    public default void search ( final Predicate predicate, final SearchOptions options, final Consumer<Stream<ArtifactInformation>> consumer )
    {
        process ( predicate, options, stream -> {
            consumer.accept ( stream );
            return null;
        } );
    }

    public default <R> R process ( final Predicate predicate, final Function<Stream<ArtifactInformation>, R> function )
    {
        return process ( predicate, SearchOptions.DEFAULT_OPTIONS, function );
    }

    public <R> R process ( final Predicate predicate, final SearchOptions options, final Function<Stream<ArtifactInformation>, R> function );
}
