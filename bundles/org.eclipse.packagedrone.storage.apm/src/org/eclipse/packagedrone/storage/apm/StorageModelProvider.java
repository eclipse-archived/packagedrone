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

public interface StorageModelProvider<V, W>
{
    public V getViewModel ();

    public void start ( StorageContext context ) throws Exception;

    public void stop ();

    public W cloneWriteModel ();

    public V makeViewModel ( Object writeModel );

    public void persistWriteModel ( W model ) throws Exception;

    public default void closeWriteModel ( final W model )
    {
    }
}
