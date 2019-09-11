/*******************************************************************************
 * Copyright (c) 2019 Trident Systems, Inc.
 * This software was developed with U.S government funding in support of the above
 * contract.  Trident grants unlimited rights to modify, distribute and incorporate
 * our contributions to Eclipse Package Drone bound by the overall restrictions from
 * the parent Eclipse Public License v1.0 available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Walker Funk - Trident Systems Inc. - initial implementation
 *******************************************************************************/
package org.eclipse.packagedrone.utils.rpm.build;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.google.common.io.ByteStreams;
import com.google.common.io.CountingOutputStream;

public class PayloadStreamer implements AutoCloseable
{
    private final boolean autoFinish;

    private final Path tempFile;

    private final CountingOutputStream payloadCounter;

    private OutputStream fileStream;

    private boolean finished;

    private boolean closed;

    public PayloadStreamer ( final boolean autoFinish, DataInputStream in ) throws IOException
    {
        this.autoFinish = autoFinish;

        this.tempFile = Files.createTempFile ( "rpm-", null );

        try
        {
            this.fileStream = new BufferedOutputStream ( Files.newOutputStream ( this.tempFile, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING ) );

            this.payloadCounter = new CountingOutputStream ( this.fileStream );

            ByteStreams.copy( in, payloadCounter );
        }
        catch ( final IOException e )
        {
            Files.deleteIfExists ( this.tempFile );
            throw e;
        }
    }

    /**
     * Stop streaming payload data
     * <p>
     * If the streamer is already finished then nothing will happen
     * </p>
     *
     * @throws IOException
     *             in case of IO errors
     */
    public void finish () throws IOException
    {
        if ( this.finished )
        {
            return;
        }

        this.finished = true;

        this.payloadCounter.close ();
    }

    public long getPayloadSize () throws IOException
    {
        checkFinished ( true );

        return this.payloadCounter.getCount ();
    }

    public FileChannel openChannel () throws IOException
    {
        checkFinished ( true );

        return FileChannel.open ( this.tempFile, StandardOpenOption.READ );
    }

    private void checkFinished ( final boolean allowAutoFinish ) throws IOException
    {
        if ( !this.finished && this.autoFinish && allowAutoFinish )
        {
            finish ();
        }

        if ( !this.finished )
        {
            throw new IllegalStateException ( "Recoderd has to be finished before accessing payload information or data" );
        }
        if ( this.closed )
        {
            throw new IllegalStateException ( "Recorder is already closed" );
        }
    }

    @Override
    public void close () throws IOException
    {
        this.closed = true;

        try
        {
            // simply close the file stream

            if ( this.fileStream != null )
            {
                this.fileStream.close ();
            }
        }
        finally
        {
            // and delete the temp file

            Files.deleteIfExists ( this.tempFile );
        }
    }
}
