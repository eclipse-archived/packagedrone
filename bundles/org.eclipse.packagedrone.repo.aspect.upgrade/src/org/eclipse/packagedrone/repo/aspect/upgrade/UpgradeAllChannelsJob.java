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
package org.eclipse.packagedrone.repo.aspect.upgrade;

import java.util.Collection;

import org.eclipse.packagedrone.job.JobFactory;
import org.eclipse.packagedrone.job.JobFactoryDescriptor;
import org.eclipse.packagedrone.job.JobInstance;
import org.eclipse.packagedrone.job.JobInstance.Context;
import org.eclipse.packagedrone.repo.channel.ChannelInformation;
import org.eclipse.packagedrone.repo.channel.ChannelService;
import org.eclipse.packagedrone.repo.channel.ChannelService.By;
import org.eclipse.packagedrone.repo.channel.ModifiableChannel;
import org.eclipse.packagedrone.utils.profiler.Profile;
import org.eclipse.packagedrone.utils.profiler.Profile.Handle;
import org.eclipse.packagedrone.web.LinkTarget;

public class UpgradeAllChannelsJob implements JobFactory
{

    private static final JobFactoryDescriptor DESCRIPTOR = new JobFactoryDescriptor () {

        @Override
        public LinkTarget getResultTarget ()
        {
            return null;
        }
    };

    public static final String ID = "drone.aspect.refreshAllChannels";

    private ChannelService service;

    public void setService ( final ChannelService service )
    {
        this.service = service;
    }

    @Override
    public JobFactoryDescriptor getDescriptor ()
    {
        return DESCRIPTOR;
    }

    @Override
    public JobInstance createInstance ( final String data ) throws Exception
    {
        return ( ctx ) -> {
            process ( ctx );
        };
    }

    private void process ( final Context ctx )
    {
        try ( Handle handle = Profile.start ( this, "process" ) )
        {
            final Collection<ChannelInformation> channels = this.service.list ();

            ctx.beginWork ( "Refreshing channels", channels.size () );

            for ( final ChannelInformation channelInformation : channels )
            {
                ctx.setCurrentTaskName ( String.format ( "Processing %s", channelInformation.getId () ) );

                this.service.accessRun ( By.id ( channelInformation.getId () ), ModifiableChannel.class, channel -> {
                    channel.getContext ().refreshAspects ( null );
                } );
                ctx.worked ( 1 );
            }

            ctx.complete ();
        }
    }

    @Override
    public String encodeConfiguration ( final Object data )
    {
        return null;
    }

    @Override
    public String makeLabel ( final String data )
    {
        return "Reprocess all channels";
    }

}
