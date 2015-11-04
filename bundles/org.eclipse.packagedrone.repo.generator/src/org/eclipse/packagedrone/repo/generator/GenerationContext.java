package org.eclipse.packagedrone.repo.generator;

import java.util.Collection;

import org.eclipse.packagedrone.repo.channel.ArtifactContext;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;

public interface GenerationContext extends ArtifactContext
{
    public Collection<ArtifactInformation> getChannelArtifacts ();
}
