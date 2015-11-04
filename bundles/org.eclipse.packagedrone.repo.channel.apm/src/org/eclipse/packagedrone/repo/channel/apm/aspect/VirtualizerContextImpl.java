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
package org.eclipse.packagedrone.repo.channel.apm.aspect;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.aspect.virtual.Virtualizer;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.eclipse.packagedrone.repo.channel.apm.aspect.AspectContextImpl.ArtifactCreator;
import org.eclipse.packagedrone.repo.generator.GenerationContext;
import org.eclipse.packagedrone.utils.Exceptions;

public class VirtualizerContextImpl implements Virtualizer.Context, GenerationContext
{
    private final String virtualizerAspectId;

    private final Path tmp;

    private final ArtifactInformation artifact;

    private final AspectableContext context;

    private final ArtifactCreator creator;

    private final ArtifactType type;

    public VirtualizerContextImpl ( final String virtualizerAspectId, final Path tmp, final ArtifactInformation artifact, final AspectableContext context, final ArtifactCreator creator, final ArtifactType type )
    {
        this.virtualizerAspectId = virtualizerAspectId;

        this.tmp = tmp;
        this.artifact = artifact;
        this.context = context;
        this.creator = creator;

        this.type = type;
    }

    @Override
    public Map<MetaKey, String> getProvidedChannelMetaData ()
    {
        return this.context.getChannelProvidedMetaData ();
    }

    @Override
    public ArtifactInformation getOtherArtifactInformation ( final String artifactId )
    {
        return this.context.getArtifacts ().get ( artifactId );
    }

    @Override
    public Collection<ArtifactInformation> getChannelArtifacts ()
    {
        return this.context.getArtifacts ().values ();
    }

    @Override
    public Path getFile ()
    {
        return this.tmp;
    }

    @Override
    public ArtifactInformation getArtifactInformation ()
    {
        return this.artifact;
    }

    @Override
    public void createVirtualArtifact ( final String name, final InputStream stream, final Map<MetaKey, String> providedMetaData )
    {
        Exceptions.wrapException ( () -> this.creator.internalCreateArtifact ( this.artifact.getId (), stream, name, providedMetaData, this.type, this.virtualizerAspectId ) );
    }
}
