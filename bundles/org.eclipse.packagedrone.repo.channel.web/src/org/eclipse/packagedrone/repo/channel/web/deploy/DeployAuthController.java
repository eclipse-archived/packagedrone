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
package org.eclipse.packagedrone.repo.channel.web.deploy;

import static com.google.common.net.UrlEscapers.urlPathSegmentEscaper;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.annotation.HttpConstraint;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.eclipse.packagedrone.repo.channel.deploy.DeployAuthService;
import org.eclipse.packagedrone.repo.channel.deploy.DeployGroup;
import org.eclipse.packagedrone.repo.channel.deploy.DeployKey;
import org.eclipse.packagedrone.repo.channel.web.breadcrumbs.Breadcrumbs;
import org.eclipse.packagedrone.repo.channel.web.breadcrumbs.Breadcrumbs.Entry;
import org.eclipse.packagedrone.sec.web.controller.HttpConstraints;
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
import org.eclipse.packagedrone.web.controller.ControllerInterceptor;
import org.eclipse.packagedrone.web.controller.binding.BindingResult;
import org.eclipse.packagedrone.web.controller.binding.PathVariable;
import org.eclipse.packagedrone.web.controller.binding.RequestParameter;
import org.eclipse.packagedrone.web.controller.form.FormData;

@Controller
@RequestMapping ( "/deploy/auth" )
@ViewResolver ( "/WEB-INF/views/deploy/auth/%s.jsp" )
@Secured
@ControllerInterceptor ( SecuredControllerInterceptor.class )
@HttpConstraint ( rolesAllowed = "MANAGER" )
@ControllerInterceptor ( HttpContraintControllerInterceptor.class )
public class DeployAuthController implements InterfaceExtender
{
    public final static Object GROUP_ACTION_TAG = new Object ();

    private static final int PAGE_SIZE = 25;

    private static final Method METHOD_LIST_GROUPS = LinkTarget.getControllerMethod ( DeployAuthController.class, "listGroups" );

    private static final Method METHOD_ADD_GROUP = LinkTarget.getControllerMethod ( DeployAuthController.class, "addGroup" );

    private DeployAuthService service;

    public void setService ( final DeployAuthService service )
    {
        this.service = service;
    }

    @Override
    public List<MenuEntry> getMainMenuEntries ( final HttpServletRequest request )
    {
        final List<MenuEntry> result = new LinkedList<> ();

        if ( HttpConstraints.isCallAllowed ( METHOD_LIST_GROUPS, request ) )
        {
            result.add ( new MenuEntry ( "Administration", 10_000, "Deploy Keys", 2_000, LinkTarget.createFromController ( METHOD_LIST_GROUPS ), null, null ) );
        }

        return result;
    }

    @Override
    public List<MenuEntry> getActions ( final HttpServletRequest request, final Object object )
    {
        final List<MenuEntry> result = new LinkedList<> ();

        if ( HttpConstraints.isCallAllowed ( METHOD_ADD_GROUP, request ) )
        {
            // TODO: check for explicit methods

            if ( GROUP_ACTION_TAG.equals ( object ) )
            {
                result.add ( new MenuEntry ( "Add group", 2_000, LinkTarget.createFromController ( METHOD_ADD_GROUP ), Modifier.PRIMARY, null ) );
            }
            else if ( object instanceof DeployGroup )
            {
                final DeployGroup dg = (DeployGroup)object;
                final Map<String, Object> model = new HashMap<> ();

                model.put ( "groupId", dg.getId () );

                result.add ( new MenuEntry ( "Create key", 2_000, LinkTarget.createFromController ( DeployAuthController.class, "createDeployKey" ).expand ( model ), Modifier.PRIMARY, null ) );
                result.add ( new MenuEntry ( "Edit", 3_000, LinkTarget.createFromController ( DeployAuthController.class, "editGroup" ).expand ( model ), Modifier.DEFAULT, null ) );
                result.add ( new MenuEntry ( "Delete", 4_000, LinkTarget.createFromController ( DeployAuthController.class, "deleteGroup" ).expand ( model ), Modifier.DANGER, "trash" ) );
            }
        }
        return result;
    }

