/*******************************************************************************
 * Copyright (c) 2016 IBH SYSTEMS GmbH.
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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.packagedrone.VersionInformation;
import org.eclipse.packagedrone.repo.web.sitemap.ChangeFrequency;
import org.eclipse.packagedrone.repo.web.sitemap.SitemapExtender;
import org.eclipse.packagedrone.repo.web.sitemap.UrlSetContext;
import org.eclipse.packagedrone.web.Controller;
import org.eclipse.packagedrone.web.ModelAndView;
import org.eclipse.packagedrone.web.RequestMapping;
import org.eclipse.packagedrone.web.RequestMethod;
import org.eclipse.packagedrone.web.ViewResolver;

@Controller
@ViewResolver ( "/WEB-INF/views/%s.jsp" )
public class AboutController implements SitemapExtender
{
    @RequestMapping ( value = "/about", method = RequestMethod.GET )
    public ModelAndView about ()
    {
        final Map<String, Object> model = new HashMap<> ();

        model.put ( "version", VersionInformation.VERSION );
        model.put ( "buildIdHtml", VersionInformation.BUILD_ID.orElse ( "<em>unknown</em>" ) );

        return new ModelAndView ( "about", model );
    }

    @Override
    public void extend ( final UrlSetContext context )
    {
        context.addLocation ( "/about", empty (), of ( ChangeFrequency.MONTHLY ), of ( 0.1 ) );
    }
}
