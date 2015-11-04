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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.packagedrone.repo.ChannelAspectInformation;
import org.eclipse.packagedrone.repo.aspect.ChannelAspectProcessor;
import org.eclipse.packagedrone.repo.channel.ChannelId;
import org.eclipse.packagedrone.repo.channel.ModifiableChannel;
import org.eclipse.packagedrone.repo.channel.provider.ModifyContext;
import org.eclipse.packagedrone.utils.profiler.Profile;
import org.eclipse.packagedrone.utils.profiler.Profile.Handle;

public class ModifiableChannelAdapter extends ReadableChannelAdapter implements ModifiableChannel
{
    private final ModifyContext context;

    private final ChannelAspectProcessor aspectProcessor;

    public ModifiableChannelAdapter ( final ChannelId descriptor, final ModifyContext context, final ChannelAspectProcessor aspectProcessor )
    {
        super ( descriptor, context );
        this.context = context;
        this.aspectProcessor = aspectProcessor;
    }

    @Override
    public ModifyContext getContext ()
    {
        return this.context;
    }

    @Override
    public void addAspects ( final boolean withDependencies, final String... aspectIds )
    {
        final Set<String> aspects = new HashSet<> ( Arrays.asList ( aspectIds ) );

        if ( withDependencies )
        {
            getContext ().addAspects ( expandDependencies ( aspects ) );
        }
        else
        {
            getContext ().addAspects ( aspects );
        }
    }

    private Set<String> expandDependencies ( final Set<String> aspects )
    {
        try ( Handle handle = Profile.start ( ModifiableChannelAdapter.class.getName () + ".expandDependencies" ) )
        {
            final Map<String, ChannelAspectInformation> all = this.aspectProcessor.getAspectInformations ();

            final Set<String> result = new HashSet<> ();
            final TreeSet<String> requested = new TreeSet<> ();
            requested.addAll ( aspects );

            while ( !requested.isEmpty () )
            {
                final String id = requested.pollFirst ();

                if ( result.add ( id ) )
                {
                    final ChannelAspectInformation asp = all.get ( id );

                    final Set<String> reqs = new HashSet<> ( asp.getRequires () );
                    reqs.removeAll ( requested ); // remove all which are already present
                    requested.addAll ( reqs ); // add to request list
                }
            }

            return result;
        }
    }

}
