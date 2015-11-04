/*******************************************************************************
 * Copyright (c) 2014, 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo;

import java.util.Collections;
import java.util.Comparator;
import java.util.SortedSet;

public class ChannelAspectInformation
{
    public static final Comparator<ChannelAspectInformation> NAME_COMPARATOR = new Comparator<ChannelAspectInformation> () {

        @Override
        public int compare ( final ChannelAspectInformation o1, final ChannelAspectInformation o2 )
        {
            final int rc = o1.label.compareTo ( o2.label );
            if ( rc != 0 )
            {
                return rc;
            }
            return o1.factoryId.compareTo ( o2.factoryId );
        }
    };

    private final String factoryId;

    private final String description;

    private final String label;

    private final boolean resolved;

    private final SortedSet<String> requires;

    private final String groupId;

    private final Version version;

    private ChannelAspectInformation ( final String factoryId )
    {
        this.factoryId = factoryId;
        this.resolved = false;

        this.groupId = null;
        this.label = null;
        this.description = null;
        this.version = Version.EMPTY;
        this.requires = Collections.emptySortedSet ();
    }

    public ChannelAspectInformation ( final String factoryId, final String label, final String description, final String groupId, final SortedSet<String> requires, final Version version )
    {
        this.factoryId = factoryId;
        this.groupId = groupId == null || groupId.isEmpty () ? "other" : groupId;
        this.label = label;
        this.description = description;
        this.requires = requires == null ? Collections.emptySortedSet () : Collections.unmodifiableSortedSet ( requires );
        this.version = version;
        this.resolved = true;
    }

    public Version getVersion ()
    {
        return this.version;
    }

    /**
     * Get an unmodifiable sorted set of requirements
     *
     * @return the set of requirement id
     */
    public SortedSet<String> getRequires ()
    {
        return this.requires;
    }

    /**
     * Get the id of the group this aspect belongs to
     *
     * @return the group id, never returns <code>null</code>
     */
    public String getGroupId ()
    {
        return this.groupId;
    }

    public String getDescription ()
    {
        return this.description;
    }

    public String getLabel ()
    {
        return this.label == null ? this.factoryId : this.label;
    }

    public String getFactoryId ()
    {
        return this.factoryId;
    }

    public boolean isResolved ()
    {
        return this.resolved;
    }

    /**
     * Create an unresolved information instance
     *
     * @param factoryId
     *            the factory id
     * @return a new information instance
     */
    public static ChannelAspectInformation unresolved ( final String factoryId )
    {
        return new ChannelAspectInformation ( factoryId );
    }

    @Override
    public String toString ()
    {
        return this.factoryId;
    }

}
