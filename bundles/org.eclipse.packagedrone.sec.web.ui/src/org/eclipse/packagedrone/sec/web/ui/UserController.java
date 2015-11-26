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
package org.eclipse.packagedrone.sec.web.ui;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.annotation.HttpConstraint;
import javax.servlet.annotation.ServletSecurity.EmptyRoleSemantic;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.eclipse.packagedrone.repo.channel.web.breadcrumbs.Breadcrumbs;
import org.eclipse.packagedrone.repo.channel.web.breadcrumbs.Breadcrumbs.Entry;
import org.eclipse.packagedrone.sec.CreateUser;
import org.eclipse.packagedrone.sec.DatabaseDetails;
import org.eclipse.packagedrone.sec.DatabaseUserInformation;
import org.eclipse.packagedrone.sec.UserDetails;
import org.eclipse.packagedrone.sec.UserStorage;
import org.eclipse.packagedrone.sec.web.controller.HttpConstraints;
import org.eclipse.packagedrone.sec.web.controller.HttpContraintControllerInterceptor;
import org.eclipse.packagedrone.sec.web.controller.Secured;
import org.eclipse.packagedrone.sec.web.controller.SecuredControllerInterceptor;
import org.eclipse.packagedrone.sec.web.filter.SecurityFilter;
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
@ViewResolver ( "/WEB-INF/views/%s.jsp" )
@RequestMapping ( "/user" )
@Secured
@ControllerInterceptor ( SecuredControllerInterceptor.class )
@HttpConstraint ( rolesAllowed = "ADMIN" )
@ControllerInterceptor ( HttpContraintControllerInterceptor.class )
public class UserController extends AbstractUserCreationController implements InterfaceExtender
{
    private static final Method METHOD_LIST = LinkTarget.getControllerMethod ( UserController.class, "list" );

    private static final Method METHOD_ADD_USER = LinkTarget.getControllerMethod ( UserController.class, "addUser" );

    @Override
    public List<MenuEntry> getMainMenuEntries ( final HttpServletRequest request )
    {
        final List<MenuEntry> result = new LinkedList<> ();

        if ( HttpConstraints.isCallAllowed ( METHOD_LIST, request ) )
        {
            result.add ( new MenuEntry ( "Administration", 10_000, "Users", 1_000, LinkTarget.createFromController ( METHOD_LIST ), null, null ) );
        }

        return result;
    }

    @Override
    public List<MenuEntry> getActions ( final HttpServletRequest request, final Object object )
    {
        if ( UserStorage.ACTION_TAG_USERS.equals ( object ) )
        {
            final List<MenuEntry> result = new LinkedList<MenuEntry> ();

            if ( HttpConstraints.isCallAllowed ( METHOD_ADD_USER, request ) )
            {
                result.add ( new MenuEntry ( "Add user", 100, LinkTarget.createFromController ( METHOD_ADD_USER ), Modifier.PRIMARY, null ) );
            }

            return result;
        }
        else if ( object instanceof DatabaseUserInformation )
        {
            final DatabaseDetails details = ( (DatabaseUserInformation)object ).getDetails ( DatabaseDetails.class );
            if ( details != null )
            {
                final List<MenuEntry> result = new LinkedList<MenuEntry> ();

                final Map<String, Object> model = new HashMap<> ( 1 );
                final String userId = ( (DatabaseUserInformation)object ).getId ();
                model.put ( "userId", userId );

                final boolean you = isYou ( userId, request );

                // TODO: check explicitly for methods
                if ( HttpConstraints.isCallAllowed ( METHOD_ADD_USER, request ) )
                {
                    result.add ( new MenuEntry ( "Edit user", 100, LinkTarget.createFromController ( UserController.class, "editUser" ).expand ( model ), Modifier.PRIMARY, null ) );

                    if ( !you )
                    {
                        if ( details.isLocked () )
                        {
                            result.add ( new MenuEntry ( "Unlock user", 200, LinkTarget.createFromController ( UserController.class, "unlockUser" ).expand ( model ), Modifier.SUCCESS, null ) );
                        }
                        else
                        {
                            result.add ( new MenuEntry ( "Lock user", 200, LinkTarget.createFromController ( UserController.class, "lockUser" ).expand ( model ), Modifier.WARNING, null ).makeModalMessage ( "Lock user", "This will prevent the user from logging in. It can be reveresed by unlocking the user." ) );
                        }

                        if ( !details.isDeleted () )
                        {
                            result.add ( new MenuEntry ( "Delete user", 300, LinkTarget.createFromController ( UserController.class, "deleteUser" ).expand ( model ), Modifier.DANGER, "trash" ).makeModalMessage ( "Delete user", "Are you sure you want to delete this user?" ) );
                        }
                    }
                }

                return result;
            }
        }
        return null;
    }

