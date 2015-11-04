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

import org.eclipse.packagedrone.web.LinkTarget;

public interface JobFactoryDescriptor
{
    /**
     * Return the link target for viewing the result
     * 
     * @return the link target, or <code>null</code> if the job does not provide
     *         a result view
     */
    public LinkTarget getResultTarget ();
}
