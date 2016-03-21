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
package org.eclipse.packagedrone.repo.channel.impl.transfer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.packagedrone.repo.trigger.TriggerHandle;
import org.eclipse.packagedrone.repo.trigger.TriggerProcessor;
import org.eclipse.packagedrone.repo.trigger.TriggeredChannel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class TriggerExport
{
    public static class TriggerEntry
    {
        private String id;

        private String factoryId;

        private String configuration;

        private List<ProcessorEntry> processors;

        public String getId ()
        {
            return this.id;
        }

        public void setId ( final String id )
        {
            this.id = id;
        }

        public String getFactoryId ()
        {
            return this.factoryId;
        }

        public void setFactoryId ( final String factoryId )
        {
            this.factoryId = factoryId;
        }

        public String getConfiguration ()
        {
            return this.configuration;
        }

        public void setConfiguration ( final String configuration )
        {
            this.configuration = configuration;
        }

        public List<ProcessorEntry> getProcessors ()
        {
            return this.processors;
        }

        public void setProcessors ( final List<ProcessorEntry> processors )
        {
            this.processors = processors;
        }
    }

    public static class ProcessorEntry
    {
        private String factoryId;

        private String configuration;

        public String getFactoryId ()
        {
            return this.factoryId;
        }

        public void setFactoryId ( final String factoryId )
        {
            this.factoryId = factoryId;
        }

        public String getConfiguration ()
        {
            return this.configuration;
        }

        public void setConfiguration ( final String configuration )
        {
            this.configuration = configuration;
        }
    }

    private final List<TriggerEntry> entries = new LinkedList<> ();

    public TriggerExport ()
    {
    }

    private static Gson createGson ()
    {
        return new GsonBuilder ().setPrettyPrinting ().create ();
    }

    public String toJson ()
    {
        return createGson ().toJson ( this );
    }

    public static TriggerExport fromJson ( final String json )
    {
        return createGson ().fromJson ( json, TriggerExport.class );
    }

    public static TriggerExport buildFrom ( final TriggeredChannel channel )
    {
        final TriggerExport result = new TriggerExport ();

        for ( final TriggerHandle handle : channel.listTriggers () )
        {
            final String id = handle.getId ();

            final TriggerEntry entry = new TriggerEntry ();
            entry.setId ( id );
            entry.setProcessors ( handle.getProcessors ().stream ().map ( TriggerExport::exportProcessor ).collect ( Collectors.toCollection ( ArrayList::new ) ) );

            handle.getConfiguration ().ifPresent ( cfg -> {
                entry.setFactoryId ( cfg.getTriggerFactoryId () );
                entry.setId ( null );
                entry.setConfiguration ( cfg.getConfiguration () );
            } );

            if ( entry.getFactoryId () != null || !entry.getProcessors ().isEmpty () )
            {
                result.entries.add ( entry );
            }
        }

        return result;
    }

    private static ProcessorEntry exportProcessor ( final TriggerProcessor processor )
    {
        final ProcessorEntry entry = new ProcessorEntry ();

        entry.setFactoryId ( processor.getConfiguration ().getFactoryId () );
        entry.setConfiguration ( processor.getConfiguration ().getConfiguration () );

        return entry;
    }

    public void apply ( final TriggeredChannel channel )
    {
        for ( final TriggerEntry entry : this.entries )
        {
            String triggerId;
            if ( entry.getFactoryId () != null )
            {
                triggerId = channel.addConfiguredTrigger ( entry.getFactoryId (), entry.getConfiguration () ).getId ();
            }
            else
            {
                triggerId = entry.getId ();
            }

            for ( final ProcessorEntry processor : entry.getProcessors () )
            {
                channel.addProcessor ( triggerId, processor.getFactoryId (), processor.getConfiguration () );
            }
        }
    }

}
