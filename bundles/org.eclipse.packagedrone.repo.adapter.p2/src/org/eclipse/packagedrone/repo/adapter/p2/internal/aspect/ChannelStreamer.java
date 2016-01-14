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
package org.eclipse.packagedrone.repo.adapter.p2.internal.aspect;

import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.MetaKeys;
import org.eclipse.packagedrone.repo.adapter.p2.P2ChannelInformation;
import org.eclipse.packagedrone.repo.adapter.p2.aspect.P2RepoConstants;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;

public class ChannelStreamer
{
    private final static Logger logger = LoggerFactory.getLogger ( ChannelStreamer.class );

    private final boolean writeCompressed;

    private final boolean writePlain;

    private final Instant now;

    private final Map<String, String> additionalProperties;

    private final String title;

    private final List<String> artifactsFragments = new LinkedList<> ();

    private long artifactsCounter = 0;

    private final List<String> metaDataFragments = new LinkedList<> ();

    private long metaDataCounter = 0;

    private Map<String, String> checksums = new HashMap<> (); // key -> md5

    private HashMultimap<String, String> checksumArtifacts = HashMultimap.create (); // key -> artifact ids

    private final Set<String> checksumErrors = new HashSet<> (); // keys

    public ChannelStreamer ( final String title, final Map<MetaKey, String> channelMetaData, final boolean writeCompressed, final boolean writePlain )
    {
        this.now = Instant.now ();
        this.writeCompressed = writeCompressed;
        this.writePlain = writePlain;

        this.title = makeTitle ( title, channelMetaData );

        this.additionalProperties = new HashMap<> ();

        final P2ChannelInformation p2ChannelInformation = new P2ChannelInformation ();
        try
        {
            MetaKeys.bind ( p2ChannelInformation, channelMetaData );
        }
        catch ( final Exception e1 )
        {
            // run with defaults
        }

        if ( p2ChannelInformation.getMirrorsUrl () != null )
        {
            this.additionalProperties.put ( "p2.mirrorsURL", p2ChannelInformation.getMirrorsUrl () );
        }
        if ( p2ChannelInformation.getStatisticsUrl () != null )
        {
            // yes, the property is URI compared to the previous URL
            this.additionalProperties.put ( "p2.statsURI", p2ChannelInformation.getStatisticsUrl () );
        }
    }

    public static String makeTitle ( final String channelTitle, final Map<MetaKey, String> channelMetaData )
    {
        final String title = channelMetaData.get ( P2RepoConstants.KEY_REPO_TITLE );
        if ( title != null && !title.isEmpty () )
        {
            return title;
        }

        return String.format ( "Package Drone Channel: %s", channelTitle );
    }

    public void stream ( final Collection<ArtifactInformation> artifacts, final ArtifactStreamer streamer )
    {
        this.checksums = new HashMap<> ( artifacts.size () );
        this.checksumArtifacts = HashMultimap.create ( artifacts.size (), 1 );

        for ( final ArtifactInformation artifact : artifacts )
        {
            process ( artifact, streamer );
        }
    }

    public void process ( final ArtifactInformation artifact, final ArtifactStreamer streamer )
    {
        final String type = artifact.getMetaData ().get ( P2RepoConstants.KEY_FRAGMENT_TYPE );

        if ( type == null )
        {
            return;
        }

        if ( "metadata".equals ( type ) )
        {
            fastTrackMetaData ( artifact, type );
        }
        else if ( "artifacts".equals ( type ) )
        {
            fastTrackArtifacts ( artifact, type );
        }
    }

    private boolean fastTrackArtifacts ( final ArtifactInformation artifact, final String type )
    {
        try
        {
            final String dataString = artifact.getMetaData ().get ( P2RepoConstants.KEY_FRAGMENT_DATA );
            final String keysString = artifact.getMetaData ().get ( P2RepoConstants.KEY_FRAGMENT_KEYS );
            final String sumsString = artifact.getMetaData ().get ( P2RepoConstants.KEY_FRAGMENT_MD5 );

            if ( dataString == null || keysString == null || sumsString == null )
            {
                return false;
            }

            final String[] keys = keysString.split ( P2RepoConstants.ENTRY_DELIMITER, -1 );
            final String[] sums = sumsString.split ( P2RepoConstants.ENTRY_DELIMITER, -1 );
            final String[] data = dataString.split ( P2RepoConstants.ENTRY_DELIMITER, -1 );

            if ( keys.length != sums.length || keys.length != data.length )
            {
                return false;
            }

            for ( int i = 0; i < keys.length; i++ )
            {
                final String sum = sums[i];
                final boolean hasSum = sum != null && !sum.isEmpty ();

                final String old;

                if ( hasSum )
                {
                    old = this.checksums.put ( keys[i], sums[i] );
                }
                else
                {
                    old = null;
                }

                // now process

                if ( old == null || old.equals ( sums[i] ) )
                {
                    // not yet present - add to repo
                    this.artifactsCounter += 1;
                    this.artifactsFragments.add ( data[i] );
                }
                else
                {
                    this.checksumErrors.add ( keys[i] );
                }

                if ( hasSum )
                {
                    // record source of artifact
                    this.checksumArtifacts.put ( keys[i], artifact.getId () );
                }
            }

            return true;
        }
        catch ( final Exception e )
        {
            logger.info ( "Failed to process artifact", e );
            return false;
        }
    }

    private boolean fastTrackMetaData ( final ArtifactInformation artifact, final String type )
    {
        try
        {
            final int count = Integer.parseInt ( artifact.getMetaData ().get ( P2RepoConstants.KEY_FRAGMENT_COUNT ) );
            final String data = artifact.getMetaData ().get ( P2RepoConstants.KEY_FRAGMENT_DATA );

            if ( data == null )
            {
                return false;
            }

            this.metaDataCounter += count;
            this.metaDataFragments.add ( data );

            return true;
        }
        catch ( final Exception e )
        {
            logger.info ( "Failed to process metadat", e );
            return false;
        }
    }

    public void spoolOut ( final SpoolOutHandler handler ) throws IOException
    {
        if ( this.writeCompressed )
        {
            spoolOut ( handler, new MetaDataWriter ( this.metaDataFragments, this.metaDataCounter, this.title, this.now, this.additionalProperties, true ) );
            spoolOut ( handler, new ArtifactsWriter ( this.artifactsFragments, this.artifactsCounter, this.title, this.now, this.additionalProperties, true ) );
        }
        if ( this.writePlain )
        {
            spoolOut ( handler, new MetaDataWriter ( this.metaDataFragments, this.metaDataCounter, this.title, this.now, this.additionalProperties, false ) );
            spoolOut ( handler, new ArtifactsWriter ( this.artifactsFragments, this.artifactsCounter, this.title, this.now, this.additionalProperties, false ) );
        }
    }

    private void spoolOut ( final SpoolOutHandler handler, final AbstractWriter writer ) throws IOException
    {
        handler.spoolOut ( writer.getId (), writer.getId (), writer.getMimeType (), stream -> {
            writer.write ( stream );
        } );
    }

    public Map<String, Set<String>> checkDuplicates ()
    {
        final Map<String, Set<String>> result = new HashMap<> ();

        for ( final String errorKey : this.checksumErrors )
        {
            result.put ( errorKey, this.checksumArtifacts.get ( errorKey ) );
        }

        return result;
    }

}
