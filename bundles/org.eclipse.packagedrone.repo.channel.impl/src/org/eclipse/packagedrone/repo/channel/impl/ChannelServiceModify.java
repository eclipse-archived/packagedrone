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

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.packagedrone.repo.channel.deploy.DeployGroup;
import org.eclipse.packagedrone.repo.channel.deploy.DeployKey;
import org.eclipse.packagedrone.repo.utils.Tokens;
import org.eclipse.packagedrone.utils.Holder;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class ChannelServiceModify implements ChannelServiceAccess
{
    private final ChannelServiceModel model;

    private final BiMap<String, String> map;

    private final Map<String, String> unmodMap;

    private final Map<String, DeployGroup> deployGroups;

    private final Map<String, DeployKey> deployKeys;

    public ChannelServiceModify ( final ChannelServiceModel model )
    {
        this.model = new ChannelServiceModel ( model );
        this.map = HashBiMap.create ( model.getNameMap () );
        this.unmodMap = Collections.unmodifiableMap ( this.map );

        this.deployGroups = model.getDeployGroups ().stream ().collect ( Collectors.toMap ( DeployGroup::getId, i -> i ) );
        this.deployKeys = model.getDeployGroups ().stream ().flatMap ( group -> group.getKeys ().stream () ).collect ( Collectors.toMap ( DeployKey::getId, i -> i ) );
    }

    public ChannelServiceModify ( final ChannelServiceModify other )
    {
        this ( other.model );
    }

    @Override
    public Map<String, String> getNameMap ()
    {
        return this.unmodMap;
    }

    @Override
    public String mapToId ( final String name )
    {
        return this.map.inverse ().get ( name );
    }

    @Override
    public String mapToName ( final String id )
    {
        return this.map.get ( id );
    }

    public void putMapping ( final String id, final String name )
    {
        if ( name == null || name.isEmpty () )
        {
            this.model.getNameMap ().remove ( id );
            this.map.remove ( id );
            return;
        }

        final String oldId = this.map.inverse ().get ( name );
        if ( oldId != null )
        {
            if ( oldId.equals ( id ) )
            {
                // no change
                return;
            }
            throw new IllegalStateException ( String.format ( "There already is a channel with the name '%s'", name ) );
        }

        // put mapping

        this.model.getNameMap ().put ( id, name );
        this.map.put ( id, name );
    }

    public String deleteMapping ( final String channelId, final String name )
    {
        this.map.remove ( channelId, name );
        return this.model.getNameMap ().remove ( channelId, name ) ? channelId : null;
    }

    public void deleteChannel ( final String channelId )
    {
        // delete channel name mapping
        this.map.remove ( channelId );
        this.model.getNameMap ().remove ( channelId );

        // delete channel group mapping
        this.model.getDeployGroupMap ().remove ( channelId );
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

    /**
     * Clear all mappings
     */
    public void clear ()
    {
        this.model.getNameMap ().clear ();
        this.map.clear ();
    }

}
