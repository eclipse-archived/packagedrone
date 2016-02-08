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

    public void search ( Predicate predicate, SearchOptions options, Consumer<Stream<ArtifactInformation>> consumer );
}
