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
package org.eclipse.packagedrone.repo.adapter.p2.internal.aspect;

import static com.google.common.xml.XmlEscapers.xmlAttributeEscaper;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public abstract class AbstractWriter
{
    protected static final char NL = '\n';

    protected static final String IN = "  ";

    protected static final String IN2 = IN + IN;

    private final String basename;

    private final String title;

    private final String type;

    private final boolean compressed;

    private final Map<String, String> properties;

    public AbstractWriter ( final String basename, final String title, final String type, final Instant timestamp, final boolean compressed, final Map<String, String> additionalProperties )
    {
        this.basename = basename;
        this.title = title;
        this.type = type;
        this.compressed = compressed;

        this.properties = new LinkedHashMap<> ();

        if ( compressed )
        {
            this.properties.put ( "p2.compressed", "true" );
        }

        this.properties.put ( "p2.timestamp", "" + timestamp.toEpochMilli () );

        if ( additionalProperties != null )
        {
            this.properties.putAll ( additionalProperties );
        }
    }

    public String getId ()
    {
        return this.basename + ( this.compressed ? ".jar" : ".xml" );
    }

    public String getMimeType ()
    {
        return this.compressed ? "application/zip" : "application/xml";
    }

    public void write ( final OutputStream stream ) throws IOException
    {
        if ( this.compressed )
        {
            final ZipOutputStream zos = new ZipOutputStream ( stream );
            zos.putNextEntry ( new ZipEntry ( this.basename + ".xml" ) );
            writeAll ( zos );
            zos.closeEntry ();
            zos.finish ();
        }
        else
        {
            writeAll ( stream );
        }
    }

    private void writeAll ( final OutputStream stream ) throws IOException
    {
        final PrintWriter out = new PrintWriter ( new OutputStreamWriter ( stream, StandardCharsets.UTF_8 ) );
        writeHeader ( out );
        writeContent ( out );
        writeFooter ( out );
        out.flush ();
    }

    protected void writeHeader ( final PrintWriter out ) throws IOException
    {
        out.append ( "<?xml version='1.0' encoding='UTF-8'?>" ).append ( NL );
        out.append ( "<?metadataRepository version='1.1.0'?>" ).append ( NL );
        out.format ( "<repository name='%s' type='%s' version='1'>", xmlAttributeEscaper ().escape ( this.title ), xmlAttributeEscaper ().escape ( this.type ) ).append ( NL );

        if ( !this.properties.isEmpty () )
        {
            out.append ( IN ).format ( "<properties size='%s'>", this.properties.size () ).append ( NL );
            for ( final Map.Entry<String, String> entry : this.properties.entrySet () )
            {
                out.append ( IN2 ).format ( "<property name='%s' value='%s'/>", xmlAttributeEscaper ().escape ( entry.getKey () ), xmlAttributeEscaper ().escape ( entry.getValue () ) ).append ( NL );
            }
            out.append ( IN ).append ( "</properties>" ).append ( NL );
        }
    }

    protected abstract void writeContent ( PrintWriter out ) throws IOException;

    protected void writeFooter ( final PrintWriter out ) throws IOException
    {
        out.append ( "</repository>" ).append ( NL );
    }

}
