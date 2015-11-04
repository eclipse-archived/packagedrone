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
package org.eclipse.packagedrone.repo.adapter.maven;

import java.time.Instant;
import java.util.Collections;

import org.eclipse.packagedrone.repo.adapter.maven.ChannelData;
import org.eclipse.packagedrone.repo.adapter.maven.MavenInformation;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.junit.Test;

public class ChannelDataTest
{
    @Test
    public void testSerialize ()
    {
        final ChannelData cd = new ChannelData ();

        final MavenInformation info = new MavenInformation ();

        info.setGroupId ( "a.b.c" );
        info.setArtifactId ( "d" );
        info.setVersion ( "v" );

        final ArtifactInformation art = new ArtifactInformation ( "id", null, Collections.emptySet (), "name", 0L, Instant.now (), Collections.singleton ( "stored" ), Collections.emptyList (), Collections.emptyMap (), Collections.emptyMap (), "virtual" );
        cd.add ( info, art );

        System.out.println ( " == JSON == " );
        System.out.println ( cd );
    }

    @Test
    public void testFull ()
    {
        ChannelData cd = new ChannelData ();

        final MavenInformation info = new MavenInformation ();

        info.setGroupId ( "a.b.c" );
        info.setArtifactId ( "d" );
        info.setVersion ( "v" );

        final ArtifactInformation art = new ArtifactInformation ( "id", null, Collections.emptySet (), "name", 0L, Instant.now (), Collections.singleton ( "stored" ), Collections.emptyList (), Collections.emptyMap (), Collections.emptyMap (), "virtual" );
        cd.add ( info, art );

        cd = ChannelData.fromJson ( cd.toJson () );

        System.out.println ( " == FULL == " );
        System.out.println ( cd );
    }
}
