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
package org.eclipse.packagedrone.repo.importer.aether;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.collection.DependencySelector;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.util.graph.selector.AndDependencySelector;
import org.eclipse.aether.util.graph.selector.ExclusionDependencySelector;
import org.eclipse.aether.util.graph.selector.OptionalDependencySelector;
import org.eclipse.aether.util.graph.selector.ScopeDependencySelector;

public class RepositoryContext
{
    private final RepositorySystem system;

    private final DefaultRepositorySystemSession session;

    private final List<RemoteRepository> repositories;

    public RepositoryContext ( final Path tmpDir, final String repositoryUrl )
    {
        this ( tmpDir, repositoryUrl, null );
    }

    public RepositoryContext ( final Path tmpDir, final String repositoryUrl, final Boolean allOptional )
    {
        this.system = Helper.newRepositorySystem ();
        this.session = Helper.newRepositorySystemSession ( tmpDir, this.system );

        if ( allOptional != null )
        {
            final List<DependencySelector> selectors = new LinkedList<> ();

            selectors.add ( new ScopeDependencySelector ( "test", "provided" ) );
            if ( !allOptional )
            {
                selectors.add ( new OptionalDependencySelector () );
            }
            selectors.add ( new ExclusionDependencySelector () );
            this.session.setDependencySelector ( new AndDependencySelector ( selectors ) );
        }

        if ( repositoryUrl == null || repositoryUrl.isEmpty () )
        {
            this.repositories = Collections.singletonList ( Helper.newCentralRepository () );
        }
        else
        {
            this.repositories = Collections.singletonList ( Helper.newRemoteRepository ( "drone.aether.import", repositoryUrl ) );
        }
    }

    public List<RemoteRepository> getRepositories ()
    {
        return this.repositories;
    }

    public RepositorySystemSession getSession ()
    {
        return this.session;
    }

    public RepositorySystem getSystem ()
    {
        return this.system;
    }
}
