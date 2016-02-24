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
package org.eclipse.packagedrone.utils.deb;

import java.io.IOException;

public enum FieldFormatter
{
    /**
     * A single line of text
     */
    SINGLE
    {
        @Override
        public void appendValue ( String value, final Appendable appendable ) throws IOException
        {
            if ( value == null )
            {
                return;
            }

            value = value.replaceAll ( "[\\n\\r]", "" );
            appendable.append ( value );
        }

        @Override
        public void append ( final String key, final String value, final Appendable appendable ) throws IOException
        {
            if ( key == null || value == null )
            {
                return;
            }
            appendable.append ( key ).append ( ':' );

            if ( !value.isEmpty () )
            {
                appendable.append ( ' ' );
            }

            appendValue ( value, appendable );
        }
    },

    /**
     * A multiline format
     * <p>
     * Newlines get prefixed with a space and empty line get replaced by a dot.
     * Whitespaces get preserved.
     * </p>
     */
    MULTI
    {
        @Override
        public void append ( final String key, final String value, final Appendable appendable ) throws IOException
        {
            if ( key == null || value == null )
            {
                return;
            }

            appendable.append ( key ).append ( ':' );

            final String[] lines = value.split ( "\\n" );
            if ( lines.length > 0 && !lines[0].isEmpty () )
            {
                appendable.append ( ' ' );
            }

            appendLines ( appendable, lines );
        }

        @Override
        public void appendValue ( final String value, final Appendable appendable ) throws IOException
        {
            if ( value == null )
            {
                return;
            }

            final String[] lines = value.split ( "\\n" );
            appendLines ( appendable, lines );
        }

        private void appendLines ( final Appendable appendable, final String[] lines ) throws IOException
        {
            for ( int i = 0; i < lines.length; i++ )
            {
                final String line = lines[i];

                if ( line.isEmpty () && i > 0 )
                {
                    // only append a dot-line if we already are in the second line
                    appendable.append ( " ." );
                }
                else if ( !line.isEmpty () )
                {
                    if ( i > 0 )
                    {
                        // only append the space when we are not in the first line and do have content
                        appendable.append ( ' ' );
                    }
                    appendable.append ( line );
                }

                if ( i < lines.length - 1 )
                {
                    // don't add a final new line
                    appendable.append ( '\n' );
                }
            }
        }
    };

    public abstract void appendValue ( final String value, final Appendable appendable ) throws IOException;

    public abstract void append ( String key, String value, Appendable appendable ) throws IOException;

    public String format ( final String key, final String value )
    {
        final StringBuilder sb = new StringBuilder ();
        try
        {
            append ( key, value, sb );
        }
        catch ( final IOException e )
        {
            // this should never ever happen
        }
        return sb.toString ();
    }

    public String formatValue ( final String value )
    {
        final StringBuilder sb = new StringBuilder ();
        try
        {
            appendValue ( value, sb );
        }
        catch ( final IOException e )
        {
            // this should never ever happen
        }
        return sb.toString ();
    }
}
