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
package org.eclipse.packagedrone.utils.profiler;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.eclipse.packagedrone.utils.ProcessUtil;
import org.eclipse.packagedrone.utils.profiler.Profile.DurationEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XmlProfileDataHandler implements ProfileDataHandler
{
    private final static Logger logger = LoggerFactory.getLogger ( XmlProfileDataHandler.class );

    /**
     * 40 spaces, so that the indent method can use it for processing
     */
    private static char[] whitespaces = "                                                            ".toCharArray ();

    private final Path dir;

    private final AtomicInteger counter = new AtomicInteger ();

    private final Long pid;

    public XmlProfileDataHandler ( final Path dir )
    {
        this.dir = dir;
        this.pid = ProcessUtil.getProcessId ();

        if ( this.pid == null )
        {
            throw new IllegalStateException ( String.format ( "Unable to evaluate current process ID" ) );
        }

        logger.warn ( "Activating XML profile data handler: {}/{}", dir, this.pid );
    }

    private Path makeNextName () throws IOException
    {
        if ( !Files.exists ( this.dir ) )
        {
            if ( Files.exists ( this.dir.getParent () ) )
            {
                // if the parent exists, create the actual directory, but only then
                Files.createDirectory ( this.dir );
            }
        }
        return this.dir.resolve ( String.format ( "pid%d-%08d.xml", this.pid, this.counter.getAndIncrement () ) );
    }

    @Override
    public void handle ( final DurationEntry entry )
    {
        try
        {
            final XMLOutputFactory xml = XMLOutputFactory.newInstance ();

            final Path name = makeNextName ();
            try ( OutputStream stream = new BufferedOutputStream ( Files.newOutputStream ( name, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE ) ) )
            {
                final XMLStreamWriter xsw = xml.createXMLStreamWriter ( stream );

                xsw.writeStartDocument ();
                xsw.writeCharacters ( "\n\n" );
                xsw.writeStartElement ( "trace" );
                xsw.writeDefaultNamespace ( "http://packagedrone.org/profile/trace/v1.0" );

                xsw.writeCharacters ( "\n" );

                dumpEntry ( xsw, entry, 1 );

                xsw.writeEndElement ();
                xsw.writeCharacters ( "\n" ); // end white new line
                xsw.writeEndDocument ();
            }

            if ( !Boolean.getBoolean ( "drone.profile.xml.disableAnnounce" ) )
            {
                System.out.format ( "Wrote profile trace: %s ms - %s -> %s%n", entry.getDuration ().toMillis (), entry.getOperation (), name.toAbsolutePath () );
            }
        }
        catch ( final Exception e )
        {
            logger.warn ( "Failed to write profile trace", e );
        }
    }

    private void dumpEntry ( final XMLStreamWriter xsw, final DurationEntry entry, final int level ) throws XMLStreamException
    {
        indent ( xsw, level );

        final boolean empty = entry.getEntries ().isEmpty ();

        if ( empty )
        {
            xsw.writeEmptyElement ( "operation" );
        }
        else
        {
            xsw.writeStartElement ( "operation" );
        }

        // write attributes

        xsw.writeAttribute ( "name", entry.getOperation () );
        xsw.writeAttribute ( "duration", "" + entry.getDuration ().toMillis () );

        if ( !empty )
        {
            xsw.writeCharacters ( "\n" );
            dumpEntries ( xsw, entry.getEntries (), level + 1 );

            // end element

            indent ( xsw, level );
            xsw.writeEndElement ();
        }

        xsw.writeCharacters ( "\n" );
    }

    private void indent ( final XMLStreamWriter xsw, final int level ) throws XMLStreamException
    {
        xsw.writeCharacters ( whitespaces, 0, Math.min ( whitespaces.length, level * 2 ) );
    }

    private void dumpEntries ( final XMLStreamWriter xsw, final List<DurationEntry> entries, final int level ) throws XMLStreamException
    {
        for ( final DurationEntry entry : entries )
        {
            dumpEntry ( xsw, entry, level );
        }
    }

}