    protected void addBreadcrumbs ( final String action, final String groupId, final Map<String, Object> model )
    {
        final List<Entry> entries = new LinkedList<> ();

        entries.add ( new Entry ( "Home", "/" ) );
        entries.add ( Breadcrumbs.create ( "Deploy Groups", DeployAuthController.class, "listGroups" ) );
        if ( groupId != null )
        {
            entries.add ( Breadcrumbs.create ( "Group", DeployAuthController.class, "viewGroup", Collections.singletonMap ( "groupId", groupId ) ) );
        }
        entries.add ( new Entry ( action ) );

        model.put ( "breadcrumbs", new Breadcrumbs ( entries ) );
    }

    @RequestMapping ( value = "/group", method = RequestMethod.GET )
    public ModelAndView listGroups ( @RequestParameter ( required = false, value = "position" ) Integer position)
    {
        final ModelAndView result = new ModelAndView ( "listGroups" );

        if ( position == null )
        {
            position = 0;
        }

        final List<DeployGroup> list = this.service.listGroups ( position, PAGE_SIZE + 1 );

        final boolean prev = position > 0;
        boolean next;

        if ( list.size () > PAGE_SIZE )
        {
            // check if we have more
            next = true;
            list.remove ( list.size () - 1 );
        }
        else
        {
            next = false;
        }

        result.put ( "groups", list );

        result.put ( "prev", prev );
        result.put ( "next", next );
        result.put ( "position", position );
        result.put ( "pageSize", PAGE_SIZE );

        return result;
    }

    @RequestMapping ( value = "/key", method = RequestMethod.GET )
    public ModelAndView listKeys ( @RequestParameter ( required = false, value = "position" ) Integer position)
    {
        final ModelAndView result = new ModelAndView ( "listKeys" );

        if ( position == null )
        {
            position = 0;
        }

        final List<DeployGroup> list = this.service.listGroups ( position, PAGE_SIZE + 1 );

        final boolean prev = position > 0;
        boolean next;

        if ( list.size () > PAGE_SIZE )
        {
            // check if we have more
            next = true;
            list.remove ( list.size () - 1 );
        }
        else
        {
            next = false;
        }

        result.put ( "keys", list );

        result.put ( "prev", prev );
        result.put ( "next", next );
        result.put ( "position", position );
        result.put ( "pageSize", PAGE_SIZE );

        return result;
    }

    @RequestMapping ( value = "/key/{keyId}/delete", method = RequestMethod.GET )
    public ModelAndView deleteKeyForGroup ( @PathVariable ( "keyId" ) final String keyId)
    {
        final DeployKey key = this.service.deleteDeployKey ( keyId );

        if ( key != null && key.getGroup ().getId () != null )
        {
            return new ModelAndView ( "redirect:/deploy/auth/group/" + urlPathSegmentEscaper ().escape ( key.getGroup ().getId () ) + "/view" );
        }
        else
        {
            return new ModelAndView ( "redirect:/deploy/auth/key" );
        }
    }

    @RequestMapping ( value = "/addGroup", method = RequestMethod.GET )
    public ModelAndView addGroup ()
    {
        final Map<String, Object> model = new HashMap<String, Object> ();

        addBreadcrumbs ( "Add group", null, model );

        return new ModelAndView ( "addGroup", model );
    }

    @RequestMapping ( value = "/group/{groupId}/delete", method = RequestMethod.GET )
    public String deleteGroup ( @PathVariable ( "groupId" ) final String groupId)
    {
        this.service.deleteGroup ( groupId );
        return "redirect:/deploy/auth/group";
    }

    @RequestMapping ( value = "/addGroup", method = RequestMethod.POST )
    public ModelAndView addGroupPost ( @RequestParameter ( "name" ) final String name)
    {
        try
        {
            this.service.createGroup ( name );
        }
        catch ( final Exception e )
        {
            return CommonController.createError ( "Create deploy group", "Failed to create deploy group", e );
        }
        return new ModelAndView ( "redirect:/deploy/auth/group" );
    }

