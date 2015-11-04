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
package org.eclipse.packagedrone.repo.adapter.rpm;

import org.eclipse.packagedrone.repo.MetaKey;

public final class Constants
{
    public static final String GROUP_ID = "rpm";

    public static final String RPM_ASPECT_ID = "rpm";

    public static final String YUM_ASPECT_ID = "yum";

    public static final MetaKey KEY_INFO = new MetaKey ( RPM_ASPECT_ID, "info" );

    private Constants ()
    {
    }
}
