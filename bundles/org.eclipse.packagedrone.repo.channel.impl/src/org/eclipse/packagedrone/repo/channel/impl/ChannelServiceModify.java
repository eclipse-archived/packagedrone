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
package org.eclipse.packagedrone.repo.channel.impl;

import static java.util.stream.Collectors.toSet;
import static org.eclipse.packagedrone.repo.channel.ChannelService.NAME_PATTERN;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.packagedrone.repo.channel.deploy.DeployGroup;
import org.eclipse.packagedrone.repo.channel.deploy.DeployKey;
import org.eclipse.packagedrone.repo.channel.impl.model.ChannelConfiguration;
import org.eclipse.packagedrone.repo.utils.Tokens;
import org.eclipse.packagedrone.utils.Holder;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

public class ChannelServiceModify implements ChannelServiceAccess
{
    private final ChannelServiceModel model;

    private final Multimap<String, String> idToNameMap;

    private final Map<String, String> nameToIdMap;

    /**
     * A map holding the deploy groups
     */
    private final Map<String, DeployGroup> deployGroups;

    /**
     * A map holding the deploy keys
     */
    private final Map<String, DeployKey> deployKeys;

    /**
     * A map holding the channel configuration information
     */
    private final Map<String, ChannelConfiguration> channels;

    public ChannelServiceModify ( final ChannelServiceModel model )
    {
        this.model = new ChannelServiceModel ( model );

        this.nameToIdMap = new HashMap<> ();

        this.idToNameMap = LinkedHashMultimap.create ();
        for ( final Map.Entry<String, List<String>> entry : model.getNameMap ().entrySet () )
        {
            this.idToNameMap.putAll ( entry.getKey (), entry.getValue () );
            entry.getValue ().stream ().forEach ( name -> this.nameToIdMap.put ( name, entry.getKey () ) );
        }

        this.deployGroups = model.getDeployGroups ().stream ().collect ( Collectors.toMap ( DeployGroup::getId, i -> i ) );
        this.deployKeys = model.getDeployGroups ().stream ().flatMap ( group -> group.getKeys ().stream () ).collect ( Collectors.toMap ( DeployKey::getId, i -> i ) );

        this.channels = new HashMap<> ( model.getChannels ().size () );
        for ( final Map.Entry<String, ChannelConfiguration> entry : model.getChannels ().entrySet () )
        {
            this.channels.put ( entry.getKey (), new ChannelConfiguration ( entry.getValue () ) );
        }
    }

    public ChannelServiceModify ( final ChannelServiceModify other )
    {
        this ( other.model );

        // FIXME: improve speed
    }

    @Override
    public String mapToId ( final String name )
    {
        return this.nameToIdMap.get ( name );
    }

    public void putMapping ( final String channelId, final String name )
    {
        if ( name == null || name.isEmpty () )
        {
            return;
        }

        channelExists ( channelId );
        checkChannelName ( name );

        if ( this.nameToIdMap.containsKey ( name ) )
        {
            throw new IllegalStateException ( String.format ( "There already is a channel with the name '%s'", name ) );
        }

        this.nameToIdMap.put ( name, channelId );
        this.idToNameMap.put ( channelId, name );

        this.model.getNameMap ().put ( channelId, new ArrayList<> ( this.idToNameMap.get ( channelId ) ) );
    }

    private void channelExists ( final String channelId )
    {
        if ( !this.channels.containsKey ( channelId ) )
        {
            throw new IllegalArgumentException ( String.format ( "Channel '%s' does not exists", channelId ) );
        }
    }

    /**
     * Check if the name is a valid channel name
     *
     * @param name
     *            the name to check
     * @throws IllegalArgumentException
     *             if the name is not a valid channel name
     */
    private static void checkChannelName ( final String name )
    {
        if ( !NAME_PATTERN.matcher ( name ).matches () )
        {
            throw new IllegalArgumentException ( String.format ( "Channel name must match pattern: %s", NAME_PATTERN.pattern () ) );
        }
    }

