/*******************************************************************************
 * Copyright (c) 2015 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.api.upload;

import org.ops4j.pax.web.service.WebContainer;

public class ServletInitializer
{
    private WebContainer webContainer;

    public void setWebContainer ( final WebContainer webContainer )
    {
        this.webContainer = webContainer;
    }

    public void start ()
    {
    }

    public void stop ()
    {
    }
}
