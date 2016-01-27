package org.eclipse.packagedrone.repo.channel.apm;

import java.util.LinkedList;
import java.util.Optional;

import org.eclipse.packagedrone.repo.channel.provider.ChannelOperationContext;

public final class ChannelContextAccessor
{
    private static final ThreadLocal<LinkedList<ChannelOperationContext>> threadLocal = ThreadLocal.withInitial ( LinkedList::new );

    private ChannelContextAccessor ()
    {
    }

    public static Optional<ChannelOperationContext> current ()
    {
        final LinkedList<ChannelOperationContext> stack = threadLocal.get ();
        if ( stack.isEmpty () )
        {
            return Optional.empty ();
        }
        else
        {
            return Optional.of ( stack.peek () );
        }
    }

    public static void push ( final ChannelOperationContext context )
    {
        final LinkedList<ChannelOperationContext> stack = threadLocal.get ();
        stack.push ( context );
    }

    public static void pop ()
    {
        final LinkedList<ChannelOperationContext> stack = threadLocal.get ();
        if ( stack.isEmpty () )
        {
            throw new IllegalStateException ( String.format ( "Unbalanced access ChannelContextAccessor for thread '%s'", Thread.currentThread ().getName () ) );
        }
        stack.pop ();
    }
}
