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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.packagedrone.repo.ChannelAspectInformation;
import org.eclipse.packagedrone.repo.Version;
import org.eclipse.packagedrone.repo.aspect.ChannelAspectFactory;
import org.eclipse.packagedrone.repo.aspect.ChannelAspectProcessor;
import org.eclipse.packagedrone.repo.channel.ChannelInformation;
import org.eclipse.packagedrone.repo.channel.ChannelService;
import org.eclipse.packagedrone.repo.manage.todo.BasicTask;
import org.eclipse.packagedrone.repo.manage.todo.DefaultTaskProvider;
import org.eclipse.packagedrone.repo.manage.todo.Task;
import org.eclipse.packagedrone.utils.profiler.Profile;
import org.eclipse.packagedrone.utils.profiler.Profile.Handle;
import org.eclipse.packagedrone.web.LinkTarget;
import org.eclipse.packagedrone.web.RequestMethod;
import org.eclipse.packagedrone.web.common.Button;
import org.eclipse.packagedrone.web.common.Modifier;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.net.UrlEscapers;

public class UpgradeTaskProvider extends DefaultTaskProvider implements EventHandler
{
    private final static Logger logger = LoggerFactory.getLogger ( UpgradeTaskProvider.class );

    private static final Button PERFORM_BUTTON = new Button ( "Refresh aspect", "refresh", Modifier.DEFAULT );

    private static final Button PERFORM_ALL_BUTTON = new Button ( "Refresh channel", "refresh", Modifier.DEFAULT );

    private static final Button PERFORM_ALL_SUPER_BUTTON = new Button ( "Refresh all channels", "refresh", Modifier.DEFAULT );

    private final ServiceListener listener = new ServiceListener () {

        @Override
        public void serviceChanged ( final ServiceEvent event )
        {
            handleServiceChange ( event );
        }
    };

    private ChannelService service;

    private BundleContext context;

    public UpgradeTaskProvider ()
    {
    }

    public void setService ( final ChannelService service )
    {
        this.service = service;
    }

    public void start () throws Exception
    {
        logger.info ( "Starting channel aspect upgrade watcher" );

        this.context = FrameworkUtil.getBundle ( UpgradeTaskProvider.class ).getBundleContext ();
        this.context.addServiceListener ( this.listener, String.format ( "(%s=%s)", Constants.OBJECTCLASS, ChannelAspectFactory.class.getName () ) );

        // fork off the initial scan, since the component has to start even if the database is not present

        final Thread t = new Thread () {
            @Override
            public void run ()
            {
                refresh ();
            }
        };
        t.setName ( "UpgradeTaskProvider/initialScan" );
        t.setDaemon ( true );
        t.start ();
    }

    public void stop ()
    {
        this.context.removeServiceListener ( this.listener );
    }

    protected void handleServiceChange ( final ServiceEvent event )
    {
        logger.debug ( "service change - {} - {}", event.getType (), event.getServiceReference () );

        switch ( event.getType () )
        {
            case ServiceEvent.UNREGISTERING:
            case ServiceEvent.REGISTERED:
                refresh ();
                break;
            default:
                break;
        }
    }

    public void refresh ()
    {
        logger.info ( "Refreshing" );
        setTasks ( updateState () );
    }

    private List<Task> updateState ()
    {
        try ( Handle handle = Profile.start ( this, "updateState" ) )
        {
            final Map<String, ChannelAspectInformation> infos = ChannelAspectProcessor.scanAspectInformations ( this.context );

            final List<Task> result = new LinkedList<> ();

            final Multimap<String, ChannelInformation> missing = HashMultimap.create ();

            final Multimap<ChannelInformation, String> channels = HashMultimap.create ();

            for ( final ChannelInformation channel : this.service.list () )
            {
                logger.debug ( "Checking channel: {}", channel.getId () );

                final Map<String, String> states = channel.getAspectStates ();
                for ( final Map.Entry<String, String> entry : states.entrySet () )
                {
                    logger.debug ( "\t{}", entry.getKey () );

                    final ChannelAspectInformation info = infos.get ( entry.getKey () );
                    if ( info == null )
                    {
                        missing.put ( entry.getKey (), channel );
                    }
                    else
                    {
                        logger.debug ( "\t{} - {} -> {}", info.getFactoryId (), entry.getValue (), info.getVersion () );

                        if ( !info.getVersion ().equals ( Version.valueOf ( entry.getValue () ) ) )
                        {
                            result.add ( makeUpgradeTask ( channel, info, entry.getValue () ) );
                            channels.put ( channel, entry.getKey () );
                        }
                    }
                }
            }

            for ( final Map.Entry<ChannelInformation, Collection<String>> entry : channels.asMap ().entrySet () )
            {
                final ChannelInformation channel = entry.getKey ();
                final LinkTarget target = new LinkTarget ( String.format ( "/channel/%s/refreshAllAspects", UrlEscapers.urlPathSegmentEscaper ().escape ( channel.getId () ) ) );
                final String description = "Channel aspects active in this channel have been updated. You can refresh the whole channel.";
                result.add ( new BasicTask ( "Refresh channel: " + channel.makeTitle (), 100, description, target, RequestMethod.GET, PERFORM_ALL_BUTTON ) );
            }

            for ( final Map.Entry<String, Collection<ChannelInformation>> entry : missing.asMap ().entrySet () )
            {
                final String missingChannels = entry.getValue ().stream ().map ( ChannelInformation::getId ).collect ( Collectors.joining ( ", " ) );
                result.add ( new BasicTask ( String.format ( "Fix missing channel aspect: %s", entry.getKey () ), 1, String.format ( "The channel aspect '%s' is being used but not installed in the system. Channels: %s", entry.getKey (), missingChannels ), null ) );
            }

            if ( !channels.isEmpty () )
            {
                result.add ( new BasicTask ( "Refresh all channels", 1, "Refresh all channels in one big task", new LinkTarget ( String.format ( "/job/%s/create", UpgradeAllChannelsJob.ID ) ), RequestMethod.POST, PERFORM_ALL_SUPER_BUTTON ) );
            }

            return result;
        }
    }

    private Task makeUpgradeTask ( final ChannelInformation channel, final ChannelAspectInformation info, final String fromVersion )
    {
        final String channelName = channel.makeTitle ();

        String factoryId;
        try
        {
            factoryId = URLEncoder.encode ( info.getFactoryId (), "UTF-8" );
        }
        catch ( final UnsupportedEncodingException e )
        {
            factoryId = info.getFactoryId ();
        }

        final LinkTarget target = new LinkTarget ( String.format ( "/channel/%s/refreshAspect?aspect=%s", channel.getId (), factoryId ) );

        final String description = String.format ( "The aspect %s (%s) in channel %s was upgraded from version %s to %s. The channel aspect has to be re-processed.", info.getLabel (), info.getFactoryId (), channelName, fromVersion, info.getVersion () );
        return new BasicTask ( "Upgrade aspect data", 1_000, description, target, RequestMethod.POST, PERFORM_BUTTON );
    }

    @Override
    public void handleEvent ( final Event event )
    {
        logger.debug ( "Received event - {}", event.getTopic () );

        final String topic = event.getTopic ();
        final Object op = event.getProperty ( "operation" );

        if ( topic.startsWith ( "drone/channel/" ) )
        {
            if ( "remove".equals ( op ) || "refresh".equals ( op ) )
            {
                refresh ();
            }
        }
    }
}
