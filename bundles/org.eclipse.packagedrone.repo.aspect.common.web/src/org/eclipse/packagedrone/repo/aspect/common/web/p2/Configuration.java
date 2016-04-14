/*******************************************************************************
 * Copyright (c) 2016 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.aspect.common.web.p2;

import org.eclipse.packagedrone.repo.MetaKeyBinding;

public class Configuration
{
    @MetaKeyBinding ( namespace = "p2.unzip", key = "reuse-metadata" )
    private boolean extractMetadata;

    public void setExtractMetadata ( final boolean extractMetadata )
    {
        this.extractMetadata = extractMetadata;
    }

    public boolean isExtractMetadata ()
    {
        return this.extractMetadata;
    }
}
