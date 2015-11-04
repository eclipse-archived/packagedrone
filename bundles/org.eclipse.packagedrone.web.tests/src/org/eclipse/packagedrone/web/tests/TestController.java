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
package org.eclipse.packagedrone.web.tests;

import org.eclipse.packagedrone.web.Controller;
import org.eclipse.packagedrone.web.ModelAndView;
import org.eclipse.packagedrone.web.RequestMapping;
import org.eclipse.packagedrone.web.ViewResolver;

@Controller
@RequestMapping ( "/a" )
@ViewResolver ( "WEB-INF/views/%s.jsp" )
public class TestController
{
    @RequestMapping
    public ModelAndView mainA ()
    {
        return new ModelAndView ( "index" );
    }

    @RequestMapping ( "/b" )
    public ModelAndView mainB ()
    {
        return new ModelAndView ( "index" );
    }
}
