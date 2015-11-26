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
package org.eclipse.packagedrone.testing.server.channel;

import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.deployment.DeployRequest;
import org.eclipse.aether.repository.Authentication;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.util.artifact.SubArtifact;
import org.eclipse.aether.util.repository.AuthenticationBuilder;
import org.eclipse.packagedrone.testing.server.AbstractServerTest;
import org.eclipse.packagedrone.testing.server.maven.MavenUtil;
import org.junit.Test;

public class MavenTest extends AbstractServerTest
{
    @Test
    public void testMvn1 () throws Exception
    {
        final ChannelTester ct = ChannelTester.create ( getWebContext (), "m1" );
        ct.addAspect ( "mvn" );
        ct.addAspect ( "maven.repo" );
        ct.assignDeployGroup ( "m1" );

        final String key = ct.getDeployKeys ().iterator ().next (); // get first

        assertNotNull ( key );

        final RepositorySystem system = MavenUtil.newRepositorySystem ();
        final RepositorySystemSession session = MavenUtil.newRepositorySystemSession ( system );

        Artifact jarArtifact = new DefaultArtifact ( "de.dentrassi", "test.bundle1", "", "jar", "1.0.0-SNAPSHOT" );
        jarArtifact = jarArtifact.setFile ( new File ( "data/mvn/test.bundle1-1.0.0-SNAPSHOT.jar" ) );

        Artifact pomArtifact = new SubArtifact ( jarArtifact, "", "pom" );
        pomArtifact = pomArtifact.setFile ( new File ( "data/mvn/test.bundle1-1.0.0-SNAPSHOT.pom" ) );

        Artifact srcArtifact = new SubArtifact ( jarArtifact, "sources", "jar" );
        srcArtifact = srcArtifact.setFile ( new File ( "data/mvn/test.bundle1-1.0.0-SNAPSHOT-sources.jar" ) );

        final AuthenticationBuilder ab = new AuthenticationBuilder ();
        ab.addUsername ( "deploy" );
        ab.addPassword ( key );
        final Authentication auth = ab.build ();
        final RemoteRepository distRepo = new RemoteRepository.Builder ( "test", "default", resolve ( String.format ( "/maven/%s", ct.getId () ) ) ).setAuthentication ( auth ).build ();

        final DeployRequest deployRequest = new DeployRequest ();
        deployRequest.addArtifact ( jarArtifact ).addArtifact ( pomArtifact ).addArtifact ( srcArtifact );
        deployRequest.setRepository ( distRepo );

        system.deploy ( session, deployRequest );

        testUrl ( String.format ( "/maven/%s", ct.getId () ) ); // index page

        // FIXME: check more data
    }
}
