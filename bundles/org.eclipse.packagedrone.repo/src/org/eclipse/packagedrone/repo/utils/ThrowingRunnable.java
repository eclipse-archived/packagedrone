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
package org.eclipse.packagedrone.repo.utils;

/**
 * A {@link Runnable} which may throw {@link Exception}s and support
 * {@link FunctionalInterface}.
 */
@FunctionalInterface
public interface ThrowingRunnable
{
    public void run () throws Exception;
}
