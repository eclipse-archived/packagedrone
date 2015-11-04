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
package org.eclipse.packagedrone.repo.channel.transfer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.packagedrone.repo.channel.ChannelId;

public interface TransferService
{
    public void exportChannel ( String channelId, OutputStream stream ) throws IOException;

    public void exportAll ( OutputStream stream ) throws IOException;

    public ChannelId importChannel ( InputStream inputStream, boolean useName ) throws IOException;

    public void importAll ( InputStream stream, boolean useNames, boolean wipe ) throws IOException;
}
