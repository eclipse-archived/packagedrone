/*******************************************************************************
 * Copyright (c) 2014, 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.web.forms.tags;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

import com.google.common.escape.Escaper;
import com.google.common.html.HtmlEscapers;

public class WriterHelper
{
    private final PageContext pageContext;

    private final JspWriter out;

    private final Escaper escaper = HtmlEscapers.htmlEscaper ();

    public WriterHelper ( final PageContext pageContext )
    {
        this.pageContext = pageContext;
        this.out = this.pageContext.getOut ();
    }

    public void write ( final String string ) throws JspException
    {
        try
        {
            this.out.write ( string );
        }
        catch ( final IOException e )
        {
            throw new JspException ( e );
        }
    }

    public void writeOptionalAttribute ( final String name, final Object value ) throws JspException
    {
        writeOptionalAttribute ( name, value, true );
    }

    public void writeOptionalAttribute ( final String name, final Object value, final boolean leadingSpace ) throws JspException
    {
        if ( value != null )
        {
            writeAttribute ( name, value, leadingSpace );
        }
    }

    public void writeEscaped ( final Object value ) throws JspException
    {
        if ( value != null )
        {
            write ( this.escaper.escape ( value.toString () ) );
        }
    }

    public void writeAttribute ( final String name, final Object value ) throws JspException
    {
        writeAttribute ( name, value, true );
    }

    public void writeAttribute ( final String name, final Object value, final boolean leadingSpace ) throws JspException
    {
        final StringBuilder sb = new StringBuilder ();
        if ( leadingSpace )
        {
            sb.append ( ' ' );
        }

        final String strValue = value == null ? "" : value.toString ();

        sb.append ( name ).append ( "=\"" );
        sb.append ( this.escaper.escape ( strValue ) );
        sb.append ( '"' );

        write ( sb.toString () );
    }

    public void writeFlagAttribute ( final String name ) throws JspException
    {
        writeAttribute ( name, name );
    }

    public void writeFlagAttribute ( final String name, final boolean state ) throws JspException
    {
        if ( state )
        {
            writeFlagAttribute ( name );
        }
    }
}
