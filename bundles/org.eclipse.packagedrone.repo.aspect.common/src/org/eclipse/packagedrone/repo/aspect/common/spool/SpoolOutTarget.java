/*******************************************************************************
 * Copyright (c) 2015 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.aspect.common.spool;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.packagedrone.utils.io.IOConsumer;

@FunctionalInterface
public interface SpoolOutTarget
{
    public void spoolOut ( final String fileName, final String mimeType, final IOConsumer<OutputStream> stream ) throws IOException;
}
