/*******************************************************************************
 * Copyright (c) 2014, 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.aspect;

public interface ChannelAspectFactory
{
    public static final String DESCRIPTION = "drone.aspect.description";

    public static final String REQUIRES = "drone.aspect.requires";

    public static final String DESCRIPTION_FILE = "drone.aspect.description.file";

    public static final String NAME = "drone.aspect.name";

    public static final String FACTORY_ID = "drone.aspect.id";

    public static final String GROUP_ID = "drone.aspect.group.id";

    public static final String VERSION = "drone.aspect.version";

    public ChannelAspect createAspect ();
}
