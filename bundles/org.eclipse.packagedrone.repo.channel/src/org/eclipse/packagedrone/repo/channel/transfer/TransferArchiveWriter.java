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
package org.eclipse.packagedrone.repo.channel.transfer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.output.CloseShieldOutputStream;
import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.channel.transfer.internal.Properties;

public class TransferArchiveWriter implements AutoCloseable, TransferWriterEntryContext
{
    private final ZipOutputStream stream;

    public TransferArchiveWriter ( final OutputStream stream )
    {
        this.stream = new ZipOutputStream ( stream, StandardCharsets.UTF_8 );
    }

    @Override
    public TransferWriterEntryContext createEntry ( final String name, final Map<MetaKey, String> properties, final ContentProvider content ) throws IOException
    {
        return store ( Collections.emptyList (), name, properties, content );
    }

    private TransferWriterEntryContext store ( final List<String> parents, final String name, final Map<MetaKey, String> properties, final ContentProvider content ) throws IOException
    {
        final List<String> newParents = new ArrayList<> ( parents.size () );
        newParents.addAll ( parents );
        newParents.add ( name );

        final String basename = makeBaseName ( newParents );

        addEntry ( basename + "/properties.json", output -> writeProperties ( properties, output ) );
        addEntry ( basename + "/content", content );

        return new TransferWriterEntryContext () {

            @Override
            public TransferWriterEntryContext createEntry ( final String name, final Map<MetaKey, String> properties, final ContentProvider content ) throws IOException
            {
                return store ( newParents, name, properties, content );
            }
        };
    }

    private void addEntry ( final String name, final ContentProvider provider ) throws IOException
    {
        this.stream.putNextEntry ( new ZipEntry ( name ) );

        provider.provide ( new CloseShieldOutputStream ( this.stream ) );

        this.stream.closeEntry ();
    }

    protected void writeProperties ( Map<MetaKey, String> properties, final OutputStream stream ) throws IOException
    {
        if ( properties == null )
        {
            properties = Collections.emptyMap ();
        }

        Properties.write ( properties, new OutputStreamWriter ( stream, StandardCharsets.UTF_8 ) );
    }

    private String makeBaseName ( final List<String> parents )
    {
        final StringBuilder sb = new StringBuilder ();

        for ( final String segment : parents )
        {
            sb.append ( "/artifacts/" ).append ( segment );
        }

        return sb.toString ();
    }

    @Override
    public void close () throws IOException
    {
        this.stream.close ();
    }

}