    public String deleteMapping ( final String channelId, final String name )
    {
        if ( !this.idToNameMap.containsEntry ( channelId, name ) )
        {
            return null;
        }

        this.nameToIdMap.remove ( name );
        this.idToNameMap.remove ( channelId, name );

        final Collection<String> names = this.model.getNameMap ().get ( channelId );
        if ( names != null )
        {
            names.remove ( name );
        }

        return channelId;
    }

    public void createChannel ( final String channelId, final ChannelConfiguration cfg )
    {
        this.channels.put ( channelId, cfg );
        this.model.getChannels ().put ( channelId, cfg );
    }

    public void deleteChannel ( final String channelId )
    {
        // delete channel name mapping

        clearChannelNameMappings ( channelId );

        // remove channel

        this.model.getChannels ().remove ( channelId );
        this.channels.remove ( channelId );

        // delete channel group mapping
        this.model.getDeployGroupMap ().remove ( channelId );
    }

    private void clearChannelNameMappings ( final String channelId )
    {
        final Collection<String> names = this.idToNameMap.removeAll ( channelId );
        if ( names != null )
        {
            names.forEach ( this.nameToIdMap::remove );
        }
        this.model.getNameMap ().remove ( channelId );
    }

    ChannelServiceModel getModel ()
    {
        return this.model;
    }

    public DeployGroup createGroup ( final String name )
    {
        final DeployGroup result = new DeployGroup ( UUID.randomUUID ().toString (), name, Collections.emptyList () );
        internalAdd ( result );
        return result;
    }

    @Override
    public DeployGroup getDeployGroup ( final String groupId )
    {
        return this.deployGroups.get ( groupId );
    }

    @Override
    public Map<String, Set<String>> getDeployGroupMap ()
    {
        return Collections.unmodifiableMap ( this.model.getDeployGroupMap () );
    }

    public void updateGroup ( final String groupId, final String name )
    {
        modifyGroup ( groupId, old -> new DeployGroup ( old.getId (), name, old.getKeys () ) );
    }

    private void internalAdd ( final DeployGroup group )
    {
        this.model.getDeployGroups ().add ( group );
        this.deployGroups.put ( group.getId (), group );
        group.getKeys ().forEach ( key -> this.deployKeys.put ( key.getId (), key ) );
    }

    private DeployGroup internalRemove ( final String groupId )
    {
        final DeployGroup result = this.deployGroups.remove ( groupId );
        if ( result != null )
        {
            this.model.getDeployGroups ().remove ( result );

            // remove all keys from the deployKey map
            result.getKeys ().stream ().map ( DeployKey::getId ).forEach ( this.deployKeys::remove );
        }
        return result;
    }

    public void deleteGroup ( final String groupId )
    {
        final DeployGroup group = internalRemove ( groupId );
        if ( group != null )
        {
            // iterate over all channel -> group assignments and remove the groups
            boolean cleanup = false;
            for ( final Set<String> groups : this.model.getDeployGroupMap ().values () )
            {
                if ( groups.remove ( groupId ) )
                {
                    cleanup = true;
                }
            }

            if ( cleanup )
            {
                // remove empty groups from the model map
                final Iterator<Set<String>> i = this.model.getDeployGroupMap ().values ().iterator ();
                while ( i.hasNext () )
                {
                    if ( i.next ().isEmpty () )
                    {
                        i.remove ();
                    }
                }
            }
        }
    }

    @Override
    public List<DeployGroup> getDeployGroups ()
    {
        return Collections.unmodifiableList ( this.model.getDeployGroups () );
    }

    private void modifyGroup ( final String groupId, final Function<DeployGroup, DeployGroup> func )
    {
        final DeployGroup group = internalRemove ( groupId );

        if ( groupId == null )
        {
            throw new IllegalArgumentException ( String.format ( "Deploy group '%s' is unknown.", groupId ) );
        }

        final DeployGroup newGroup = func.apply ( group );

        if ( newGroup != null )
        {
            internalAdd ( newGroup );
        }
    }

    public DeployKey createKey ( final String groupId, final String name )
    {
        final Holder<DeployKey> result = new Holder<> ();

        modifyGroup ( groupId, old -> new DeployGroup ( old.getId (), old.getName (), old.getKeys (), ( newGroup ) -> {
            result.value = new DeployKey ( newGroup, UUID.randomUUID ().toString (), name, makeKey (), Instant.now () );
            return Collections.singletonList ( result.value );
        } ) );

        return result.value;
    }

