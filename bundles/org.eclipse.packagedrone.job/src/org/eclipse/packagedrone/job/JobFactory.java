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
package org.eclipse.packagedrone.job;

public interface JobFactory
{
    public static final String FACTORY_ID = "package.drone.job.factoryId";

    public JobInstance createInstance ( String data ) throws Exception;

    public String encodeConfiguration ( Object data );

    public String makeLabel ( String data );

    public JobFactoryDescriptor getDescriptor ();
}
