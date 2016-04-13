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
import java.nio.channels.ReadableByteChannel;

public interface PayloadProvider
{
    /**
     * Open a new channel to the payload data
     * <p>
     * The caller is responsible for closing the resource
     * </p>
     *
     * @return the newly created channel
     * @throws IOException
     *             if opening the channels fails
     */
    public ReadableByteChannel openChannel () throws IOException;

    /**
     * The number of bytes of the compressed archive file
     *
     * @return the number of bytes of the compressed archive file
     * @throws IOException
     *             if anything goes wrong
     */
    public long getPayloadSize () throws IOException;

    /**
     * Get the number of bytes of the uncompressed payload archive
     *
     * @return the number of bytes of the uncompressed payload archive
     * @throws IOException
     *             if anything goes wrong
     */
    public long getArchiveSize () throws IOException;
}
