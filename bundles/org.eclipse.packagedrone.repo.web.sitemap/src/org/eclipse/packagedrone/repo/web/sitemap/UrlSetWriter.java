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
package org.eclipse.packagedrone.repo.web.sitemap;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class UrlSetWriter implements AutoCloseable, UrlSetContext
{

    private static final String NS = "http://www.sitemaps.org/schemas/sitemap/0.9";

    private static final String NL = "\n";

    private static final String IN = "  ";

    private static final String IN2 = IN + IN;

    private static DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern ( "yyyy-MM-dd'T'hh:mm:ssXXX", Locale.ROOT );

    private final String urlPrefix;

    private final XMLStreamWriter out;

    private boolean finished;

    private final Writer writer;

    public UrlSetWriter ( final Writer writer, final String urlPrefix, final XMLOutputFactory outputFactory ) throws IOException
    {
        this.writer = writer;
        this.urlPrefix = urlPrefix;

        try
        {
            this.out = outputFactory.createXMLStreamWriter ( writer );
        }
        catch ( final XMLStreamException e )
        {
            throw new IOException ( e );
        }

        writeStart ();
    }

    private void writeEnd () throws IOException
    {
        try
        {
            this.out.writeEndElement ();
            this.out.writeEndDocument ();
        }
        catch ( final XMLStreamException e )
        {
            throw new IOException ( e );
        }
    }

    private void writeStart () throws IOException
    {
        try
        {
            this.out.writeStartDocument ( "UTF-8", "1.0" );
            this.out.writeCharacters ( NL );

            this.out.writeStartElement ( "urlset" );
            this.out.writeDefaultNamespace ( NS );
            this.out.writeCharacters ( NL );
        }
        catch ( final XMLStreamException e )
        {
            throw new IOException ( e );
        }
    }

    @Override
    public void addLocation ( String localUrl, final Optional<Instant> lastModification, final Optional<ChangeFrequency> changeFrequency, final Optional<Double> priority )
    {
        if ( !localUrl.startsWith ( "/" ) )
        {
            localUrl = "/" + localUrl;
        }

        try
        {
            this.out.writeCharacters ( IN );
            this.out.writeStartElement ( "url" );
            this.out.writeCharacters ( NL );

            writeTag ( this.out, "loc", URI.create ( this.urlPrefix + localUrl ).toASCIIString () );

            if ( lastModification.isPresent () )
            {
                writeTag ( this.out, "lastmod", FORMAT.format ( lastModification.get ().atZone ( ZoneId.systemDefault () ) ) );
            }
            if ( changeFrequency.isPresent () )
            {
                writeTag ( this.out, "changefreq", changeFrequency.get ().getValue () );
            }
            if ( priority.isPresent () )
            {
                final double value = Math.min ( Math.max ( 0.0, priority.get () ), 1.0 );
                writeTag ( this.out, "priority", String.format ( "%.1f", value ) );
            }

            this.out.writeCharacters ( IN );
            this.out.writeEndElement ();
        }
        catch ( final Exception e )
        {
            throw new RuntimeException ( e );
        }
    }

    private void writeTag ( final XMLStreamWriter out, final String tagName, final String value ) throws XMLStreamException
    {
        out.writeCharacters ( IN2 );
        out.writeStartElement ( tagName );
        out.writeCharacters ( value );
        out.writeEndElement ();
        out.writeCharacters ( NL );
    }

    /**
     * Complete the document but don't close the underlying writer
     *
     * @throws IOException
     *             if there is an IO error
     */
    public void finish () throws IOException
    {
        if ( !this.finished )
        {
            this.finished = true;
            writeEnd ();
        }
        try
        {
            this.out.close ();
        }
        catch ( final XMLStreamException e )
        {
            throw new IOException ( e );
        }
    }

    /**
     * Complete the document and close the underlying writer
     *
     * @throws IOException
     *             if there is an IO error
     */
    @Override
    public void close () throws IOException
    {
        try
        {
            finish ();
        }
        finally
        {
            this.writer.close ();
        }
    }
}
