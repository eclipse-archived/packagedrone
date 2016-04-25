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
import java.util.Objects;

public class ProviderRule<T>
{
    private final Class<T> clazz;

    private final FileInformationProvider<T> provider;

    public ProviderRule ( final Class<T> clazz, final FileInformationProvider<T> provider )
    {
        this.clazz = clazz;
        this.provider = provider;
    }

    public FileInformation run ( final Object object, final PayloadEntryType type ) throws IOException
    {
        Objects.requireNonNull ( object );

        if ( this.clazz.isAssignableFrom ( object.getClass () ) )
        {
            return this.provider.provide ( this.clazz.cast ( object ), type );
        }
        return null;
    }
}
