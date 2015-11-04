package org.eclipse.packagedrone.utils.deb;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class ControlFileWriter
{
    private final Appendable writer;

    private final Map<String, FieldFormatter> alternateFormatters;

    public ControlFileWriter ( final OutputStream os )
    {
        this ( os, null );
    }

    public ControlFileWriter ( final Appendable writer )
    {
        this ( writer, null );
    }

    public ControlFileWriter ( final OutputStream os, final Map<String, FieldFormatter> alternateFormatters )
    {
        this ( new OutputStreamWriter ( os, StandardCharsets.UTF_8 ), alternateFormatters );
    }

    public ControlFileWriter ( final Appendable writer, final Map<String, FieldFormatter> alternateFormatters )
    {
        this.writer = writer;
        this.alternateFormatters = alternateFormatters == null ? Collections.emptyMap () : alternateFormatters;
    }

    public void writeEntries ( final Map<String, String> entries ) throws IOException
    {
        for ( final Map.Entry<String, String> entry : entries.entrySet () )
        {
            writeEntry ( entry.getKey (), entry.getValue () );
        }
    }

    public void writeEntry ( final String key, final String value ) throws IOException
    {
        writeEntry ( key, value, Optional.empty () );
    }

    public void writeEntry ( final String key, final String value, final Optional<FieldFormatter> overrideFormatter ) throws IOException
    {
        final FieldFormatter formatter = overrideFormatter.orElseGet ( () -> this.alternateFormatters.getOrDefault ( key, FieldFormatter.SINGLE ) );
        formatter.append ( key, value, this.writer );
        this.writer.append ( '\n' );
    }
}
