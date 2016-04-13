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
package org.eclipse.packagedrone.utils.rpm.build;

import java.io.IOException;

@FunctionalInterface
public interface FileInformationProvider<T>
{
    public FileInformation provide ( T object ) throws IOException;

    public default FileInformationProvider<T> customize ( final FileInformationCustomizer<T> customizer )
    {
        if ( customizer == null )
        {
            return this;
        }

        return ( object ) -> {
            final FileInformation information = provide ( object );
            customizer.perform ( object, information );
            return information;
        };
    }

    public default FileInformationProvider<T> customize ( final SimpleFileInformationCustomizer customizer )
    {
        if ( customizer == null )
        {
            return this;
        }

        return ( object ) -> {
            final FileInformation information = provide ( object );
            customizer.perform ( information );
            return information;
        };
    }
}
