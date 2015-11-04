/*******************************************************************************
 * Copyright (c) 2014, 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.web.controller.binding;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.packagedrone.web.controller.binding.BindingManager.Result;

public class SimpleBindingResult implements BindingResult
{
    private final Map<String, BindingResult> children = new HashMap<> ();

    private final List<BindingError> errors = new LinkedList<> ();

    private final Set<String> markers = new HashSet<> ();

    public void addMarkers ( final Set<String> markers )
    {
        this.markers.addAll ( markers );
    }

    @Override
    public boolean hasErrors ()
    {
        if ( !this.errors.isEmpty () )
        {
            return true;
        }

        for ( final BindingResult br : this.children.values () )
        {
            if ( br.hasErrors () )
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasMarker ( final String marker )
    {
        if ( this.markers.contains ( marker ) )
        {
            return true;
        }

        for ( final BindingResult br : this.children.values () )
        {
            if ( br.hasMarker ( marker ) )
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public void addChild ( final String name, final BindingResult bindingResult )
    {
        this.children.put ( name, bindingResult );
    }

    @Override
    public BindingResult getChild ( final String name )
    {
        return this.children.get ( name );
    }

    @Override
    public BindingResult getChildOrAdd ( final String name )
    {
        BindingResult child = this.children.get ( name );
        if ( child == null )
        {
            child = new Result ();
            this.children.put ( name, child );
        }

        return child;
    }

    @Override
    public Map<String, BindingResult> getChildren ()
    {
        return Collections.unmodifiableMap ( this.children );
    }

    public void addErrors ( final String name, final List<BindingError> errors )
    {
        BindingResult br;

        if ( name == null || name.isEmpty () )
        {
            br = this;
        }
        else
        {
            br = getChildOrAdd ( name );
        }

        br.addErrors ( errors );
    }

    @Override
    public void addError ( final BindingError error )
    {
        this.errors.add ( error );
    }

    @Override
    public void addErrors ( final Collection<BindingError> errors )
    {
        this.errors.addAll ( errors );
    }

    @Override
    public List<BindingError> getErrors ()
    {
        final List<BindingError> result = new LinkedList<> ( this.errors );

        for ( final BindingResult br : this.children.values () )
        {
            result.addAll ( br.getErrors () );
        }

        return result;
    }

    @Override
    public List<BindingError> getLocalErrors ()
    {
        return Collections.unmodifiableList ( this.errors );
    }
}
