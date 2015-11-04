/*******************************************************************************
 * Copyright (c) 2014 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.web.controller.binding;

import org.eclipse.packagedrone.web.ModelAndView;

public abstract class ModelAndViewAwareBinding extends SimpleBinding
{
    public ModelAndViewAwareBinding ( final Object value, final BindingResult bindingResult )
    {
        super ( value, bindingResult );
    }

    @Override
    public Object postProcess ( final Object result )
    {
        if ( result instanceof ModelAndView )
        {
            postProcessModelAndView ( (ModelAndView)result );
        }
        else if ( result instanceof String )
        {
            final ModelAndView mav = new ModelAndView ( (String)result );
            postProcessModelAndView ( mav );
            return mav;
        }

        return result;
    }

    public abstract void postProcessModelAndView ( ModelAndView mav );
}
