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
package org.eclipse.packagedrone.repo.web.sitemap;

import static java.util.Optional.empty;

import java.time.Instant;
import java.util.Optional;

public interface UrlSetContext
{
    public default void addLocation ( final String localUrl )
    {
        addLocation ( localUrl, empty (), empty (), empty () );
    }

    public void addLocation ( String localUrl, Optional<Instant> lastModification, Optional<ChangeFrequency> changeFrequency, Optional<Double> priority );
}
