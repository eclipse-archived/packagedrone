/*******************************************************************************
 * Copyright (c) 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *     M-Ezzat - code cleanup - squid:S2162
 *******************************************************************************/

package org.eclipse.packagedrone.repo.channel.web.channel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.packagedrone.repo.ChannelAspectInformation;
import org.eclipse.packagedrone.repo.Version;
import org.eclipse.packagedrone.repo.aspect.group.GroupInformation;

/**
 * More UI suited channel aspect information
 */
public class AspectInformation
{
    private static final Comparator<AspectInformation> NAME_COMPARATOR = new Comparator<AspectInformation> () {

        @Override
        public int compare ( final AspectInformation o1, final AspectInformation o2 )
        {
            final int rc = o1.getName ().compareTo ( o2.getName () );
            if ( rc != 0 )
            {
                return rc;
            }

            return o1.getFactoryId ().compareTo ( o2.getFactoryId () );
        }
    };

    private Group group;

    private final ChannelAspectInformation information;

    private List<AspectInformation> requires = Collections.emptyList ();

    public AspectInformation ( final ChannelAspectInformation information )
    {
        this.information = information;
    }

    public String getName ()
    {
        return this.information.getLabel ();
    }

    public String getFactoryId ()
    {
        return this.information.getFactoryId ();
    }

    public ChannelAspectInformation getInformation ()
    {
        return this.information;
    }

    public boolean isResolved ()
    {
        return this.information.isResolved ();
    }

    public Version getVersion ()
    {
        return this.information.getVersion ();
    }

    public List<AspectInformation> getRequires ()
    {
        return this.requires;
    }

    public Group getGroup ()
    {
        return this.group;
    }

    public static List<AspectInformation> resolve ( final Collection<GroupInformation> groups, final Collection<ChannelAspectInformation> aspects )
    {
        if ( aspects == null )
        {
            return null;
        }

        final Map<String, AspectInformation> map = new HashMap<> ( aspects.size () );

        // convert

        for ( final ChannelAspectInformation aspect : aspects )
        {
            map.put ( aspect.getFactoryId (), new AspectInformation ( aspect ) );
        }

        // convert groups

        final Map<String, Group> groupMap = new HashMap<> ( groups.size () );
        for ( final GroupInformation gi : groups )
        {
            groupMap.put ( gi.getId (), new Group ( gi.getId (), gi.getName () ) );
        }

        // then resolve dependencies and assign groups

        final List<AspectInformation> result = new ArrayList<> ( aspects.size () );

        for ( final AspectInformation info : map.values () )
        {
            info.resolveDeps ( map );
            info.group = groupMap.get ( info.information.getGroupId () );
            if ( info.group == null )
            {
                info.group = Group.OTHER;
            }
            result.add ( info );
        }

        // sort

        Collections.sort ( result, NAME_COMPARATOR );

        return result;

    }

    private void resolveDeps ( final Map<String, AspectInformation> map )
    {
        if ( this.information.getRequires () == null )
        {
            this.requires = Collections.emptyList ();
            return;
        }

        this.requires = new ArrayList<> ( this.information.getRequires ().size () );
        for ( final String req : this.information.getRequires () )
        {
            AspectInformation reqInfo = map.get ( req );
            if ( reqInfo == null )
            {
                reqInfo = new AspectInformation ( ChannelAspectInformation.unresolved ( req ) );
            }
            this.requires.add ( reqInfo );
        }

        Collections.sort ( this.requires, NAME_COMPARATOR );
    }

    /**
     * Filter the provided aspect lists by a predicate on the ID
     *
     * @param list
     *            the list to filter
     * @param predicate
     *            the ID predicate
     * @return the filtered list, returns only <code>null</code> when the list
     *         was <code>null</code>
     */
    public static List<AspectInformation> filterIds ( final List<AspectInformation> list, final Predicate<String> predicate )
    {
        if ( list == null )
        {
            return null;
        }

        return list.stream ().filter ( ( i ) -> predicate.test ( i.getFactoryId () ) ).collect ( Collectors.toList () );
    }

    /**
     * Get all aspect ids which are currently missing but required by this
     * aspect
     *
     * @param assignedAspects
     *            the aspects which are considered assigned
     * @return A possibly empty, array of all missing but required aspects.
     *         Never returns <code>null</code>
     */
    public String[] getMissingIds ( final List<AspectInformation> assignedAspects )
    {
        final Set<AspectInformation> required = new HashSet<> ();

        addRequired ( required, this, assignedAspects );

        return required.stream ().map ( AspectInformation::getFactoryId ).toArray ( size -> new String[size] );
    }

    private static void addRequired ( final Set<AspectInformation> result, final AspectInformation aspect, final List<AspectInformation> assignedAspects )
    {
        for ( final AspectInformation req : aspect.getRequires () )
        {
            if ( !assignedAspects.contains ( req ) && result.add ( req ) )
            {
                addRequired ( result, req, assignedAspects );
            }
        }
    }

    @Override
    public int hashCode ()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( getFactoryId () == null ? 0 : getFactoryId ().hashCode () );
        return result;
    }

    @Override
    public boolean equals ( final Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( obj == null )
        {
            return false;
        }
        if ( this.getClass() != obj.getClass() )
        {
            return false;
        }
        final AspectInformation other = (AspectInformation)obj;
        if ( getFactoryId () == null )
        {
            if ( other.getFactoryId () != null )
            {
                return false;
            }
        }
        else if ( !getFactoryId ().equals ( other.getFactoryId () ) )
        {
            return false;
        }
        return true;
    }

    public static class Group implements Comparable<Group>
    {
        public static final Group OTHER = new Group ( "other", "Other" );

        private final String id;

        private final String name;

        public Group ( final String id, final String name )
        {
            this.id = id;
            this.name = name;
        }

        public String getId ()
        {
            return this.id;
        }

        public String getName ()
        {
            return this.name;
        }

        @Override
        public int compareTo ( final Group o )
        {
            final int rc = this.name.compareTo ( o.name );
            if ( rc != 0 )
            {
                return rc;
            }

            return this.id.compareTo ( o.id );
        }

        @Override
        public String toString ()
        {
            return this.id;
        }

        @Override
        public int hashCode ()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ( this.id == null ? 0 : this.id.hashCode () );
            return result;
        }

        @Override
        public boolean equals ( final Object obj )
        {
            if ( this == obj )
            {
                return true;
            }
            if ( obj == null )
            {
                return false;
            }
            if ( this.getClass() != obj.getClass() )
            {
                return false;
            }
            final Group other = (Group)obj;
            if ( this.id == null )
            {
                if ( other.id != null )
                {
                    return false;
                }
            }
            else if ( !this.id.equals ( other.id ) )
            {
                return false;
            }
            return true;
        }
    }

    public static Map<Group, List<AspectInformation>> group ( final List<AspectInformation> aspects )
    {
        final Map<Group, List<AspectInformation>> result = new HashMap<> ();

        for ( final AspectInformation ai : aspects )
        {
            List<AspectInformation> list = result.get ( ai.getGroup () );
            if ( list == null )
            {
                list = new LinkedList<> ();
                result.put ( ai.getGroup (), list );
            }
            list.add ( ai );
        }

        return result;
    }

    @Override
    public String toString ()
    {
        return String.format ( "[%s: %s]", this.information, this.group );
    }

}
