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
package org.eclipse.packagedrone.repo.channel.web.internal;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import org.eclipse.packagedrone.repo.web.sitemap.ChangeFrequency;
import org.eclipse.packagedrone.repo.web.sitemap.UrlSetContext;
import org.eclipse.packagedrone.repo.web.sitemap.SitemapExtender;
import org.eclipse.packagedrone.web.Controller;
import org.eclipse.packagedrone.web.ModelAndView;
import org.eclipse.packagedrone.web.RequestMapping;
import org.eclipse.packagedrone.web.RequestMethod;
import org.eclipse.packagedrone.web.ViewResolver;

@Controller
@ViewResolver ( "/WEB-INF/views/%s.jsp" )
public class WelcomeController implements SitemapExtender
{
    @RequestMapping ( value = "/", method = RequestMethod.GET )
    public ModelAndView main ()
    {
        return new ModelAndView ( "index" );
    }

    @Override
    public void extend ( final UrlSetContext context )
    {
        context.addLocation ( "/", empty (), of ( ChangeFrequency.MONTHLY ), of ( 0.1 ) );
    }
}
