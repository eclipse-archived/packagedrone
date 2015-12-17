package org.eclipse.packagedrone.repo.trigger.http;

import java.util.Dictionary;
import java.util.Hashtable;

import javax.servlet.Servlet;

import org.eclipse.packagedrone.repo.channel.ChannelService;
import org.eclipse.packagedrone.repo.channel.ChannelService.By;
import org.eclipse.packagedrone.repo.channel.ModifiableChannel;
import org.eclipse.packagedrone.repo.trigger.Trigger;
import org.eclipse.packagedrone.repo.trigger.TriggerContext;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;

public class HttpTrigger implements Trigger
{
    private ServiceRegistration<Servlet> handle;

    private final String alias;

    private final ChannelService service;

    private final String channelId;

    private TriggerContext context;

    public HttpTrigger ( final String alias, final ChannelService service, final String channelId )
    {
        this.alias = alias;
        this.service = service;
        this.channelId = channelId;
    }

    @Override
    public Class<?>[] supportsContextClasses ()
    {
        return new Class<?>[] { ModifiableChannel.class };
    }

    @Override
    public void start ( final TriggerContext context )
    {
        this.context = context;

        final BundleContext bundleContext = FrameworkUtil.getBundle ( HttpTrigger.class ).getBundleContext ();

        final Servlet servlet = new TriggerServlet ( this );

        final Dictionary<String, Object> properties = new Hashtable<> ();

        properties.put ( "alias", "/trigger/" + this.alias );

        this.handle = bundleContext.registerService ( Servlet.class, servlet, properties );
    }

    @Override
    public void stop ()
    {
        this.handle.unregister ();
    }

    public void process ()
    {
        this.service.accessRun ( By.id ( this.channelId ), ModifiableChannel.class, channel -> {
            this.context.triggered ( channel );
        } );
    }
}