    @RequestMapping ( value = "/group/{groupId}/view" )
    public ModelAndView viewGroup ( @PathVariable ( "groupId" ) final String groupId)
    {
        final DeployGroup group = this.service.getGroup ( groupId );

        if ( group == null )
        {
            return CommonController.createNotFound ( "Deploy Group", groupId );
        }

        final Map<String, Object> model = new HashMap<> ();

        model.put ( "group", group );
        addBreadcrumbs ( "View", null, model );

        return new ModelAndView ( "viewGroup", model );
    }

    @RequestMapping ( value = "/group/{groupId}/edit" )
    public ModelAndView editGroup ( @PathVariable ( "groupId" ) final String groupId)
    {
        final DeployGroup group = this.service.getGroup ( groupId );

        if ( group == null )
        {
            return CommonController.createNotFound ( "Deploy Group", groupId );
        }

        final Map<String, Object> model = new HashMap<> ();

        model.put ( "command", DeployGroupBean.fromGroup ( group ) );
        addBreadcrumbs ( "Edit", groupId, model );

        return new ModelAndView ( "editGroup", model );
    }

    @RequestMapping ( value = "/group/{groupId}/edit", method = RequestMethod.POST )
    public ModelAndView editGroupPost ( @PathVariable ( "groupId" ) final String groupId, @Valid @FormData ( "command" ) final DeployGroupBean group, final BindingResult result)
    {
        final Map<String, Object> model = new HashMap<> ( 1 );

        if ( !result.hasErrors () )
        {
            this.service.updateGroup ( groupId, group.getName () );
        }

        model.put ( "command", group );
        addBreadcrumbs ( "Edit", groupId, model );

        return new ModelAndView ( "editGroup", model );
    }

    @RequestMapping ( value = "/key/{keyId}/edit" )
    public ModelAndView editKey ( @PathVariable ( "keyId" ) final String keyId)
    {
        final DeployKey key = this.service.getDeployKey ( keyId );

        if ( key == null )
        {
            return CommonController.createNotFound ( "Deploy Key", keyId );
        }

        final Map<String, Object> model = new HashMap<> ();

        model.put ( "command", DeployKeyBean.fromKey ( key ) );

        return new ModelAndView ( "editKey", model );
    }

    @RequestMapping ( value = "/key/{keyId}/edit", method = RequestMethod.POST )
    public ModelAndView editKeyPost ( @PathVariable ( "keyId" ) final String keyId, @Valid @FormData ( "command" ) final DeployKeyBean key, final BindingResult result)
    {
        if ( !result.hasErrors () )
        {
            final DeployKey dk = this.service.updateDeployKey ( keyId, key.getName () );
            if ( dk != null && dk.getGroup ().getId () != null )
            {
                return new ModelAndView ( "redirect:/deploy/auth/group/" + urlPathSegmentEscaper ().escape ( dk.getGroup ().getId () ) + "/view" );
            }
        }

        return new ModelAndView ( "editKey", Collections.singletonMap ( "command", key ) );
    }

    @RequestMapping ( value = "/group/{groupId}/createKey" )
    public ModelAndView createDeployKey ( @PathVariable ( "groupId" ) final String groupId)
    {
        final DeployGroup group = this.service.getGroup ( groupId );

        if ( group == null )
        {
            return CommonController.createNotFound ( "Deploy Group", groupId );
        }

        final Map<String, Object> model = new HashMap<> ();

        model.put ( "group", group );
        addBreadcrumbs ( "Create key", groupId, model );

        return new ModelAndView ( "createDeployKey", model );
    }

    @RequestMapping ( value = "/group/{groupId}/createKey", method = RequestMethod.POST )
    public ModelAndView createDeployKeyPost ( @PathVariable ( "groupId" ) final String groupId, @RequestParameter (
            value = "name", required = false ) final String name)
    {
        try
        {
            this.service.createDeployKey ( groupId, name );
            return new ModelAndView ( "redirect:/deploy/auth/group/" + urlPathSegmentEscaper ().escape ( groupId ) + "/view" );
        }
        catch ( final Exception e )
        {
            return CommonController.createError ( "Create deploy key", "Failed to create deploy key", e );
        }
    }
}
