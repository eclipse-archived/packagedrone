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
package org.eclipse.packagedrone.repo.channel.impl.trigger;

import static com.google.common.collect.Iterables.indexOf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.eclipse.packagedrone.repo.trigger.TriggerConfiguration;
import org.eclipse.packagedrone.repo.trigger.TriggerProcessorConfiguration;

public class TriggeredChannelModel
{
    private final Map<String, List<TriggerProcessorConfiguration>> processors = new HashMap<> ();

    private final Map<String, TriggerConfiguration> triggers = new HashMap<> ();

    public static class UnknownProcessorException extends IllegalArgumentException
    {
        private static final long serialVersionUID = 1L;

        public UnknownProcessorException ( final String triggerId, final String processorId )
        {
            super ( String.format ( "There is no processor '%s' for trigger '%s'", processorId, triggerId ) );
        }
    }

    public void addProcessor ( final String triggerId, final TriggerProcessorConfiguration value )
    {
        Objects.requireNonNull ( triggerId );
        Objects.requireNonNull ( value );

        List<TriggerProcessorConfiguration> trigger = this.processors.get ( triggerId );
        if ( trigger == null )
        {
            // create a new slot

            trigger = new ArrayList<> ();
            this.processors.put ( triggerId, trigger );
        }
        else
        {
            // check if the processor is already present

            if ( trigger.stream ().anyMatch ( cfg -> cfg.getId ().equals ( value.getId () ) ) )
            {
                throw new IllegalArgumentException ( String.format ( "Processor entry with ID '%s' already exists for trigger '%s'", value.getId (), triggerId ) );
            }
        }

        trigger.add ( value );
    }

    public void modifyProcessor ( final String triggerId, final String processorId, final String configuration )
    {
        Objects.requireNonNull ( triggerId );
        Objects.requireNonNull ( processorId );

        final List<TriggerProcessorConfiguration> trigger = this.processors.get ( triggerId );
        if ( trigger == null )
        {
            throw new UnknownProcessorException ( triggerId, processorId );
        }

        final ListIterator<TriggerProcessorConfiguration> i = trigger.listIterator ();
        while ( i.hasNext () )
        {
            final TriggerProcessorConfiguration cfg = i.next ();
            if ( !cfg.getId ().equals ( processorId ) )
            {
                continue;
            }

            // replace

            i.set ( new TriggerProcessorConfiguration ( processorId, cfg.getFactoryId (), configuration ) );

            // return

            return;
        }

        // did not return, so not found

        throw new UnknownProcessorException ( triggerId, processorId );
    }

    public void deleteProcessor ( final String triggerId, final String processorId )
    {
        final List<TriggerProcessorConfiguration> trigger = this.processors.get ( triggerId );
        if ( trigger == null )
        {
            // nothing to do
            return;
        }

        trigger.removeIf ( cfg -> cfg.getId ().equals ( processorId ) );

        if ( trigger.isEmpty () )
        {
            this.processors.remove ( triggerId );
        }
    }

    public Set<String> getTriggers ()
    {
        final Set<String> result = new HashSet<> ();
        result.addAll ( this.processors.keySet () );
        result.addAll ( this.triggers.keySet () );
        return result;
    }

    public Map<String, TriggerConfiguration> getConfiguredTriggers ()
    {
        return this.triggers;
    }

    public List<TriggerProcessorConfiguration> getProcessors ( final String triggerId )
    {
        final List<TriggerProcessorConfiguration> trigger = this.processors.get ( triggerId );
        if ( trigger == null )
        {
            return Collections.emptyList ();
        }

        return Collections.unmodifiableList ( trigger );
    }

    public void addTrigger ( final String triggerId, final TriggerConfiguration triggerConfiguration )
    {
        Objects.requireNonNull ( triggerId );
        Objects.requireNonNull ( triggerConfiguration );

        if ( this.triggers.containsKey ( triggerId ) )
        {
            throw new IllegalStateException ( String.format ( "Trigger '%s' already exists for this channel", triggerId ) );
        }

        this.triggers.put ( triggerId, triggerConfiguration );
    }

    public void modifyTrigger ( final String triggerId, final String configuration )
    {
        Objects.requireNonNull ( triggerId );
        Objects.requireNonNull ( configuration );

        final TriggerConfiguration trigger = this.triggers.get ( triggerId );

        if ( trigger == null )
        {
            throw new IllegalStateException ( String.format ( "Trigger '%s' does not exist on this channel", triggerId ) );
        }

        this.triggers.put ( triggerId, new TriggerConfiguration ( trigger.getTriggerFactoryId (), configuration ) );
    }

    public void deleteTrigger ( final String triggerId )
    {
        this.triggers.remove ( triggerId );
    }

    public Optional<TriggerConfiguration> getTriggerConfiguration ( final String triggerId )
    {
        return Optional.ofNullable ( this.triggers.get ( triggerId ) );
    }

    public void reorderProcessors ( final String triggerId, final String processorId1, final String processorId2 )
    {
        Objects.requireNonNull ( triggerId );
        Objects.requireNonNull ( processorId1 );

        final List<TriggerProcessorConfiguration> list = this.processors.get ( triggerId );
        if ( list == null )
        {
            throw new IllegalArgumentException ( String.format ( "Unknow trigger '%s'", triggerId ) );
        }

        final int p1 = indexOf ( list, e -> e.getId ().equals ( processorId1 ) );
        final int p2 = processorId2 != null ? indexOf ( list, e -> e.getId ().equals ( processorId2 ) ) : 0;

        if ( p1 < 0 )
        {
            throw new IllegalArgumentException ( String.format ( "Unknown processor '%s' for trigger '%s'", processorId1, triggerId ) );
        }
        if ( p2 < 0 )
        {
            throw new IllegalArgumentException ( String.format ( "Unknown processor '%s' for trigger '%s'", processorId2, triggerId ) );
        }

        final TriggerProcessorConfiguration t1 = list.remove ( p1 );
        if ( processorId2 != null )
        {
            list.add ( p2, t1 );
        }
        else
        {
            list.add ( t1 );
        }
    }

    public void moveProcessor ( final String triggerId1, final String processorId1, final String triggerId2, final String processorId2 )
    {
        Objects.requireNonNull ( triggerId1 );
        Objects.requireNonNull ( triggerId2 );
        Objects.requireNonNull ( processorId1 );

        final List<TriggerProcessorConfiguration> list1 = this.processors.get ( triggerId1 );
        if ( list1 == null )
        {
            throw new IllegalArgumentException ( String.format ( "Unknow trigger '%s'", triggerId1 ) );
        }

        final List<TriggerProcessorConfiguration> list2 = this.processors.get ( triggerId2 );
        if ( list2 == null )
        {
            throw new IllegalArgumentException ( String.format ( "Unknow trigger '%s'", triggerId2 ) );
        }

        final int p1 = indexOf ( list1, e -> e.getId ().equals ( processorId1 ) );
        final int p2 = processorId2 != null ? indexOf ( list2, e -> e.getId ().equals ( processorId2 ) ) : 0;

        if ( p1 < 0 )
        {
            throw new IllegalArgumentException ( String.format ( "Unknown processor '%s' for trigger '%s'", processorId1, triggerId1 ) );
        }
        if ( p2 < 0 )
        {
            throw new IllegalArgumentException ( String.format ( "Unknown processor '%s' for trigger '%s'", processorId2, triggerId2 ) );
        }

        final TriggerProcessorConfiguration t1 = list1.remove ( p1 );

        if ( processorId2 != null )
        {
            list2.add ( p2, t1 );
        }
        else
        {
            list2.add ( t1 );
        }
    }

}
