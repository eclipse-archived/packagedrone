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

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.eclipse.aether.util.artifact.JavaScopes.COMPILE;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.aether.RepositoryException;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.graph.DependencyVisitor;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.util.filter.DependencyFilterUtils;
import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.importer.ImportContext;
import org.eclipse.packagedrone.repo.importer.ImportSubContext;
import org.eclipse.packagedrone.repo.importer.Importer;
import org.eclipse.packagedrone.repo.importer.ImporterDescription;
import org.eclipse.packagedrone.repo.importer.SimpleImporterDescription;
import org.eclipse.packagedrone.repo.importer.aether.web.AetherResult;
import org.eclipse.packagedrone.web.LinkTarget;
import org.eclipse.scada.utils.io.RecursiveDeleteVisitor;

import com.google.gson.GsonBuilder;

public class AetherImporter implements Importer
{
    public static final String ID = "aether";

    private static final SimpleImporterDescription DESCRIPTION = new SimpleImporterDescription ();

    static
    {
        DESCRIPTION.setId ( ID );
        DESCRIPTION.setLabel ( "Maven 2 Importer" );
        DESCRIPTION.setDescription ( "Import artifacts from Maven Repositories using Eclipse Aether" );
        DESCRIPTION.setStartTarget ( new LinkTarget ( "/import/{token}/aether/start" ) );
    }

    private final GsonBuilder gsonBuilder;

    public AetherImporter ()
    {
        this.gsonBuilder = new GsonBuilder ();
    }

    @Override
    public ImporterDescription getDescription ()
    {
        return DESCRIPTION;
    }

    @Override
    public void runImport ( final ImportContext context, final String configuration ) throws Exception
    {
        final ImportConfiguration cfg = this.gsonBuilder.create ().fromJson ( configuration, ImportConfiguration.class );
        runImport ( context, cfg );
    }

    private void runImport ( final ImportContext context, final ImportConfiguration cfg ) throws Exception
    {
        final Path tmpDir = Files.createTempDirectory ( "aether" );

        context.addCleanupTask ( () -> {
            Files.walkFileTree ( tmpDir, new RecursiveDeleteVisitor () );
            Files.deleteIfExists ( tmpDir );
        } );

        final Collection<ArtifactResult> results = processImport ( tmpDir, cfg );

        final List<ArtifactResult> later = new LinkedList<> ();
        final Map<String, ImportSubContext> roots = new HashMap<> ();

        for ( final ArtifactResult result : results )
        {
            if ( !result.isResolved () )
            {
                continue;
            }

            importArtifact ( context, result, roots, later );
        }

        // try sub artifacts again

        for ( final ArtifactResult result : later )
        {
            importArtifact ( context, result, roots, null );
        }
    }

    private void importArtifact ( final ImportContext context, final ArtifactResult result, final Map<String, ImportSubContext> roots, final List<ArtifactResult> later )
    {
        final Artifact artifact = result.getArtifact ();

        final Map<MetaKey, String> metadata = makeMetaData ( artifact );

        final String key = makeRootKey ( artifact );

        if ( later != null && key != null && artifact.getClassifier () != null && !artifact.getClassifier ().isEmpty () )
        {
            final ImportSubContext sub = roots.get ( key );
            if ( sub == null )
            {
                later.add ( result );
            }
            else
            {
                sub.scheduleImport ( artifact.getFile ().toPath (), false, artifact.getFile ().getName (), metadata );
            }
        }
        else
        {
            final ImportSubContext sub = context.scheduleImport ( artifact.getFile ().toPath (), false, artifact.getFile ().getName (), metadata );
            if ( key != null )
            {
                roots.put ( key, sub );
            }
        }
    }

    private String makeRootKey ( final Artifact artifact )
    {
        if ( !"jar".equals ( artifact.getExtension () ) )
        {
            return null;
        }
        return String.format ( "%s:%s:%s", artifact.getGroupId (), artifact.getArtifactId (), artifact.getBaseVersion () );
    }

    private static Map<MetaKey, String> makeMetaData ( final Artifact artifact )
    {
        final Map<MetaKey, String> md = new HashMap<> ();

        md.put ( new MetaKey ( "mvn", "groupId" ), artifact.getGroupId () );
        md.put ( new MetaKey ( "mvn", "artifactId" ), artifact.getArtifactId () );
        md.put ( new MetaKey ( "mvn", "version" ), artifact.getVersion () );
        md.put ( new MetaKey ( "mvn", "extension" ), artifact.getExtension () );
        if ( artifact.getClassifier () != null )
        {
            md.put ( new MetaKey ( "mvn", "classifier" ), artifact.getClassifier () );
        }

        return md;
    }

