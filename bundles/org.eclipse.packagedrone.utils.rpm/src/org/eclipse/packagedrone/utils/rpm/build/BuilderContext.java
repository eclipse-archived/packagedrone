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
package org.eclipse.packagedrone.utils.rpm.build;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public interface BuilderContext
{
    public static final String DEFAULT_USER = "root";

    public static final String DEFAULT_GROUP = "root";

    public static final class Directory
    {
        private Directory ()
        {
        }
    }

    public static final Directory DIRECTORY = new Directory ();

    public static FileInformationCustomizer<Path> pathCustomizer ()
    {
        return new FileInformationCustomizer<Path> () {

            @Override
            public void perform ( final Path path, final FileInformation information ) throws IOException
            {
                information.setTimestamp ( Files.getLastModifiedTime ( path ).toInstant () );
            }
        };
    }

    public static SimpleFileInformationCustomizer nowTimestampCustomizer ()
    {
        return Defaults.NOW_TIMESTAMP_CUSTOMIZER;
    }

    public static SimpleFileInformationCustomizer modeCustomizer ( final short mode )
    {
        return new SimpleFileInformationCustomizer () {

            @Override
            public void perform ( final FileInformation information )
            {
                information.setMode ( mode );
            }
        };
    }

    public static FileInformationProvider<Path> pathProvider ()
    {
        return Defaults.PATH_PROVIDER;
    }

    public static <T> FileInformationProvider<T> simpleProvider ( final int mode )
    {
        return new FileInformationProvider<T> () {

            @Override
            public FileInformation provide ( final T object ) throws IOException
            {
                return new FileInformation ();
            }
        }.customize ( nowTimestampCustomizer () ).customize ( modeCustomizer ( (short)mode ) );
    }

    @SuppressWarnings ( "unchecked" )
    public static <T> FileInformationProvider<T> simpleFileProvider ()
    {
        return (FileInformationProvider<T>)Defaults.SIMPLE_FILE_PROVIDER;
    }

    @SuppressWarnings ( "unchecked" )
    public static <T> FileInformationProvider<T> simpleDirectoryProvider ()
    {
        return (FileInformationProvider<T>)Defaults.SIMPLE_DIRECTORY_PROVIDER;
    }

    public static <T> FileInformationProvider<T> multiProvider ( final FileInformationProvider<Object> defaultProvider, final ProviderRule<?>... rules )
    {
        Objects.requireNonNull ( rules );

        return multiProvider ( defaultProvider, Arrays.asList ( rules ) );
    }

    public static <T> FileInformationProvider<T> multiProvider ( final FileInformationProvider<Object> defaultProvider, final List<ProviderRule<?>> rules )
    {
        Objects.requireNonNull ( defaultProvider );
        Objects.requireNonNull ( rules );

        return new FileInformationProvider<T> () {

            @Override
            public FileInformation provide ( final Object object ) throws IOException
            {
                for ( final ProviderRule<?> rule : rules )
                {
                    final FileInformation result = rule.run ( object );
                    if ( result != null )
                    {
                        return result;
                    }
                }
                return defaultProvider.provide ( object );
            }
        };
    }

    /**
     * Get a default information provider
     * <p>
     * This provider will only used provided information, set the access modes
     * to @{@code 0755} for directories and to {@code 0644} for all others. It
     * will use the default user ({@code root}) and group ({@code root}) and use
     * the current time as file timestamp.
     * </p>
     *
     * @return the default information provider
     */
    @SuppressWarnings ( "unchecked" )
    public static <T> FileInformationProvider<T> defaultProvider ()
    {
        return (FileInformationProvider<T>)Defaults.DEFAULT_MULTI_PROVIDER;
    }

    public void setDefaultInformationProvider ( FileInformationProvider<Object> provider );

    public FileInformationProvider<Object> getDefaultInformationProvider ();

    public void addFile ( String targetName, Path source, FileInformationProvider<? super Path> provider ) throws IOException;

    public default void addFile ( final String targetName, final Path source, final SimpleFileInformationCustomizer customizer ) throws IOException
    {
        addFile ( targetName, source, getDefaultInformationProvider ().customize ( customizer ) );
    }

    public void addFile ( String targetName, InputStream source, FileInformationProvider<Object> provider ) throws IOException;

    public default void addFile ( final String targetName, final InputStream source, final SimpleFileInformationCustomizer customizer ) throws IOException
    {
        addFile ( targetName, source, getDefaultInformationProvider ().customize ( customizer ) );
    }

    public void addFile ( String targetName, ByteBuffer source, FileInformationProvider<Object> provider ) throws IOException;

    public default void addFile ( final String targetName, final ByteBuffer source, final SimpleFileInformationCustomizer customizer ) throws IOException
    {
        addFile ( targetName, source, getDefaultInformationProvider ().customize ( customizer ) );
    }

    public default void addFile ( final String targetName, final byte[] source, final FileInformationProvider<Object> provider ) throws IOException
    {
        addFile ( targetName, ByteBuffer.wrap ( source ), provider );
    }

    public default void addFile ( final String targetName, final byte[] source, final SimpleFileInformationCustomizer customizer ) throws IOException
    {
        addFile ( targetName, source, getDefaultInformationProvider ().customize ( customizer ) );
    }

    public void addDirectory ( String targetName, final FileInformationProvider<Directory> provider ) throws IOException;
}
