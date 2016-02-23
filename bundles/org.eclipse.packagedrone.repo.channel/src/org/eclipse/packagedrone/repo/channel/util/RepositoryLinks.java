/*******************************************************************************
 * Copyright (c) 2016 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.channel.util;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.eclipse.packagedrone.repo.channel.ChannelInformation;
import org.eclipse.packagedrone.web.LinkTarget;
import org.eclipse.packagedrone.web.common.Modifier;
import org.eclipse.packagedrone.web.common.menu.MenuEntry;

public class RepositoryLinks
{
    /**
     * Create a set of default repository links
     * <p>
     * This method calls
     * {@link #fillRepoLinks(ChannelInformation, List, String, int, String, int, LinkTarget)}
     * by using the baseName also as prefix and using a default
     * {@code priorityOffset} of 10_000
     * </p>
     *
     * @param channel
     *            the channel to generate for
     * @param links
     *            the receiver of the links
     * @param baseName
     *            the name of the top level menu entry
     * @param basePriority
     *            the priority of the top level menu entry
     * @param linkTemplate
     *            the template for generating the links, this template must
     *            contain the path variable <code>{idOrName}</code>
     */
    public static void fillRepoLinks ( final ChannelInformation channel, final List<MenuEntry> links, final String baseName, final int basePriority, final LinkTarget linkTemplate )
    {
        fillRepoLinks ( channel, links, baseName, basePriority, baseName, 10_000, linkTemplate );
    }

    /**
     * Create a set of default repository links
     *
     * @param channel
     *            the channel to generate for
     * @param links
     *            the receiver of the links
     * @param baseName
     *            the name of the top level menu entry
     * @param basePriority
     *            the priority of the top level menu entry
     * @param prefix
     *            the prefix of the child menu entries
     * @param priorityOffset
     *            the base priority of the child menu entries
     * @param linkTemplate
     *            the template for generating the links, this template must
     *            contain the path variable <code>{idOrName}</code>
     */
    public static void fillRepoLinks ( final ChannelInformation channel, final List<MenuEntry> links, final String baseName, final int basePriority, final String prefix, final int priorityOffset, final LinkTarget linkTemplate )
    {
        Objects.requireNonNull ( linkTemplate, "'linkTemplate' must not be null" );

        fillRepoLinks ( channel, links, baseName, basePriority, prefix, priorityOffset, idOrName -> makeDefaultRepoLink ( linkTemplate, idOrName ) );
    }

    /**
     * Create a set of default repository links
     *
     * @param channel
     *            the channel to generate for
     * @param links
     *            the receiver of the links
     * @param baseName
     *            the name of the top level menu entry
     * @param basePriority
     *            the priority of the top level menu entry
     * @param prefix
     *            the prefix of the child menu entries
     * @param priorityOffset
     *            the base priority of the child menu entries
     * @param targetFunction
     *            the function which provides a link target based on the id or
     *            name of the channel. Returning a {@code null} target will skip
     *            this id or name from the list
     */
    public static void fillRepoLinks ( final ChannelInformation channel, final List<MenuEntry> links, final String baseName, final int basePriority, final String prefix, final int priorityOffset, final Function<String, LinkTarget> targetFunction )
    {
        Objects.requireNonNull ( channel, "'channel' must not be null" );
        Objects.requireNonNull ( links, "'links' must not be null" );
        Objects.requireNonNull ( baseName, "'baseName' must not be null" );
        Objects.requireNonNull ( prefix, "'prefix' must not be null" );
        Objects.requireNonNull ( targetFunction, "'targetFunction' must not be null" );

        links.add ( new MenuEntry ( baseName, basePriority, prefix + " (by ID)", priorityOffset, targetFunction.apply ( channel.getId () ), Modifier.LINK, null ) );

        int i = 1;
        for ( final String name : channel.getNames () )
        {
            final LinkTarget target = targetFunction.apply ( name );
            if ( target != null )
            {
                links.add ( new MenuEntry ( baseName, basePriority, String.format ( "%s (name: %s)", prefix, name ), priorityOffset + i, target, Modifier.LINK, null ) );
            }
            i++;
        }
    }

    private static LinkTarget makeDefaultRepoLink ( final LinkTarget linkTemplate, final String idOrName )
    {
        return linkTemplate.expand ( Collections.singletonMap ( "idOrName", idOrName ) );
    }
}
