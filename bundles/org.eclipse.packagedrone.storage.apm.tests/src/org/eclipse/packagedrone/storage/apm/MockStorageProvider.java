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
package org.eclipse.packagedrone.storage.apm;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import org.eclipse.packagedrone.storage.apm.AbstractSimpleStorageModelProvider;
import org.eclipse.packagedrone.storage.apm.StorageContext;

public class MockStorageProvider extends AbstractSimpleStorageModelProvider<MockStorageViewModel, MockStorageModel>
{
    private final String key;

    private final String initialValue;

    public MockStorageProvider ( final String key, final String initialValue )
    {
        super ( MockStorageViewModel.class, MockStorageModel.class );

        this.key = key;
        this.initialValue = initialValue;
    }

    @Override
    public MockStorageViewModel makeViewModelTyped ( final MockStorageModel writeModel )
    {
        return new MockStorageViewModel ( writeModel );
    }

    @Override
    protected MockStorageModel cloneWriteModel ( final MockStorageModel writeModel )
    {
        return new MockStorageModel ( writeModel );
    }

    @Override
    protected void persistWriteModel ( final StorageContext context, final MockStorageModel writeModel ) throws Exception
    {
        Files.createDirectories ( context.getBasePath () );
        final Path path = context.getBasePath ().resolve ( this.key );

        try ( ObjectOutputStream os = new ObjectOutputStream ( Files.newOutputStream ( path ) ) )
        {
            os.writeObject ( writeModel );
        }
    }

    @Override
    protected MockStorageModel loadWriteModel ( final StorageContext context ) throws Exception
    {
        try ( ObjectInputStream is = new ObjectInputStream ( Files.newInputStream ( context.getBasePath ().resolve ( this.key ) ) ) )
        {
            return (MockStorageModel)is.readObject ();
        }
        catch ( final NoSuchFileException e )
        {
            return new MockStorageModel ( this.initialValue );
        }
    }

}
