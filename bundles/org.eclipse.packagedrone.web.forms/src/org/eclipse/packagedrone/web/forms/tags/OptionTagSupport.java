/*******************************************************************************
 * Copyright (c) 2014, 2016 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.web.forms.tags;

import java.util.Collection;
import java.util.function.Function;

import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

public class OptionTagSupport extends TagSupport
{
    private static final long serialVersionUID = 1L;

    protected Object getSelectedValue ()
    {
        final Tag parent = findAncestorWithClass ( this, Select.class );
        if ( parent instanceof Select )
        {
            return ( (Select)parent ).getSelectedValue ();
        }
        else
        {
            return null;
        }
    }

    protected boolean isSelected ( final String value, final Function<Object, String> toStringFunction )
    {
        final Object selectedValue = getSelectedValue ();
        if ( selectedValue == value )
        {
            return true;
        }
        if ( selectedValue == null )
        {
            return false;
        }
        if ( selectedValue instanceof Collection<?> )
        {
            return ( (Collection<?>)selectedValue ).stream ().map ( v -> mapIfNonString ( v, toStringFunction ) ).anyMatch ( v -> v.equals ( value ) );
        }
        else
        {
            return mapIfNonString ( selectedValue, toStringFunction ).equals ( value );
        }
    }

    private static String mapIfNonString ( final Object object, final Function<Object, String> toStringFunction )
    {
        if ( object instanceof String )
        {
            return (String)object;
        }
        return toStringFunction.apply ( object );
    }

}
