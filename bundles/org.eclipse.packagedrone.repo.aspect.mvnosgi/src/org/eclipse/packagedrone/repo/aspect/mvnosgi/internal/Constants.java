/*******************************************************************************
 * Copyright (c) 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Rathgeb - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.aspect.mvnosgi.internal;

public class Constants
{
    public static final String ASPECT_ID = "mvnosgi";

    public static final String DEFAULT_POM_EXTENSION = "pom";

    public static final String DEFAULT_POM_POSTIFX = "-" + ASPECT_ID + "." + DEFAULT_POM_EXTENSION;

    public static final String DEFAULT_POM_NAME = ASPECT_ID + "." + DEFAULT_POM_EXTENSION;

    public static final String DEFAULT_GROUPID = ASPECT_ID;

    public static final String METADATA_NAMESPACE = ASPECT_ID;

    public static final String METADATA_KEY_GROUPID = "groupId";

    private Constants ()
    {

    }
}
