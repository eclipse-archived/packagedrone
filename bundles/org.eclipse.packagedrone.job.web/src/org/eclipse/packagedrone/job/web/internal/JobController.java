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
package org.eclipse.packagedrone.job.web.internal;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.HttpConstraint;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.packagedrone.job.JobFactoryDescriptor;
import org.eclipse.packagedrone.job.JobHandle;
import org.eclipse.packagedrone.job.JobManager;
import org.eclipse.packagedrone.sec.web.controller.HttpContraintControllerInterceptor;
import org.eclipse.packagedrone.sec.web.controller.Secured;
import org.eclipse.packagedrone.sec.web.controller.SecuredControllerInterceptor;
import org.eclipse.packagedrone.web.Controller;
import org.eclipse.packagedrone.web.LinkTarget;
import org.eclipse.packagedrone.web.ModelAndView;
import org.eclipse.packagedrone.web.RequestMapping;
import org.eclipse.packagedrone.web.RequestMethod;
import org.eclipse.packagedrone.web.ViewResolver;
import org.eclipse.packagedrone.web.controller.ControllerInterceptor;
import org.eclipse.packagedrone.web.controller.binding.PathVariable;
import org.eclipse.packagedrone.web.controller.binding.RequestParameter;
import org.eclipse.packagedrone.web.util.Requests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@ViewResolver ( "/WEB-INF/views/%s.jsp" )
@Secured
@RequestMapping ( "/job" )
@ControllerInterceptor ( SecuredControllerInterceptor.class )
@HttpConstraint ( rolesAllowed = "MANAGER" )
@ControllerInterceptor ( HttpContraintControllerInterceptor.class )
public class JobController
{
    private final static Logger logger = LoggerFactory.getLogger ( JobController.class );

    private JobManager manager;

    public void setManager ( final JobManager manager )
    {
        this.manager = manager;
    }

    @RequestMapping ( value = "/{factoryId}/create", method = RequestMethod.POST )
    @HttpConstraint ( rolesAllowed = { "MANAGER", "ADMIN" } )
    public ModelAndView create ( @PathVariable ( "factoryId" ) final String factoryId, @RequestParameter (
            required = false, value = "data" ) final String data)
    {
        final JobHandle job = this.manager.startJob ( factoryId, data );

        // forward to get loose of the POST request, so that we can reload the status page
        return new ModelAndView ( String.format ( "redirect:/job/%s/view", job.getId () ) );
    }

    @RequestMapping ( "/{id}/view" )
    public ModelAndView view ( @PathVariable ( "id" ) final String id)
    {
        final JobHandle job = this.manager.getJob ( id );

        final Map<String, Object> model = new HashMap<> ( 1 );
        model.put ( "job", job );
        return new ModelAndView ( "view", model );
    }

    /**
     * Monitor the job, only produces an HTML fragment of the current job state
     *
     * @param id
     *            the job id
     * @return the view
     */
    @RequestMapping ( "/{id}/monitor" )
    public ModelAndView monitor ( @PathVariable ( "id" ) final String id)
    {
        final JobHandle job = this.manager.getJob ( id );

        if ( job != null )
        {
            logger.debug ( "Job: {} - {}", job.getId (), job.getState () );
        }
        else
        {
            logger.debug ( "No job: {}", id );
        }

        final Map<String, Object> model = new HashMap<> ( 1 );
        model.put ( "job", job );
        return new ModelAndView ( "monitor", model );
    }

    @RequestMapping ( "/{id}/result" )
    public ModelAndView result ( @PathVariable ( "id" ) final String id, final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
    {
        final JobHandle job = this.manager.getJob ( id );

        if ( job != null && job.isComplete () && job.getError () != null )
        {
            // show default error page
            return defaultResult ( job );
        }

        final String factoryId = job.getRequest ().getFactoryId ();
        final JobFactoryDescriptor desc = this.manager.getFactory ( factoryId );

        if ( desc == null )
        {
            return defaultResult ( job );
        }

        final LinkTarget target = desc.getResultTarget ();
        if ( target == null )
        {
            return defaultResult ( job );
        }

        final LinkTarget url = target.expand ( Collections.singletonMap ( "id", id ) );

        logger.debug ( "Forwarding to job result view: {}", url );

        if ( url.getUrl ().equals ( Requests.getOriginalPath ( request ) ) )
        {
            throw new IllegalStateException ( String.format ( "Illegal redirect to same URL: %s", url.getUrl () ) );
        }

        final RequestDispatcher rd = request.getRequestDispatcher ( url.getUrl () );
        rd.forward ( request, response );

        return null;
    }

    protected ModelAndView defaultResult ( final JobHandle job )
    {
        final Map<String, Object> model = new HashMap<> ( 1 );
        model.put ( "job", job );
        return new ModelAndView ( "defaultResult", model );
    }
}
