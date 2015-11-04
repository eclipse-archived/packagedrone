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

import static org.eclipse.packagedrone.repo.adapter.maven.upload.Coordinates.makePath;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

import org.eclipse.packagedrone.repo.adapter.maven.upload.Coordinates;
import org.eclipse.packagedrone.repo.adapter.maven.upload.Options;
import org.eclipse.packagedrone.repo.adapter.maven.upload.Uploader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class UploadTest
{
    private Options options;

    @Before
    public void setup ()
    {
        this.options = new Options ();
    }

    @Test
    public void testBase ()
    {
        Assert.assertEquals ( "/a/b/c/test.id/1.0.0/test.id-1.0.0.jar", makePath ( "a.b.c", "test.id", "1.0.0", "1.0.0", null, "jar" ) );
        Assert.assertEquals ( "/a/b/c/test.id/1.0.0-SNAPSHOT/test.id-1.0.0-123-456-1.jar", makePath ( "a.b.c", "test.id", "1.0.0-SNAPSHOT", "1.0.0-123-456-1", null, "jar" ) );
    }

    @Test
    public void testCoordinates ()
    {
        assertCoordinates ( "/a/b/c/test.id/1.0.0/test.id-1.0.0.jar", "a.b.c", "test.id", "1.0.0", "1.0.0", null, "jar" );
        assertCoordinates ( "/a/b/c/test.id/1.0.0/test.id-1.0.0.pom", "a.b.c", "test.id", "1.0.0", "1.0.0", null, "pom" );
        assertCoordinates ( "/a/b/c/test.id/1.0.0.m1/test.id-1.0.0.m1.jar", "a.b.c", "test.id", "1.0.0.m1", "1.0.0.m1", null, "jar" );
        assertCoordinates ( "/a/b/c/test.id/1.0.0.m1/test.id-1.0.0.m1-sources.jar", "a.b.c", "test.id", "1.0.0.m1", "1.0.0.m1", "sources", "jar" );

        assertCoordinates ( "/a/b/c/test.id/1.0.0-SNAPSHOT/test.id-1.0.0-SNAPSHOT.jar", "a.b.c", "test.id", "1.0.0-SNAPSHOT", "1.0.0-SNAPSHOT", null, "jar" );
        assertCoordinates ( "/a/b/c/test.id/1.0.0-SNAPSHOT/test.id-1.0.0-SNAPSHOT.pom", "a.b.c", "test.id", "1.0.0-SNAPSHOT", "1.0.0-SNAPSHOT", null, "pom" );

        assertCoordinates ( "/a/b/c/test.id/1.0.0-SNAPSHOT/test.id-1.0.0-20150930.105040-1.jar", "a.b.c", "test.id", "1.0.0-SNAPSHOT", "1.0.0-20150930.105040-1", null, "jar" );
        assertCoordinates ( "/a/b/c/test.id/1.0.0-SNAPSHOT/test.id-1.0.0-20150930.105040-1.pom", "a.b.c", "test.id", "1.0.0-SNAPSHOT", "1.0.0-20150930.105040-1", null, "pom" );

        assertCoordinates ( "/a/b/c/test.id/1.0.0-SNAPSHOT/test.id-1.0.0-20150930.105040-1-sources.jar", "a.b.c", "test.id", "1.0.0-SNAPSHOT", "1.0.0-20150930.105040-1", "sources", "jar" );
        assertCoordinates ( "/a/b/c/test.id/1.0.0-SNAPSHOT/test.id-1.0.0-20150930.105040-1-sources.pom", "a.b.c", "test.id", "1.0.0-SNAPSHOT", "1.0.0-20150930.105040-1", "sources", "pom" );
    }

    private void assertCoordinates ( final String path, final String groupId, final String artifactId, final String version, final String qualifiedVersion, final String classifier, final String extension )
    {
        final Coordinates c = Coordinates.parse ( path );
        Assert.assertNotNull ( "Unable to parse coordinates: " + path, c );

        Assert.assertEquals ( groupId, c.getGroupId () );
        Assert.assertEquals ( artifactId, c.getArtifactId () );
        Assert.assertEquals ( version, c.getVersion () );
        Assert.assertEquals ( qualifiedVersion, c.getQualifiedVersion () );
        Assert.assertEquals ( classifier, c.getClassifier () );
        Assert.assertEquals ( extension, c.getExtension () );
    }

    @Test
    public void test1 () throws Exception
    {
        final String[] groups = new String[] { "group1.group2" };

        defaultTest ( groups, "artifact.id.1", "1.0.0" );
        defaultTest ( groups, "artifact.id.1", "1.0.0-M1" );
        defaultTest ( groups, "artifact.id.1", "1.0.0.1" );
    }

    @Test
    public void test1s () throws Exception
    {
        final String[] groups = new String[] { "group1.group2" };

        defaultTest ( groups, "artifact.id.1", "1.0.0-SNAPSHOT", "1.0.0-20150930.105040-1" );
    }

    @Test
    public void test2 () throws Exception
    {
        final String[] groups = new String[] { "a.1", "b.1", "c.1" };

        defaultTest ( groups, "artifact.id.1", "1.0.0" );
        defaultTest ( groups, "artifact.id.1", "1.0.0-M1" );
        defaultTest ( groups, "artifact.id.1", "1.0.0.1" );
    }

    @Test
    public void test2s () throws Exception
    {
        final String[] groups = new String[] { "a.1", "b.1", "c.1" };

        defaultTest ( groups, "artifact.id.1", "1.0.0-SNAPSHOT", "1.0.0-20150930.105040-1" );
    }

    protected void defaultTest ( final String[] groupIds, final String artifactId, final String version ) throws Exception
    {
        defaultTest ( groupIds, artifactId, version, version );
    }

    @SuppressWarnings ( "unused" )
    protected void defaultTest ( final String[] groupIds, final String artifactId, final String version, final String qualifiedVersion ) throws Exception
    {
        final MockUploadTarget target = new MockUploadTarget ();
        final Uploader uploader = new Uploader ( target, this.options );

        int pos = 1;
        for ( final String groupId : groupIds )
        {
            Collection<MockArtifact> arts;

            // .jar

            uploader.receive ( makePath ( groupId, artifactId, version, qualifiedVersion, null, "jar" ), null );
            uploader.receive ( makePath ( groupId, artifactId, version, qualifiedVersion, null, "jar.md5" ), fromString ( "d41d8cd98f00b204e9800998ecf8427e" ) );

            arts = target.getArtifact ( groupId, artifactId, version, qualifiedVersion, null, "jar" );
            Assert.assertEquals ( 1, arts.size () );
            final MockArtifact jarArt = arts.iterator ().next ();

            // .pom

            uploader.receive ( makePath ( groupId, artifactId, version, qualifiedVersion, null, "pom" ), null );
            uploader.receive ( makePath ( groupId, artifactId, version, qualifiedVersion, null, "pom.md5" ), fromString ( "d41d8cd98f00b204e9800998ecf8427e" ) );

            arts = target.getArtifact ( groupId, artifactId, version, qualifiedVersion, null, "pom" );
            Assert.assertEquals ( 1, arts.size () );
            final MockArtifact pomArt = arts.iterator ().next ();

            // -sources.jar

            uploader.receive ( makePath ( groupId, artifactId, version, qualifiedVersion, "sources", "jar" ), null );
            uploader.receive ( makePath ( groupId, artifactId, version, qualifiedVersion, "sources", "jar.md5" ), fromString ( "d41d8cd98f00b204e9800998ecf8427e" ) );

            arts = target.getArtifact ( groupId, artifactId, version, qualifiedVersion, "sources", "jar" );
            Assert.assertEquals ( 1, arts.size () );
            final MockArtifact sourceArt = arts.iterator ().next ();

            // meta data

            uploader.receive ( makeMetaData ( groupId, artifactId, null, null ), null );
            uploader.receive ( makeMetaData ( groupId, artifactId, null, ".md5" ), null );
            uploader.receive ( makeMetaData ( groupId, artifactId, null, ".sha1" ), null );

            uploader.receive ( makeMetaData ( groupId, artifactId, version, null ), null );
            uploader.receive ( makeMetaData ( groupId, artifactId, version, ".md5" ), null );
            uploader.receive ( makeMetaData ( groupId, artifactId, version, ".sha1" ), null );

            Assert.assertEquals ( 3 * pos, target.getArtifacts ().size () );

            // other validations

            Assert.assertEquals ( jarArt.getId (), sourceArt.getParentId () );

            pos++;
        }
    }

    private String makeMetaData ( final String groupId, final String artifactId, final String version, final String suffix )
    {
        final StringBuilder sb = new StringBuilder ();

        sb.append ( '/' ).append ( groupId.replace ( '.', '/' ) );
        sb.append ( '/' ).append ( artifactId );

        if ( version != null )
        {
            sb.append ( '/' ).append ( version );
        }

        sb.append ( "maven-metadata.xml" );

        if ( suffix != null )
        {
            sb.append ( suffix );
        }

        return sb.toString ();
    }

    private InputStream fromString ( final String string )
    {
        return new ByteArrayInputStream ( string.getBytes ( StandardCharsets.UTF_8 ) );
    }

}