    /**
     * Prepare an import with dependencies
     * <p>
     * This method does resolve even transient dependencies and also adds the
     * sources if requested
     * </p>
     */
    public static AetherResult prepareDependencies ( final Path tmpDir, final ImportConfiguration cfg ) throws RepositoryException
    {
        Objects.requireNonNull ( tmpDir );
        Objects.requireNonNull ( cfg );

        final RepositoryContext ctx = new RepositoryContext ( tmpDir, cfg.getRepositoryUrl (), cfg.isAllOptional () );

        // add all coordinates

        final CollectRequest cr = new CollectRequest ();
        cr.setRepositories ( ctx.getRepositories () );
        for ( final MavenCoordinates coords : cfg.getCoordinates () )
        {
            final Dependency dep = new Dependency ( new DefaultArtifact ( coords.toString () ), COMPILE );
            cr.addDependency ( dep );
        }

        final DependencyFilter filter = DependencyFilterUtils.classpathFilter ( COMPILE );
        final DependencyRequest deps = new DependencyRequest ( cr, filter );

        // resolve

        final DependencyResult dr = ctx.getSystem ().resolveDependencies ( ctx.getSession (), deps );
        final List<ArtifactResult> arts = dr.getArtifactResults ();

        if ( !cfg.isIncludeSources () )
        {
            // we are already done here
            return asResult ( arts, cfg, of ( dr ) );
        }

        // resolve sources

        final List<ArtifactRequest> requests = extendRequests ( arts.stream ().map ( ArtifactResult::getRequest ), ctx, cfg );

        return asResult ( resolve ( ctx, requests ), cfg, of ( dr ) );
    }

    /**
     * Prepare a plain import process
     * <p>
     * Prepare a simple import request with a specific list of coordinates
     * </p>
     */
    public static AetherResult preparePlain ( final Path tmpDir, final ImportConfiguration cfg ) throws ArtifactResolutionException
    {
        Objects.requireNonNull ( tmpDir );
        Objects.requireNonNull ( cfg );

        final RepositoryContext ctx = new RepositoryContext ( tmpDir, cfg.getRepositoryUrl (), cfg.isAllOptional () );

        // extend

        final List<ArtifactRequest> requests = extendRequests ( cfg.getCoordinates ().stream ().map ( c -> {
            final DefaultArtifact artifact = new DefaultArtifact ( c.toString () );
            return makeRequest ( ctx.getRepositories (), artifact );
        } ), ctx, cfg );

        // process

        return asResult ( resolve ( ctx, requests ), cfg, empty () );
    }

    private static List<ArtifactRequest> extendRequests ( final Stream<ArtifactRequest> requests, final RepositoryContext ctx, final ImportConfiguration cfg )
    {
        final List<ArtifactRequest> result = new LinkedList<> ();

        final Iterator<ArtifactRequest> i = requests.iterator ();
        while ( i.hasNext () )
        {
            final ArtifactRequest ar = i.next ();

            result.add ( ar );

            if ( cfg.isIncludeSources () )
            {
                final DefaultArtifact sources = makeOtherClassifier ( ar.getArtifact (), "sources" );
                if ( sources != null )
                {
                    result.add ( makeRequest ( ctx.getRepositories (), sources ) );
                }
            }

            if ( cfg.isIncludeJavadoc () )
            {
                final DefaultArtifact javadoc = makeOtherClassifier ( ar.getArtifact (), "javadoc" );
                if ( javadoc != null )
                {
                    result.add ( makeRequest ( ctx.getRepositories (), javadoc ) );
                }
            }

            if ( cfg.isIncludePoms () )
            {
                final DefaultArtifact pom = makeOtherExtension ( ar.getArtifact (), "pom" );
                if ( pom != null )
                {
                    result.add ( makeRequest ( ctx.getRepositories (), pom ) );
                }
            }
        }

        return result;
    }

    protected static List<ArtifactResult> resolve ( final RepositoryContext ctx, final List<ArtifactRequest> requests )
    {
        try
        {
            return ctx.getSystem ().resolveArtifacts ( ctx.getSession (), requests );
        }
        catch ( final ArtifactResolutionException e )
        {
            return e.getResults ();
        }
    }

