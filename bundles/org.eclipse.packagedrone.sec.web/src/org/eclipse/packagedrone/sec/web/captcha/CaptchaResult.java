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
package org.eclipse.packagedrone.sec.web.captcha;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.scada.utils.ExceptionHelper;

public interface CaptchaResult
{
    public static final CaptchaResult OK = new CaptchaResult () {

        @Override
        public List<String> getErrorMessages ()
        {
            return Collections.emptyList ();
        }
    };

    public List<String> getErrorMessages ();

    public default boolean isSuccess ()
    {
        return getErrorMessages ().isEmpty ();
    }

    public static CaptchaResult errorResult ( final Collection<String> messages )
    {
        return errorResult ( new ArrayList<> ( messages ) );
    }

    public static CaptchaResult errorResult ( final List<String> messages )
    {
        return new CaptchaResult () {
            @Override
            public List<String> getErrorMessages ()
            {
                return messages;
            }
        };
    }

    public static CaptchaResult errorResult ( final String... messages )
    {
        return errorResult ( Arrays.asList ( messages ) );
    }

    public static CaptchaResult exceptionResult ( final Throwable error )
    {
        final String message = String.format ( "Captcha error: %s", ExceptionHelper.getMessage ( error ) );
        return errorResult ( message );
    }
}
