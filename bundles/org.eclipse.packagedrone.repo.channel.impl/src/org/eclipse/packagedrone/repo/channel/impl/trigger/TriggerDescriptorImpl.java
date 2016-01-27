package org.eclipse.packagedrone.repo.channel.impl.trigger;

import org.eclipse.packagedrone.repo.trigger.TriggerDescriptor;

public final class TriggerDescriptorImpl implements TriggerDescriptor
{
    private final String label;

    private final String description;

    private final Class<?>[] supportedContexts;

    public TriggerDescriptorImpl ( final String label, final String description, final Class<?>... supportedContexts )
    {
        this.label = label;
        this.description = description;
        this.supportedContexts = supportedContexts;
    }

    @Override
    public String getLabel ()
    {
        return this.label;
    }

    @Override
    public String getDescription ()
    {
        return this.description;
    }

    @Override
    public Class<?>[] getSupportedContexts ()
    {
        return this.supportedContexts;
    }
}