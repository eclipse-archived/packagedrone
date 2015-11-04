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
package org.eclipse.packagedrone.repo.adapter.unzip;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.adapter.unzip.MavenVersionedArtifact;
import org.eclipse.packagedrone.repo.adapter.unzip.UnzipServlet;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.eclipse.packagedrone.repo.channel.ChannelId;
import org.eclipse.scada.utils.str.Tables;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class MavenTest
{
    private static List<ArtifactInformation> list1;

    @BeforeClass
    public static void setup ()
    {
        final String channelId = "channel1";

        list1 = new LinkedList<> ();
        for ( int major = 0; major < 3; major++ )
        {
            for ( int minor = 0; minor < 3; minor++ )
            {
                for ( int micro = 0; micro < 3; micro++ )
                {
                    final String base = String.format ( "%s.%s.%s", major + 1, minor + 1, micro + 1 );
                    for ( int i = 0; i < 3; i++ )
                    {
                        list1.add ( MockArtifact.maven ( channelId, "group.id", "artifact.id", base, "zip", String.format ( "201501%02d.101010-1", i + 1 ) ) );
                    }

                    for ( int rc = 0; rc < 3; rc++ )
                    {
                        list1.add ( MockArtifact.maven ( channelId, "group.id", "artifact.id", base + "-RC" + ( rc + 1 ), "zip", null ) );
                    }

                    list1.add ( MockArtifact.maven ( channelId, "group.id", "artifact.id", base, "zip", null ) );
                }
            }
        }

        dumpList ( "List 1", list1 );
    }

    protected static void dumpList ( final String header, final List<ArtifactInformation> list )
    {
        System.out.println ( "\t" + header );
        final List<List<String>> data = new LinkedList<> ();

        for ( final ArtifactInformation art : list )
        {
            final List<String> row = new LinkedList<> ();

            row.add ( art.getName () );

            data.add ( row );
        }

        Tables.showTable ( System.out, Arrays.asList ( "Name" ), data, 2 );
        System.out.println ();
    }

    protected static void dumpMavenList ( final String header, final List<MavenVersionedArtifact> list )
    {
        dumpList ( header, list.stream ().map ( MavenVersionedArtifact::getArtifact ).collect ( Collectors.toList () ) );
    }

    private final Supplier<Collection<ArtifactInformation>> test1 = () -> list1;

    private void assertResult ( final List<MavenVersionedArtifact> result, final String version )
    {
        Assert.assertEquals ( 1, result.size () );
        final MavenVersionedArtifact art = result.get ( 0 );

        final ArtifactInformation ai = art.getArtifact ();
        final String sv = ai.getMetaData ().get ( new MetaKey ( "mvn", "snapshotVersion" ) );
        final String v = ai.getMetaData ().get ( new MetaKey ( "mvn", "version" ) );

        if ( sv != null )
        {
            Assert.assertEquals ( version, sv );
        }
        else
        {
            Assert.assertEquals ( version, v );
        }
    }

    @Test
    public void testLatest () throws IOException
    {
        final List<MavenVersionedArtifact> result = new LinkedList<> ();
        UnzipServlet.handleMavenLatest ( this.test1, new ChannelId ( "forTesting" ), path ( "group.id/artifact.id" ), false, result::add );

        dumpMavenList ( "Result - latest", result );

        assertResult ( result, "3.3.3" );
    }

    @Test
    public void testLatestSnapshot () throws IOException
    {
        final List<MavenVersionedArtifact> result = new LinkedList<> ();
        UnzipServlet.handleMavenLatest ( this.test1, new ChannelId ( "forTesting" ), path ( "group.id/artifact.id" ), true, result::add );

        dumpMavenList ( "Result - latest-SNAPSHOT", result );

        assertResult ( result, "3.3.3-20150103.101010-1" );
    }

    @Test
    public void testPerfect () throws IOException
    {
        final List<MavenVersionedArtifact> result = new LinkedList<> ();
        UnzipServlet.handleMavenPerfect ( this.test1, new ChannelId ( "forTesting" ), path ( "group.id/artifact.id/2.3.1-RC1" ), result::add );

        dumpMavenList ( "Result - perfect", result );

        assertResult ( result, "2.3.1-RC1" );
    }

    @Test
    public void testPerfectSnapshot () throws IOException
    {
        final List<MavenVersionedArtifact> result = new LinkedList<> ();
        UnzipServlet.handleMavenPerfect ( this.test1, new ChannelId ( "forTesting" ), path ( "group.id/artifact.id/2.1.1-SNAPSHOT" ), result::add );

        dumpMavenList ( "Result - perfect - snapshot", result );

        assertResult ( result, "2.1.1-20150103.101010-1" );
    }

    @Test
    public void testPerfectSnapshot2 () throws IOException
    {
        final List<MavenVersionedArtifact> result = new LinkedList<> ();
        UnzipServlet.handleMavenPerfect ( this.test1, new ChannelId ( "forTesting" ), path ( "group.id/artifact.id/2.3.1-20150102.101010-1" ), result::add );

        dumpMavenList ( "Result - perfect - snapshot 2", result );

        assertResult ( result, "2.3.1-20150102.101010-1" );
    }

    @Test
    public void testPrefixed1 () throws IOException
    {
        final List<MavenVersionedArtifact> result = new LinkedList<> ();
        UnzipServlet.handleMavenPrefixed ( this.test1, new ChannelId ( "forTesting" ), path ( "group.id/artifact.id/2.x" ), result::add );

        dumpMavenList ( "Result - prefixed 2.x", result );

        assertResult ( result, "2.3.3" );
    }

    @Test
    public void testPrefixed2 () throws IOException
    {
        final List<MavenVersionedArtifact> result = new LinkedList<> ();
        UnzipServlet.handleMavenPrefixed ( this.test1, new ChannelId ( "forTesting" ), path ( "group.id/artifact.id/2.2.x" ), result::add );

        dumpMavenList ( "Result - prefixed 2.2.x", result );

        assertResult ( result, "2.2.3" );
    }

    @Test
    public void testPrefixed1Snapshot () throws IOException
    {
        final List<MavenVersionedArtifact> result = new LinkedList<> ();
        UnzipServlet.handleMavenPrefixed ( this.test1, new ChannelId ( "forTesting" ), path ( "group.id/artifact.id/2.x-SNAPSHOT" ), result::add );

        dumpMavenList ( "Result - prefixed 2.x", result );

        assertResult ( result, "2.3.3-20150103.101010-1" );
    }

    @Test
    public void testPrefixed2Snapshot () throws IOException
    {
        final List<MavenVersionedArtifact> result = new LinkedList<> ();
        UnzipServlet.handleMavenPrefixed ( this.test1, new ChannelId ( "forTesting" ), path ( "group.id/artifact.id/2.2.x-SNAPSHOT" ), result::add );

        dumpMavenList ( "Result - prefixed 2.2.x", result );

        assertResult ( result, "2.2.3-20150103.101010-1" );
    }

    private LinkedList<String> path ( final String string )
    {
        return new LinkedList<> ( Arrays.asList ( string.split ( "/" ) ) );
    }
}
