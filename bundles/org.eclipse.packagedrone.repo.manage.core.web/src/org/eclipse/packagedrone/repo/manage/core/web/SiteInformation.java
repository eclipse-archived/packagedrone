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
package org.eclipse.packagedrone.repo.manage.core.web;

import org.eclipse.packagedrone.repo.MetaKeyBinding;
import org.hibernate.validator.constraints.URL;

public class SiteInformation
{
    @MetaKeyBinding ( namespace = "core", key = "site-prefix" )
    @URL
    private String prefix;

    @MetaKeyBinding ( namespace = "core", key = "allow-self-registration" )
    private boolean allowSelfRegistration;

    @MetaKeyBinding ( namespace = "recaptcha", key = "recaptcha-site-key" )
    private String recaptchaSiteKey;

    @MetaKeyBinding ( namespace = "recaptcha", key = "recaptcha-secret-key" )
    private String recaptchaSecretKey;

    public void setAllowSelfRegistration ( final boolean disableRegistration )
    {
        this.allowSelfRegistration = disableRegistration;
    }

    public boolean isAllowSelfRegistration ()
    {
        return this.allowSelfRegistration;
    }

    public void setPrefix ( final String prefix )
    {
        this.prefix = prefix;
    }

    public String getPrefix ()
    {
        return this.prefix;
    }

    public void setRecaptchaSiteKey ( final String recaptchaSiteKey )
    {
        this.recaptchaSiteKey = recaptchaSiteKey;
    }

    public String getRecaptchaSiteKey ()
    {
        return this.recaptchaSiteKey;
    }

    public void setRecaptchaSecretKey ( final String recaptchaSecretKey )
    {
        this.recaptchaSecretKey = recaptchaSecretKey;
    }

    public String getRecaptchaSecretKey ()
    {
        return this.recaptchaSecretKey;
    }

}
