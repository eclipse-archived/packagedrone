package org.eclipse.packagedrone.repo.trigger.http;

import java.util.Map;

import org.eclipse.packagedrone.repo.channel.ChannelService;
import org.eclipse.packagedrone.repo.trigger.Trigger;
import org.eclipse.packagedrone.repo.trigger.TriggerFactory;

public class HttpTriggerFactory implements TriggerFactory
{
    private ChannelService service;

    public void setService ( final ChannelService service )
    {
        this.service = service;
    }

    @Override
    public Trigger create ( final String channelId, final Map<String, String> configuration )
    {
        final String alias = configuration.get ( "alias" );
        if ( alias == null || alias.trim ().isEmpty () )
        {
            throw new IllegalArgumentException ( "'alias' must not be null or empty" );
        }

        return new HttpTrigger ( alias, this.service, channelId );
    }
}
