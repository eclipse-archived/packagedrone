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
package org.eclipse.packagedrone.repo.importer.aether;

import org.eclipse.aether.transfer.TransferCancelledException;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.transfer.TransferListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerTransferListener implements TransferListener
{

    private final static Logger logger = LoggerFactory.getLogger ( LoggerTransferListener.class );

    @Override
    public void transferCorrupted ( final TransferEvent event ) throws TransferCancelledException
    {

    }

    @Override
    public void transferFailed ( final TransferEvent event )
    {

    }

    @Override
    public void transferInitiated ( final TransferEvent event ) throws TransferCancelledException
    {
        logger.debug ( "transferInitiated - {}", event );
    }

    @Override
    public void transferProgressed ( final TransferEvent event ) throws TransferCancelledException
    {

    }

    @Override
    public void transferStarted ( final TransferEvent event ) throws TransferCancelledException
    {

    }

    @Override
    public void transferSucceeded ( final TransferEvent event )
    {

    }

}
