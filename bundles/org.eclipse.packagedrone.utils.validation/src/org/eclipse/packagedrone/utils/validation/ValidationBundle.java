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
package org.eclipse.packagedrone.utils.validation;

import javax.validation.Configuration;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.bootstrap.GenericBootstrap;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class ValidationBundle implements BundleActivator
{

    private static OsgiMessageInterpolator messageInterpolator;

    private static OsgiValidationProviderTracker tracker;

    private static Validator validator;

    private static int lastTrackingCount;

    @Override
    public void start ( final BundleContext context ) throws Exception
    {
        messageInterpolator = new OsgiMessageInterpolator ( context );
        tracker = new OsgiValidationProviderTracker ();
        tracker.open ();
    }

    @Override
    public void stop ( final BundleContext context ) throws Exception
    {
        tracker.close ();
        messageInterpolator.dispose ();
        messageInterpolator = null;
    }

    public static OsgiMessageInterpolator getMessageInterpolator ()
    {
        return messageInterpolator;
    }

    public static javax.validation.Validator getValidator ()
    {
        synchronized ( ValidationBundle.class )
        {
            if ( lastTrackingCount != tracker.getTrackingCount () )
            {
                lastTrackingCount = tracker.getTrackingCount ();
                validator = null;
            }

            if ( validator == null )
            {
                validator = buildValidator ();
            }

            return validator;
        }
    }

    private static Validator buildValidator ()
    {
        final GenericBootstrap bootstrap = Validation.byDefaultProvider ();
        bootstrap.providerResolver ( tracker );

        final Configuration<?> cfg = bootstrap.configure ();
        final OsgiMessageInterpolator mi = getMessageInterpolator ();
        mi.setFallback ( cfg.getDefaultMessageInterpolator () );
        cfg.messageInterpolator ( mi );

        final ValidatorFactory factory = cfg.buildValidatorFactory ();
        return factory.getValidator ();
    }

}
