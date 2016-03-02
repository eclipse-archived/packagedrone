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
package org.eclipse.packagedrone.repo.signing.pgp.web.managed;

import static org.eclipse.packagedrone.repo.signing.pgp.PgpHelper.fromString;
import static org.eclipse.packagedrone.repo.signing.pgp.PgpHelper.streamSecretKeys;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.annotation.HttpConstraint;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.operator.bc.BcPBESecretKeyDecryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;
import org.eclipse.packagedrone.repo.signing.pgp.ManagedPgpFactory;
import org.eclipse.packagedrone.sec.web.controller.HttpContraintControllerInterceptor;
import org.eclipse.packagedrone.sec.web.controller.Secured;
import org.eclipse.packagedrone.sec.web.controller.SecuredControllerInterceptor;
import org.eclipse.packagedrone.web.Controller;
import org.eclipse.packagedrone.web.LinkTarget;
import org.eclipse.packagedrone.web.ModelAndView;
import org.eclipse.packagedrone.web.RequestMapping;
import org.eclipse.packagedrone.web.RequestMethod;
import org.eclipse.packagedrone.web.ViewResolver;
import org.eclipse.packagedrone.web.common.InterfaceExtender;
import org.eclipse.packagedrone.web.common.Modifier;
import org.eclipse.packagedrone.web.common.menu.MenuEntry;
import org.eclipse.packagedrone.web.common.page.Pagination;
import org.eclipse.packagedrone.web.controller.ControllerInterceptor;
import org.eclipse.packagedrone.web.controller.binding.BindingResult;
import org.eclipse.packagedrone.web.controller.binding.ExceptionError;
import org.eclipse.packagedrone.web.controller.binding.PathVariable;
import org.eclipse.packagedrone.web.controller.binding.RequestParameter;
import org.eclipse.packagedrone.web.controller.form.FormData;
import org.eclipse.packagedrone.web.controller.validator.ControllerValidator;
import org.eclipse.packagedrone.web.controller.validator.ValidationContext;
import org.eclipse.scada.utils.ExceptionHelper;

@Controller
@Secured
@ControllerInterceptor ( SecuredControllerInterceptor.class )
@HttpConstraint ( rolesAllowed = "ADMIN" )
@ControllerInterceptor ( HttpContraintControllerInterceptor.class )
@ViewResolver ( "/WEB-INF/views/managed/%s.jsp" )
@RequestMapping ( "/pgp.sign.managed" )
public class ServiceController implements InterfaceExtender
{
    public final static Object ACTION_TAG_PGP = new Object ();

    private ManagedPgpFactory factory;

    public void setFactory ( final ManagedPgpFactory factory )
    {
        this.factory = factory;
    }

    @Override
    public List<MenuEntry> getMainMenuEntries ( final HttpServletRequest request )
    {
        final List<MenuEntry> result = new LinkedList<> ();

        if ( request.isUserInRole ( "ADMIN" ) )
        {
            result.add ( new MenuEntry ( "Signing", 4_100, "PGP Signers (Managed)", 1_100, LinkTarget.createFromController ( ServiceController.class, "index" ), null, null ) );
        }

        return result;
    }

    @Override
    public List<MenuEntry> getActions ( final HttpServletRequest request, final Object object )
    {
        final List<MenuEntry> result = new LinkedList<> ();

        if ( object == ACTION_TAG_PGP )
        {
            if ( request.isUserInRole ( "ADMIN" ) )
            {
                result.add ( new MenuEntry ( "Add", 100, LinkTarget.createFromController ( ServiceController.class, "add" ), Modifier.PRIMARY, "plus" ) );
            }
        }

        return result;
    }

    @RequestMapping
    public ModelAndView index ( @RequestParameter ( value = "start",
            required = false ) final Integer start) throws Exception
    {
        final Map<String, Object> model = new HashMap<> ();

        model.put ( "configurations", Pagination.paginate ( start, 20, this.factory::list ) );

        return new ModelAndView ( "index", model );
    }

    @ControllerValidator ( formDataClass = AddEntry.class )
    public void validateKey ( final AddEntry entry, final ValidationContext context )
    {
        if ( entry != null && entry.getSecretKey () != null && !entry.getSecretKey ().isEmpty () )
        {
            try ( Stream<PGPSecretKey> s = streamSecretKeys ( fromString ( entry.getSecretKey () ) ).filter ( PGPSecretKey::isSigningKey ) )
            {
                final List<PGPSecretKey> keys = s.collect ( Collectors.toList () );
                if ( keys.size () <= 0 )
                {
                    context.error ( "secretKey", "Import does not contain secret signing keys" );
                }
                else
                {
                    final String passphrase = entry.getPassphrase () == null ? "" : entry.getPassphrase ();
                    for ( final PGPSecretKey key : keys )
                    {
                        try
                        {
                            key.extractPrivateKey ( new BcPBESecretKeyDecryptorBuilder ( new BcPGPDigestCalculatorProvider () ).build ( passphrase.toCharArray () ) );
                        }
                        catch ( final PGPException e )
                        {
                            context.error ( "secretKey", String.format ( "Failed to extract key. Passphrase might be wrong. (%s)", ExceptionHelper.getMessage ( e ) ) );
                        }
                    }
                }
            }
            catch ( final Exception e )
            {
                context.error ( "secretKey", new ExceptionError ( e ) );
            }
        }
    }

    @RequestMapping ( "/add" )
    public ModelAndView add ()
    {
        final Map<String, Object> model = new HashMap<> ();

        model.put ( "command", new AddEntry () );

        return new ModelAndView ( "add", model );
    }

    @RequestMapping ( value = "/add", method = RequestMethod.POST )
    public ModelAndView addPost ( @FormData ( "command" ) @Valid final AddEntry command, final BindingResult result)
    {
        final Map<String, Object> model = new HashMap<> ();

        if ( !result.hasErrors () )
        {
            // do stuff
            this.factory.createService ( command.getLabel (), command.getSecretKey (), command.getPassphrase () );
            return new ModelAndView ( "redirect:/pgp.sign.managed" );
        }
        else
        {
            return new ModelAndView ( "add", model );
        }
    }

    @RequestMapping ( "/{id}/delete" )
    public ModelAndView delete ( @PathVariable ( "id" ) final String id)
    {
        this.factory.deleteService ( id );
        return new ModelAndView ( "redirect:/pgp.sign.managed" );
    }
}
