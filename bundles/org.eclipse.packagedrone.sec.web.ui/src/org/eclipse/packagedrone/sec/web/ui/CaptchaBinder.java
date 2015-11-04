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

import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.packagedrone.sec.web.captcha.CaptchaResult;
import org.eclipse.packagedrone.sec.web.captcha.CaptchaService;
import org.eclipse.packagedrone.sec.web.ui.internal.Activator;
import org.eclipse.packagedrone.utils.converter.ConverterManager;
import org.eclipse.packagedrone.web.controller.AbstractControllerBinder;
import org.eclipse.packagedrone.web.controller.binding.BindTarget;
import org.eclipse.packagedrone.web.controller.binding.Binding;
import org.eclipse.packagedrone.web.controller.binding.BindingError;
import org.eclipse.packagedrone.web.controller.binding.BindingManager;
import org.eclipse.packagedrone.web.controller.binding.MessageBindingError;
import org.eclipse.packagedrone.web.controller.binding.SimpleBinding;
import org.eclipse.packagedrone.web.controller.binding.SimpleBindingResult;

/**
 * A captcha binder
 * <p>
 * This binder will bind to {@link CaptchaResult} by validating the
 * {@link HttpServletRequest} with the {@link CaptchaService}.
 * </p>
 * <p>
 * On order to actually use this binder you will need to:
 * </p>
 * <ul>
 * <li>Add the binder to the controller method call by using
 * <code>@ControllerBinder(CaptchaBinder.class)</code></li>
 * <li>Add a parameter of type {@link CaptchaResult} to the method</li>
 * </ul>
 * <p>
 * The result of the captcha check will also be added as a binding result.
 * </p>
 */
public class CaptchaBinder extends AbstractControllerBinder
{
    private HttpServletRequest request;

    @Initializer
    public void setRequest ( final HttpServletRequest request )
    {
        this.request = request;
    }

    protected CaptchaService getService ()
    {
        return Activator.getCaptchaService ();
    }

    @Override
    public Binding performBind ( final BindTarget target, final ConverterManager converter, final BindingManager bindingManager )
    {
        final Class<?> type = target.getType ();
        if ( !type.isAssignableFrom ( CaptchaResult.class ) )
        {
            return null;
        }

        final CaptchaService service = getService ();
        if ( service == null )
        {
            // if there is no service registered, it is "ok"
            return new SimpleBinding ( CaptchaResult.OK );
        }

        final CaptchaResult result = getService ().checkCaptcha ( this.request );

        final SimpleBindingResult bindingResult = new SimpleBindingResult ();

        if ( !result.isSuccess () )
        {
            final List<BindingError> errors = result.getErrorMessages ().stream ().map ( MessageBindingError::new ).collect ( Collectors.toList () );
            bindingResult.addErrors ( getName (), errors );
        }

        return new SimpleBinding ( result, bindingResult );
    }

    private String getName ()
    {
        return getParameters ().getOrDefault ( "name", "captcha" );
    }

}
