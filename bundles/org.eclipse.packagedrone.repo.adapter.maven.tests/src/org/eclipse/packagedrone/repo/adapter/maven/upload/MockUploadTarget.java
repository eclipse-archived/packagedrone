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
package org.eclipse.packagedrone.repo.adapter.maven.upload;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.adapter.maven.upload.ChecksumValidationException;
import org.eclipse.packagedrone.repo.adapter.maven.upload.Coordinates;
import org.eclipse.packagedrone.repo.adapter.maven.upload.UploadTarget;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class MockUploadTarget implements UploadTarget
{
    private final Multimap<Coordinates, MockArtifact> artifacts = HashMultimap.create ();

    public Multimap<Coordinates, MockArtifact> getArtifacts ()
    {
        return this.artifacts;
    }

    public Collection<MockArtifact> getArtifact ( final String groupId, final String artifactId, final String version, final String qualifiedVersion, final String classifier, final String extension )
    {
        final Coordinates c = new Coordinates ( groupId, artifactId, version, qualifiedVersion, classifier, extension );

        return this.artifacts.get ( c );
    }

    @Override
    public String createArtifact ( final String parentId, final Coordinates coordinates, final InputStream stream, final Map<MetaKey, String> metaData ) throws IOException
    {
        final MockArtifact art = new MockArtifact ( UUID.randomUUID ().toString (), parentId, coordinates.toFileName (), stream, metaData );

        this.artifacts.put ( coordinates, art );

        return art.getId ();
    }

    @Override
    public Set<String> findArtifacts ( final Coordinates coordinates )
    {
        return this.artifacts.get ( coordinates ).stream ().map ( MockArtifact::getId ).collect ( Collectors.toSet () );
    }

    @Override
    public void validateChecksum ( final Coordinates coordinates, final String algorithm, final String value ) throws ChecksumValidationException
    {
        final Collection<MockArtifact> arts = this.artifacts.get ( coordinates );

        if ( arts.isEmpty () )
        {
            throw new ChecksumValidationException ( "Artifact not found: " + coordinates );
        }

        if ( arts.size () != 1 )
        {
            return;
        }

        final MockArtifact art = arts.iterator ().next ();
        final String current = art.calcChecksum ( algorithm );

        if ( current == null )
        {
            System.out.println ( "No algorithm: " + algorithm );

            // ignore
            return;
        }

        if ( !value.equalsIgnoreCase ( current ) )
        {
            throw new ChecksumValidationException ( String.format ( "%s: invalid checksum - expected: %s, actual: %s", coordinates, value, current ) );
        }
    }

}
