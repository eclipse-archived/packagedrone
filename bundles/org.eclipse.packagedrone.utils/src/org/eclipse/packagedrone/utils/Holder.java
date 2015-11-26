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
package org.eclipse.packagedrone.utils;

/**
 * Hold a value
 *
 * @param <T>
 *            the actual type
 */
public class Holder<T>
{
    /**
     * Holds the actual value
     * <p>
     * This field is explicitly public
     * </p>
     */
    public T value;

    public Holder ()
    {
    }

    public Holder ( final T value )
    {
        this.value = value;
    }

    public T getValue ()
    {
        return this.value;
    }

    public void setValue ( final T value )
    {
        this.value = value;
    }
}
