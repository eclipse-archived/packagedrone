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
package org.eclipse.packagedrone.utils;

import java.util.Optional;
import java.util.function.Function;

/**
 * Map only once
 * <p>
 * This class helps to map from a source value to a target value where many
 * mapping variants are possible. After the first successful mapping (the
 * mapping result is not {@code null}) further mapping calls will be ignored.
 * </p>
 *
 * @param <T>
 *            the type of the input type
 * @param <R>
 *            the type of the result type
 */
public class MapOnce<T, R>
{
    private final T value;

    private Optional<R> result = Optional.empty ();

    public MapOnce ( final T value )
    {
        this.value = value;
    }

    public void map ( final Function<T, Optional<R>> func )
    {
        if ( !this.result.isPresent () )
        {
            this.result = func.apply ( this.value );
        }
    }

    /**
     * Get the mapping result
     *
     * @return the mapping result, may be {@link Optional#empty()} but is never
     *         {@code null}
     */
    public Optional<R> get ()
    {
        return this.result;
    }
}
