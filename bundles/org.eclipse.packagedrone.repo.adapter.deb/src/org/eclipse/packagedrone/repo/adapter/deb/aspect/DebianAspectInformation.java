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
package org.eclipse.packagedrone.repo.adapter.deb.aspect;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.adapter.deb.aspect.internal.DebianChannelAspectFactory;

public interface DebianAspectInformation
{
    public static final String ID = "deb";

    public static final MetaKey KEY_ARCH = new MetaKey ( DebianChannelAspectFactory.ID, "architecture" );

    public static final MetaKey KEY_PACKAGE = new MetaKey ( DebianChannelAspectFactory.ID, "package" );

    public static final MetaKey KEY_VERSION = new MetaKey ( DebianChannelAspectFactory.ID, "version" );

    public static final MetaKey KEY_CONTROL_JSON = new MetaKey ( DebianChannelAspectFactory.ID, "control.json" );

}