    @RequestMapping ( method = RequestMethod.GET )
    public ModelAndView list ( @RequestParameter ( required = false, value = "start" ) final Integer position)
    {
        final ModelAndView result = new ModelAndView ( "user/list" );
        result.put ( "users", Pagination.paginate ( position, 25, this.storage::list ) );
        return result;
    }

    @RequestMapping ( value = "/add", method = RequestMethod.GET )
    public ModelAndView addUser ()
    {
        final ModelAndView model = new ModelAndView ( "user/add" );

        model.put ( "command", new CreateUser () );

        return model;
    }

    @RequestMapping ( value = "/add", method = RequestMethod.POST )
    public ModelAndView addUserPost ( @Valid @FormData ( "command" ) final CreateUser data, final BindingResult result)
    {
        if ( result.hasErrors () )
        {
            final Map<String, Object> model = new HashMap<> ( 1 );
            model.put ( "command", data );
            return new ModelAndView ( "user/add", model );
        }

        final DatabaseUserInformation newUser = this.storage.createUser ( data, true );

        return new ModelAndView ( String.format ( "redirect:/user/%s/view", newUser.getId () ) );
    }

    @RequestMapping ( value = "/{userId}/view", method = RequestMethod.GET )
    @HttpConstraint ( value = EmptyRoleSemantic.PERMIT )
    public ModelAndView viewUser ( @PathVariable ( "userId" ) final String userId, final HttpServletRequest request)
    {
        final boolean you = isYou ( userId, request );

        if ( !you && !request.isUserInRole ( "ADMIN" ) )
        {
            return CommonController.createAccessDenied ();
        }

        final DatabaseUserInformation user = this.storage.getUserDetails ( userId );

        if ( user == null || user.getDetails ( DatabaseDetails.class ) == null )
        {
            return CommonController.createNotFound ( "user", userId );
        }

        final ModelAndView model = new ModelAndView ( "user/view" );
        model.put ( "user", user );
        model.put ( "you", you );
        return model;
    }

    protected boolean isYou ( final String userId, final HttpServletRequest request )
    {
        return userId.equals ( request.getRemoteUser () );
    }

    protected void addBreadcrumbs ( final String action, final String userId, final Map<String, Object> model )
    {
        model.put ( "breadcrumbs", new Breadcrumbs ( new Entry ( "Home", "/" ), Breadcrumbs.create ( "Users", UserController.class, "list" ), Breadcrumbs.create ( "User", UserController.class, "viewUser", "userId", userId ), new Entry ( action ) ) );
    }

    @RequestMapping ( value = "/{userId}/edit", method = RequestMethod.GET )
    public ModelAndView editUser ( @PathVariable ( "userId" ) final String userId)
    {
        final DatabaseUserInformation user = this.storage.getUserDetails ( userId );

        if ( user == null || user.getDetails ( DatabaseDetails.class ) == null )
        {
            return CommonController.createNotFound ( "user", userId );
        }

        final Map<String, Object> model = new HashMap<> ( 2 );

        model.put ( "user", user );

        final DatabaseDetails details = user.getDetails ( DatabaseDetails.class );

        final UserDetailsBean bean = new UserDetailsBean ();
        bean.setEmail ( details.getEmail () );
        bean.setName ( details.getName () );
        bean.setRoles ( new HashSet<> ( details.getRoles () ) /* we need a modifiable copy */ );

        model.put ( "command", bean );
        model.put ( "allRoles", makePossibleRoles ( details.getRoles () ) );

        addBreadcrumbs ( "Edit", userId, model );

        return new ModelAndView ( "user/edit", model );
    }

    private SortedSet<String> makePossibleRoles ( final Set<String> roles )
    {
        if ( roles == null )
        {
            return null;
        }

        final SortedSet<String> result = new TreeSet<> ();

        result.addAll ( roles );
        result.add ( "MANAGER" );
        result.add ( "ADMIN" );

        return result;
    }

