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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.input.CloseShieldInputStream;
import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.channel.transfer.internal.Properties;

public class TransferArchiveReader implements AutoCloseable
{
    @FunctionalInterface
    public interface Handler
    {
        /**
         * Handle the current entry
         * <p>
         * This method should handle the entry which is currently being
         * processed.
         * </p>
         * <p>
         * The method must return an ID of the artifact which is currently being
         * processed. This ID will be provided again to the method as
         * {@code parentId} for all direct children of the artifact. The ID
         * should be unique, but this is not enforced. It may even be
         * {@code null}, in which case the implementation may not know if it is
         * a root or child artifact. The ID must not be globally unique, but can
         * be unique for the context of the
         * {@link TransferArchiveReader#process(Handler)} call.
         * </p>
         *
         * @param parentId
         *            or {@code null} if it is a root artifacts.
         *            <strong>Note:</strong> this is the ID of the entry
         *            returned by a previous call to
         *            {@link Handler#handleEntry(String, String, Map, InputStream)}
         * @param artifactName
         *            the name of the artifact
         * @param metadata
         *            the meta data map, never {@code null}
         * @param stream
         *            the content stream, never {@code null}
         * @return a unique ID assigned by the method for further referencing,
         *         may be {@code null}
         * @throws IOException
         *             in case of any I/O errors
         */
        public String handleEntry ( String parentId, String artifactName, Map<MetaKey, String> metadata, InputStream stream ) throws IOException;
    }

    public abstract class SimpleHandler implements Handler
    {
        private long counter;

        @Override
        public String handleEntry ( final String parentId, final String artifactName, final Map<MetaKey, String> metadata, final InputStream stream ) throws IOException
        {
            simpleHandleEntry ( parentId, artifactName, metadata, stream );
            return Long.toString ( this.counter++ );
        }

        public abstract void simpleHandleEntry ( String parentId, String artifactName, Map<MetaKey, String> metadata, InputStream stream );
    }

    private final ZipInputStream stream;

    public TransferArchiveReader ( final InputStream stream )
    {
        this.stream = new ZipInputStream ( stream, StandardCharsets.UTF_8 );
    }

    public void process ( final Handler handler ) throws IOException
    {
        Objects.requireNonNull ( handler );

        final Map<List<String>, String> paths = new HashMap<> ();
        Map<MetaKey, String> properties = null;

        ZipEntry entry;
        while ( ( entry = this.stream.getNextEntry () ) != null )
        {
            final String name = entry.getName ();
            final LinkedList<String> path = makePath ( name );

            if ( path.isEmpty () )
            {
                throw new IllegalStateException ( "Invalid path entry: " + name );
            }

            if ( name.endsWith ( "/content" ) )
            {
                if ( paths.containsKey ( path ) )
                {
                    throw new IllegalStateException ( String.format ( "Artifact %s already processed", path.stream ().collect ( Collectors.joining ( " -> " ) ) ) );
                }
                if ( properties == null )
                {
                    throw new IllegalStateException ( "The 'properties.json' file must be stored before the 'content' stream" );
                }

                // make parent path and fetch ID

                final LinkedList<String> parentPath = new LinkedList<> ( path );
                parentPath.removeLast ();
                final String parentId = paths.get ( parentPath );

                // handle

                final String id = handler.handleEntry ( parentId, path.peekLast (), properties, new CloseShieldInputStream ( this.stream ) );

                // store ID

                paths.put ( path, id );

                // reset properties

                properties = null;

                // store
            }
            else if ( name.endsWith ( "/properties.json" ) )
            {
                // load properties
                properties = loadProperties ( new CloseShieldInputStream ( this.stream ) );
            }
            else
            {
                throw new IllegalStateException ( "Invalid path entry: " + name );
            }
        }
    }

    private Map<MetaKey, String> loadProperties ( final InputStream input ) throws IOException
    {
        return Properties.read ( new InputStreamReader ( new CloseShieldInputStream ( input ), StandardCharsets.UTF_8 ) );
    }

    protected LinkedList<String> makePath ( String fullName )
    {
        while ( fullName.startsWith ( "/" ) && !fullName.isEmpty () )
        {
            fullName = fullName.substring ( 1 );
        }

        final String[] toks = fullName.split ( "/" );

        final LinkedList<String> result = new LinkedList<> ();

        for ( int i = 0; i < toks.length / 2; i++ )
        {
            if ( !toks[i * 2].equals ( "artifacts" ) )
            {
                throw new IllegalStateException ( "artifact must be contained in 'artifacts' folder" );
            }
            final String name = toks[i * 2 + 1];
            result.add ( name );
        }

        return result;
    }

    @Override
    public void close () throws IOException
    {
        this.stream.close ();
    }
}
