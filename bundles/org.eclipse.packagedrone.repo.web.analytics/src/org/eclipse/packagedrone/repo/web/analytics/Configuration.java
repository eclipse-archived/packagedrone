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
package org.eclipse.packagedrone.repo.web.analytics;

import javax.validation.constraints.Pattern;

import org.eclipse.packagedrone.repo.MetaKeyBinding;

public class Configuration
{
    @Pattern ( regexp = "[a-zA-Z0-9-]*", message = "Invalid tracking ID" )
    @MetaKeyBinding ( namespace = Constants.NAMESPACE, key = Constants.KEY_TRACKING_ID )
    private String trackingId;

    @MetaKeyBinding ( namespace = Constants.NAMESPACE, key = Constants.KEY_ANONYMIZE_IP )
    private boolean anonymizeIp;

    @MetaKeyBinding ( namespace = Constants.NAMESPACE, key = Constants.KEY_FORCE_SSL )
    private boolean forceSsl;

    public void setTrackingId ( final String trackingId )
    {
        this.trackingId = trackingId;
    }

    public String getTrackingId ()
    {
        return this.trackingId;
    }

    public void setAnonymizeIp ( final boolean anonymizeIp )
    {
        this.anonymizeIp = anonymizeIp;
    }

    public boolean isAnonymizeIp ()
    {
        return this.anonymizeIp;
    }

    public void setForceSsl ( final boolean forceSsl )
    {
        this.forceSsl = forceSsl;
    }

    public boolean isForceSsl ()
    {
        return this.forceSsl;
    }
}