    @RequestMapping ( value = "/{userId}/edit", method = RequestMethod.POST )
    public ModelAndView editUserPost ( @PathVariable ( "userId" ) final String userId, @Valid @FormData ( "command" ) final UserDetailsBean data, final BindingResult result, final HttpSession session)
    {
        final DatabaseUserInformation user = this.storage.getUserDetails ( userId );

        if ( user == null || user.getDetails ( DatabaseDetails.class ) == null )
        {
            return CommonController.createNotFound ( "user", userId );
        }

        if ( result.hasErrors () )
        {
            final Map<String, Object> model = new HashMap<> ( 2 );
            model.put ( "command", data );
            model.put ( "user", user );
            model.put ( "allRoles", makePossibleRoles ( data.getRoles () ) );

            addBreadcrumbs ( "Edit", userId, model );

            return new ModelAndView ( "user/edit", model );
        }

        this.storage.updateUser ( userId, new UserDetails ( data.getName (), data.getEmail (), data.getRoles () ) );

        // TODO: only reload if it was our own profile
        SecurityFilter.markReloadDetails ( session );

        return new ModelAndView ( String.format ( "redirect:/user/%s/view", userId ) );
    }

    @RequestMapping ( "/{userId}/lock" )
    public ModelAndView lockUser ( @PathVariable ( "userId" ) final String userId)
    {
        this.storage.lockUser ( userId );
        return new ModelAndView ( "redirect:/user/" + userId + "/view" );
    }

    @RequestMapping ( "/{userId}/unlock" )
    public ModelAndView unlockUser ( @PathVariable ( "userId" ) final String userId)
    {
        this.storage.unlockUser ( userId );
        return new ModelAndView ( "redirect:/user/" + userId + "/view" );
    }

    @RequestMapping ( "/{userId}/delete" )
    public ModelAndView deleteUser ( @PathVariable ( "userId" ) final String userId)
    {
        this.storage.deleteUser ( userId );
        return new ModelAndView ( "redirect:/user" );
    }

    @RequestMapping ( "/{userId}/newPassword" )
    @HttpConstraint ( value = EmptyRoleSemantic.PERMIT )
    public ModelAndView changePassword ( @PathVariable ( "userId" ) final String userId, final HttpServletRequest request)
    {
        final Map<String, Object> model = new HashMap<> ();

        final boolean you = isYou ( userId, request );
        if ( !you && !request.isUserInRole ( "ADMIN" ) )
        {
            return CommonController.createAccessDenied ();
        }

        final DatabaseUserInformation user = this.storage.getUserDetails ( userId );
        if ( user == null )
        {
            return CommonController.createNotFound ( "user", userId );
        }

        final DatabaseDetails details = user.getDetails ( DatabaseDetails.class );

        if ( details == null )
        {
            return CommonController.createNotFound ( "details", userId );
        }

        final NewPassword data = new NewPassword ();
        data.setEmail ( details.getEmail () );

        model.put ( "you", you );
        model.put ( "command", data );

        return new ModelAndView ( "user/newPassword", model );
    }

    @RequestMapping ( value = "/{userId}/newPassword", method = RequestMethod.POST )
    @HttpConstraint ( value = EmptyRoleSemantic.PERMIT )
    public ModelAndView changePasswordPost ( @PathVariable ( "userId" ) final String userId, @Valid @FormData ( "command" ) final NewPassword data, final BindingResult result, final HttpServletRequest request)
    {
        final boolean you = isYou ( userId, request );

        if ( !you && !request.isUserInRole ( "ADMIN" ) )
        {
            return CommonController.createAccessDenied ();
        }

        final Map<String, Object> model = new HashMap<> ();
        model.put ( "you", you );

        if ( result.hasErrors () )
        {
            model.put ( "command", data );
            return new ModelAndView ( "user/newPassword", model );
        }

        try
        {
            if ( !you /* but we are ADMIN */ )
            {
                this.storage.updatePassword ( userId, null, data.getPassword () );
            }
            else
            {
                this.storage.updatePassword ( userId, data.getCurrentPassword (), data.getPassword () );
            }

            return new ModelAndView ( "redirect:/user/" + userId + "/view" );
        }
        catch ( final Exception e )
        {
            return CommonController.createError ( "Error", "Failed to change password", e );
        }
    }
}
