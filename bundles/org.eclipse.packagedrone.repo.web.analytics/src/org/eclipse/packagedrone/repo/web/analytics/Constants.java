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

import org.eclipse.packagedrone.repo.MetaKey;

public final class Constants
{
    private Constants ()
    {
    }

    public static final String NAMESPACE = "ga";

    public static final String KEY_TRACKING_ID = "trackingId";

    public static final String KEY_ANONYMIZE_IP = "anonymizeIp";

    public static final String KEY_FORCE_SSL = "forceSSL";

    public static final MetaKey KEY = new MetaKey ( NAMESPACE, KEY_TRACKING_ID );
}
