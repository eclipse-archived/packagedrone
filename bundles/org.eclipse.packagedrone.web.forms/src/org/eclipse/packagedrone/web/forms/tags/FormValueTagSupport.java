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
package org.eclipse.packagedrone.web.forms.tags;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

import javax.servlet.jsp.tagext.Tag;

import org.apache.commons.beanutils.PropertyUtils;
import org.eclipse.packagedrone.web.controller.binding.BindingError;
import org.eclipse.packagedrone.web.controller.binding.BindingResult;

public class FormValueTagSupport extends FormTagSupport
{
    private static final long serialVersionUID = 1L;

    protected String path;

    public void setPath ( final String path )
    {
        this.path = path;
    }

    protected List<BindingError> getErrors ()
    {
        final BindingResult br = getBindingResult ();
        if ( br == null )
        {
            return Collections.emptyList ();
        }
        return br.getLocalErrors ();
    }

    protected BindingResult getBindingResult ()
    {
        final BindingResult br = getCommandBindingResult ();
        if ( br == null )
        {
            return null;
        }

        if ( this.path == null || this.path.isEmpty () )
        {
            return br;
        }

        return br.getChild ( this.path );
    }

    protected Object getPathValue ( final String path )
    {
        final Object command = getCommandValue ();
        if ( command == null )
        {
            return null;
        }

        try
        {
            return PropertyUtils.getProperty ( command, path );
        }
        catch ( NoSuchMethodException | IllegalAccessException | InvocationTargetException e )
        {
            return null;
        }
    }

    protected Object getCommandValue ()
    {
        final Tag formTag = findAncestorWithClass ( this, Form.class );
        if ( formTag instanceof Form )
        {
            return ( (Form)formTag ).getCommandValue ();
        }
        else
        {
            return null;
        }
    }

    protected BindingResult getCommandBindingResult ()
    {
        final Tag formTag = findAncestorWithClass ( this, Form.class );
        if ( formTag instanceof Form )
        {
            return ( (Form)formTag ).getBindingResult ();
        }
        else
        {
            return null;
        }
    }
}
