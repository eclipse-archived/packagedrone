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
package org.eclipse.packagedrone.web.forms.tags;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.jsp.PageContext;

import org.eclipse.packagedrone.web.controller.binding.BindingResult;

public class Functions
{
    public static String validationState ( final PageContext pageContext, final String command, final String path, final String okCssClass, final String errorCssClass )
    {
        final BindingResult br = (BindingResult)pageContext.getRequest ().getAttribute ( BindingResult.ATTRIBUTE_NAME );
        if ( br == null )
        {
            return "";
        }

        BindingResult result = br.getChild ( command );
        if ( result == null )
        {
            return "";
        }

        if ( path != null && !path.isEmpty () )
        {
            result = result.getChild ( path );
            if ( result == null )
            {
                return "";
            }
        }

        return result.hasErrors () ? errorCssClass : okCssClass;
    }

    public static List<String> errors ( final PageContext pageContext, final String command, final String path, final boolean local )
    {
        final BindingResult br = (BindingResult)pageContext.getRequest ().getAttribute ( BindingResult.ATTRIBUTE_NAME );
        if ( br == null )
        {
            return Collections.emptyList ();
        }

        BindingResult result = br.getChild ( command );
        if ( result == null )
        {
            return Collections.emptyList ();
        }

        if ( path != null && !path.isEmpty () )
        {
            result = result.getChild ( path );
            if ( result == null )
            {
                return Collections.emptyList ();
            }
        }

        return ( local ? result.getLocalErrors () : result.getErrors () ).stream ().map ( err -> err.getMessage () ).collect ( Collectors.toList () );
    }
}
