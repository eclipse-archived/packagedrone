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
package org.eclipse.packagedrone.sec.web.ui.internal;

import org.eclipse.packagedrone.sec.web.captcha.CaptchaService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator
{
    private static Activator INSTANCE;

    private ServiceTracker<CaptchaService, CaptchaService> captchaServiceTracker;

    @Override
    public void start ( final BundleContext context ) throws Exception
    {
        INSTANCE = this;
        this.captchaServiceTracker = new ServiceTracker<> ( context, CaptchaService.class, null );
        this.captchaServiceTracker.open ();
    }

    @Override
    public void stop ( final BundleContext context ) throws Exception
    {
        this.captchaServiceTracker.close ();
        this.captchaServiceTracker = null;
        INSTANCE = null;
    }

    public static CaptchaService getCaptchaService ()
    {
        return INSTANCE.captchaServiceTracker.getService ();
    }

}