    /**
     * Process the actual import request
     * <p>
     * This method takes the import configuration as is and simply tries to
     * import it. Not manipulating the list of coordinates any more
     * </p>
     */
    public static Collection<ArtifactResult> processImport ( final Path tmpDir, final ImportConfiguration cfg ) throws ArtifactResolutionException
    {
        Objects.requireNonNull ( tmpDir );
        Objects.requireNonNull ( cfg );

        final RepositoryContext ctx = new RepositoryContext ( tmpDir, cfg.getRepositoryUrl () );

        final Collection<ArtifactRequest> requests = new LinkedList<> ();

        for ( final MavenCoordinates coords : cfg.getCoordinates () )
        {
            // main artifact

            final DefaultArtifact main = new DefaultArtifact ( coords.toString () );
            requests.add ( makeRequest ( ctx.getRepositories (), main ) );
        }

        // process

        return ctx.getSystem ().resolveArtifacts ( ctx.getSession (), requests );
    }

    /**
     * Convert aether result list to AetherResult object
     *
     * @param results
     *            the result collection
     * @param cfg
     *            the import configuration
     * @param dependencyResult
     *            The result of the dependency resolution
     * @return the AetherResult object
     */
    public static AetherResult asResult ( final Collection<ArtifactResult> results, final ImportConfiguration cfg, final Optional<DependencyResult> dependencyResult )
    {
        final AetherResult result = new AetherResult ();

        // create set of requested coordinates

        final Set<String> requested = new HashSet<> ( cfg.getCoordinates ().size () );
        for ( final MavenCoordinates mc : cfg.getCoordinates () )
        {
            requested.add ( mc.toString () );
        }

        // generate dependency map

        final Map<String, Boolean> optionalDeps = new HashMap<> ();
        fillOptionalDependenciesMap ( dependencyResult, optionalDeps );

        // convert artifacts

        for ( final ArtifactResult ar : results )
        {
            final AetherResult.Entry entry = new AetherResult.Entry ();

            final MavenCoordinates coordinates = MavenCoordinates.fromResult ( ar );
            final String key = coordinates.toBase ().toString ();

            entry.setCoordinates ( coordinates );
            entry.setResolved ( ar.isResolved () );
            entry.setRequested ( requested.contains ( key ) );
            entry.setOptional ( optionalDeps.getOrDefault ( key, Boolean.FALSE ) );

            // convert error

            if ( ar.getExceptions () != null && !ar.getExceptions ().isEmpty () )
            {
                final StringBuilder sb = new StringBuilder ( ar.getExceptions ().get ( 0 ).getMessage () );
                if ( ar.getExceptions ().size () > 1 )
                {
                    sb.append ( " ..." );
                }
                entry.setError ( sb.toString () );
            }

            // add to list

            result.getArtifacts ().add ( entry );
        }

        // sort by coordinates

        Collections.sort ( result.getArtifacts (), Comparator.comparing ( AetherResult.Entry::getCoordinates ) );

        // set repo url

        result.setRepositoryUrl ( cfg.getRepositoryUrl () );

        return result;
    }

    private static void fillOptionalDependenciesMap ( final Optional<DependencyResult> dependencyResult, final Map<String, Boolean> optionalDeps )
    {
        if ( !dependencyResult.isPresent () )
        {
            return;
        }

        dependencyResult.get ().getRoot ().accept ( new DependencyVisitor () {

            @Override
            public boolean visitLeave ( final DependencyNode node )
            {
                return true;
            }

            @Override
            public boolean visitEnter ( final DependencyNode node )
            {
                final Dependency d = node.getDependency ();
                if ( d == null )
                {
                    return true;
                }

                final String key = MavenCoordinates.fromArtifact ( d.getArtifact () ).toBase ().toString ();

                if ( d.isOptional () )
                {
                    if ( !optionalDeps.containsKey ( key ) )
                    {
                        optionalDeps.put ( key, Boolean.TRUE );
                    }
                }
                else
                {
                    optionalDeps.put ( key, Boolean.FALSE );
                }
                return true;
            }
        } );
    }

    private static DefaultArtifact makeOtherClassifier ( final Artifact main, final String classifier )
    {
        if ( main.getClassifier () != null && !main.getClassifier ().isEmpty () )
        {
            // we only change main artifacts
            return null;
        }

        return new DefaultArtifact ( main.getGroupId (), main.getArtifactId (), classifier, main.getExtension (), main.getVersion () );
    }

    private static DefaultArtifact makeOtherExtension ( final Artifact main, final String extension )
    {
        if ( main.getClassifier () != null && !main.getClassifier ().isEmpty () )
        {
            // we only change main artifacts
            return null;
        }

        return new DefaultArtifact ( main.getGroupId (), main.getArtifactId (), null, extension, main.getVersion () );
    }

    private static ArtifactRequest makeRequest ( final List<RemoteRepository> repositories, final Artifact artifact )
    {
        final ArtifactRequest artifactRequest = new ArtifactRequest ();
        artifactRequest.setArtifact ( artifact );
        artifactRequest.setRepositories ( repositories );
        return artifactRequest;
    }
}