    public DeployKey deleteKey ( final String keyId )
    {
        final DeployKey key = this.deployKeys.remove ( keyId );

        if ( key != null )
        {
            this.deployKeys.remove ( key.getId () );
            modifyGroup ( key.getGroup ().getId (), old -> new DeployGroup ( old.getId (), old.getName (), subKeys ( old.getKeys (), key ) ) );
        }

        return key;
    }

    private static List<DeployKey> subKeys ( final List<DeployKey> keys, final DeployKey key )
    {
        final List<DeployKey> result = new CopyOnWriteArrayList<> ( keys );
        result.remove ( key );
        return result;
    }

    @Override
    public DeployKey getDeployKey ( final String keyId )
    {
        return this.deployKeys.get ( keyId );
    }

    public DeployKey updateKey ( final String keyId, final String name )
    {
        final DeployKey key = this.deployKeys.get ( keyId );

        if ( key == null )
        {
            return null;
        }

        final Holder<DeployKey> result = new Holder<> ();

        modifyGroup ( key.getGroup ().getId (), old -> new DeployGroup ( old.getId (), old.getName (), subKeys ( old.getKeys (), key ), newGroup -> {
            result.value = new DeployKey ( newGroup, key.getId (), name, key.getKey (), key.getCreationTimestamp () );
            return Collections.singleton ( result.value );
        } ) );

        return result.value;
    }

    private String makeKey ()
    {
        return Tokens.createToken ( 32 /* FIXME: make configurable */ );
    }

    public Set<DeployGroup> getDeployGroupsForChannel ( final String channelId )
    {
        final Set<String> forChannel = this.model.getDeployGroupMap ().get ( channelId );
        if ( forChannel == null )
        {
            return Collections.emptySet ();
        }

        return forChannel.stream ().map ( this.deployGroups::get ).collect ( toSet () );
    }

    public void assignDeployGroup ( final String channelId, final String groupId )
    {
        if ( !this.deployGroups.containsKey ( groupId ) )
        {
            throw new IllegalArgumentException ( String.format ( "Deploy group %s does not exists", groupId ) );
        }

        Set<String> list = this.model.getDeployGroupMap ().get ( channelId );
        if ( list == null )
        {
            list = new HashSet<> ();
            this.model.getDeployGroupMap ().put ( channelId, list );
        }

        list.add ( groupId );
    }

    public void unassignDeployGroup ( final String channelId, final String groupId )
    {
        final Set<String> list = this.model.getDeployGroupMap ().get ( channelId );
        if ( list == null )
        {
            return;
        }

        list.remove ( groupId );
        if ( list.isEmpty () )
        {
            this.model.getDeployGroupMap ().remove ( channelId );
        }
    }

    public void setNameMappings ( final String channelId, final Collection<String> names )
    {
        Objects.requireNonNull ( channelId );
        Objects.requireNonNull ( names );

        clearChannelNameMappings ( channelId );

        for ( final String name : names )
        {
            putMapping ( channelId, name );
        }
    }

    @Override
    public Collection<String> getNameMappings ( final String channelId )
    {
        return this.idToNameMap.get ( channelId );
    }

    /**
     * Clear all mappings
     */
    public void clearNameMappings ()
    {
        this.model.getNameMap ().clear ();
        this.idToNameMap.clear ();
        this.nameToIdMap.clear ();
    }

    @Override
    public Map<String, ChannelConfiguration> getChannels ()
    {
        return Collections.unmodifiableMap ( this.channels );
    }

    public void setDescription ( final String channelId, final String description )
    {
        channelExists ( channelId );

        final ChannelConfiguration entry = this.model.getChannels ().get ( channelId );
        entry.setDescription ( description );
    }

    @Override
    public String getDescription ( final String channelId )
    {
        channelExists ( channelId );

        final ChannelConfiguration entry = this.model.getChannels ().get ( channelId );
        if ( entry == null )
        {
            return null;
        }
        return entry.getDescription ();
    }
}
