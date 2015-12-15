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
package org.eclipse.packagedrone.repo.channel.provider;

import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;

public interface ModifyContext extends AccessContext
{
    public void applyMetaData ( Map<MetaKey, String> changes );

    public void applyMetaData ( String artifactId, Map<MetaKey, String> changes );

    public void lock ();

    public void unlock ();

    public ArtifactInformation createArtifact ( InputStream source, String name, Map<MetaKey, String> providedMetaData );

    public ArtifactInformation createArtifact ( String parentId, InputStream source, String name, Map<MetaKey, String> providedMetaData );

    public ArtifactInformation createGeneratorArtifact ( String generatorId, InputStream source, String name, Map<MetaKey, String> providedMetaData );

    public boolean deleteArtifact ( String artifactId );

    public void clear ();

    public void addAspects ( Set<String> aspectIds );

    public void removeAspects ( Set<String> aspectIds );

    public void refreshAspects ( Set<String> aspectIds );

    public void regenerate ( String artifactId );
}
