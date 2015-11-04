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
package org.eclipse.packagedrone.repo.aspect.extract;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;

import org.eclipse.packagedrone.repo.aspect.ValidationContext;

public interface Extractor
{
    public interface Context extends ValidationContext
    {
        /**
         * Get the name of the uploaded artifact
         *
         * @return the name of the artifact
         */
        public String getName ();

        /**
         * Get the path to a temporary file where the BLOB is stored
         *
         * @return the path to the temporary BLOB file
         */
        public Path getPath ();

        public Instant getCreationTimestamp ();
    }

    public void extractMetaData ( Context context, Map<String, String> metadata ) throws Exception;
}
