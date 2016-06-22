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
package org.eclipse.packagedrone.sec.web.ui;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.annotation.HttpConstraint;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.packagedrone.sec.service.AccessToken;
import org.eclipse.packagedrone.sec.service.AccessTokenService;
import org.eclipse.packagedrone.sec.web.controller.HttpContraintControllerInterceptor;
import org.eclipse.packagedrone.sec.web.controller.Secured;
import org.eclipse.packagedrone.sec.web.controller.SecuredControllerInterceptor;
import org.eclipse.packagedrone.web.Controller;
import org.eclipse.packagedrone.web.LinkTarget;
import org.eclipse.packagedrone.web.ModelAndView;
import org.eclipse.packagedrone.web.RequestMapping;
import org.eclipse.packagedrone.web.RequestMethod;
import org.eclipse.packagedrone.web.ViewResolver;
import org.eclipse.packagedrone.web.common.CommonController;
import org.eclipse.packagedrone.web.common.InterfaceExtender;
import org.eclipse.packagedrone.web.common.Modifier;
import org.eclipse.packagedrone.web.common.menu.MenuEntry;
import org.eclipse.packagedrone.web.common.page.Pagination;
import org.eclipse.packagedrone.web.controller.ControllerInterceptor;
import org.eclipse.packagedrone.web.controller.binding.BindingResult;
import org.eclipse.packagedrone.web.controller.binding.PathVariable;
import org.eclipse.packagedrone.web.controller.binding.RequestParameter;
import org.eclipse.packagedrone.web.controller.form.FormData;

@Controller
@ViewResolver ( "/WEB-INF/views/accessToken/%s.jsp" )
@RequestMapping ( "/accessToken" )
@Secured
@ControllerInterceptor ( SecuredControllerInterceptor.class )
@HttpConstraint ( rolesAllowed = "ADMIN" )
@ControllerInterceptor ( HttpContraintControllerInterceptor.class )
public class AccessTokenController implements InterfaceExtender
{
    private AccessTokenService accessTokenService;

    public static final Object TAG = new Object ();

    public void setAccessTokenService ( final AccessTokenService accessTokenService )
    {
        this.accessTokenService = accessTokenService;
    }

    @Override
    public List<MenuEntry> getMainMenuEntries ( final HttpServletRequest request )
    {
        if ( request.isUserInRole ( "ADMIN" ) )
        {
            final List<MenuEntry> result = new LinkedList<> ();
            result.add ( new MenuEntry ( "Administration", 10_000, "Access Tokens", 1_200, LinkTarget.createFromController ( AccessTokenController.class, "list" ), null, null ) );
            return result;
        }
        return null;
    }

    @Override
    public List<MenuEntry> getActions ( final HttpServletRequest request, final Object object )
    {
        if ( request.isUserInRole ( "ADMIN" ) )
        {
            final List<MenuEntry> result = new LinkedList<> ();
            if ( object == TAG )
            {
                result.add ( new MenuEntry ( "Create", 1_000, LinkTarget.createFromController ( AccessTokenController.class, "create" ), Modifier.PRIMARY, "plus" ) );
            }
            return result;
        }
        else
        {
            return null;
        }
    }

    @RequestMapping
    public ModelAndView list ( @RequestParameter ( required = false, value = "start" ) final Integer position )
    {
        final Map<String, Object> model = new HashMap<> ();

        model.put ( "tokens", Pagination.paginate ( position, 25, this.accessTokenService::list ) );

        return new ModelAndView ( "list", model );
    }

    @RequestMapping ( value = "/create", method = RequestMethod.GET )
    public ModelAndView create ()
    {
        final Map<String, Object> model = new HashMap<> ();

        model.put ( "command", new AccessTokenBean () );

        return new ModelAndView ( "create", model );
    }

    @RequestMapping ( value = "/create", method = RequestMethod.POST )
    public ModelAndView createPost ( @FormData ( "command" ) final AccessTokenBean command, final BindingResult result )
    {
        final Map<String, Object> model = new HashMap<> ();

        if ( result.hasErrors () )
        {
            return new ModelAndView ( "create", model );
        }

        this.accessTokenService.createAccessToken ( command.getDescription () );

        return ModelAndView.redirect ( "/accessToken" );
    }

    @RequestMapping ( value = "/{id}/edit", method = RequestMethod.GET )
    public ModelAndView edit ( @PathVariable ( "id" ) final String id )
    {
        final Optional<AccessToken> token = this.accessTokenService.getToken ( id );

        if ( !token.isPresent () )
        {
            return CommonController.createNotFound ( "access token", id );
        }

        final Map<String, Object> model = new HashMap<> ();

        final AccessTokenBean command = new AccessTokenBean ();
        command.setDescription ( token.get ().getDescription () );
        model.put ( "command", command );
        model.put ( "token", token.get () );

        return new ModelAndView ( "edit", model );
    }

    @RequestMapping ( value = "/{id}/edit", method = RequestMethod.POST )
    public ModelAndView editPost ( @PathVariable ( "id" ) final String id, @FormData ( "command" ) final AccessTokenBean command, final BindingResult result )
    {
        final Optional<AccessToken> token = this.accessTokenService.getToken ( id );
        if ( !token.isPresent () )
        {
            return CommonController.createNotFound ( "access token", id );
        }

        final Map<String, Object> model = new HashMap<> ();

        if ( result.hasErrors () )
        {
            model.put ( "token", token.get () );
            return new ModelAndView ( "edit", model );
        }

        this.accessTokenService.editAccessToken ( id, command.getDescription () );

        return ModelAndView.redirect ( "/accessToken" );
    }

    @RequestMapping ( value = "/{id}/delete", method = RequestMethod.GET )
    public ModelAndView delete ( @PathVariable ( "id" ) final String id )
    {
        this.accessTokenService.deleteAccessToken ( id );
        return ModelAndView.redirect ( "/accessToken" );
    }
}
