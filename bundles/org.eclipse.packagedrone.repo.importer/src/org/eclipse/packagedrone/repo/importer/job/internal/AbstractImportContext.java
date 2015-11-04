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
package org.eclipse.packagedrone.repo.importer.job.internal;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.packagedrone.job.ErrorInformation;
import org.eclipse.packagedrone.job.JobInstance.Context;
import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.eclipse.packagedrone.repo.channel.ChannelService;
import org.eclipse.packagedrone.repo.channel.ModifiableChannel;
import org.eclipse.packagedrone.repo.channel.ChannelService.By;
import org.eclipse.packagedrone.repo.importer.ImportContext;
import org.eclipse.packagedrone.repo.importer.ImportSubContext;
import org.eclipse.packagedrone.repo.importer.job.ImporterResult;
import org.eclipse.packagedrone.repo.importer.job.ImporterResult.Entry;

public abstract class AbstractImportContext implements ImportContext, AutoCloseable
{
    private interface ImportEntry
    {
        public void close () throws Exception;

        public Map<MetaKey, String> getProvidedMetaData ();

        public String getName ();

        public InputStream openStream () throws Exception;

        public List<ImportEntry> getChildren ();
    }

    private static abstract class AbstractEntry implements ImportEntry
    {
        private final String name;

        private final Map<MetaKey, String> providedMetaData;

        private final List<ImportEntry> children = new LinkedList<> ();

        public AbstractEntry ( final String name, final Map<MetaKey, String> providedMetaData )
        {
            this.name = name;
            this.providedMetaData = providedMetaData;
        }

        @Override
        public List<ImportEntry> getChildren ()
        {
            return this.children;
        }

        @Override
        public String getName ()
        {
            return this.name;
        }

        @Override
        public Map<MetaKey, String> getProvidedMetaData ()
        {
            return this.providedMetaData;
        }

        @Override
        public void close () throws Exception
        {
            closeEntries ( this.children );
        }
    }

    private static class StreamEntry extends AbstractEntry
    {
        private final InputStream stream;

        public StreamEntry ( final InputStream stream, final String name, final Map<MetaKey, String> providedMetaData )
        {
            super ( name, providedMetaData );
            this.stream = stream;
        }

        @Override
        public InputStream openStream ()
        {
            return this.stream;
        }

        @Override
        public void close () throws Exception
        {
            Exception ex = null;
            try
            {
                super.close ();
            }
            catch ( final Exception e )
            {
                ex = e;
            }

            try
            {
                this.stream.close ();
            }
            catch ( final Exception e )
            {
                if ( ex != null )
                {
                    e.addSuppressed ( ex );
                }
                throw e;
            }
        }
    }

    private static class FileEntry extends AbstractEntry
    {
        private final Path file;

        private final boolean deleteAfterImport;

        private BufferedInputStream stream;

        public FileEntry ( final Path file, final boolean deleteAfterImport, final String name, final Map<MetaKey, String> providedMetaData )
        {
            super ( name, providedMetaData );

            this.file = file;
            this.deleteAfterImport = deleteAfterImport;
        }

        @Override
        public InputStream openStream () throws Exception
        {
            this.stream = new BufferedInputStream ( new FileInputStream ( this.file.toFile () ) );
            return this.stream;
        }

        @Override
        public void close () throws Exception
        {
            Exception ex = null;
            try
            {
                super.close ();
            }
            catch ( final Exception e )
            {
                ex = e;
            }

            try
            {
                if ( this.stream != null )
                {
                    try
                    {
                        this.stream.close ();
                    }
                    catch ( final Exception e )
                    {
                        if ( ex != null )
                        {
                            e.addSuppressed ( ex );
                        }
                        throw e;
                    }
                }
            }
            finally
            {
                if ( this.deleteAfterImport )
                {
                    Files.deleteIfExists ( this.file );
                }
            }
        }
    }

    private final List<ImportEntry> entries = new LinkedList<> ();

    private final List<CleanupTask> cleanup = new LinkedList<> ();

    private final Context context;

    private final ChannelService service;

    private final String channelId;

    public AbstractImportContext ( final Context context, final ChannelService service, final String channelId )
    {
        this.context = context;
        this.service = service;
        this.channelId = channelId;
    }

    @Override
    public Context getJobContext ()
    {
        return this.context;
    }

    @Override
    public ImportSubContext scheduleImport ( final InputStream stream, final String name, final Map<MetaKey, String> providedMetaData )
    {
        return scheduleStream ( this.entries, stream, name, providedMetaData );
    }

    @Override
    public ImportSubContext scheduleImport ( final Path file, final boolean deleteAfterImport, final String name, final Map<MetaKey, String> providedMetaData )
    {
        return scheduleFile ( this.entries, file, deleteAfterImport, name, providedMetaData );
    }

    protected ImportSubContext scheduleFile ( final List<ImportEntry> entries, final Path file, final boolean deleteAfterImport, final String name, final Map<MetaKey, String> providedMetaData )
    {
        final FileEntry entry = new FileEntry ( file, deleteAfterImport, name, providedMetaData );
        synchronized ( entries )
        {
            entries.add ( entry );
        }
        return createSubContext ( entry );
    }

    protected ImportSubContext scheduleStream ( final List<ImportEntry> entries, final InputStream stream, final String name, final Map<MetaKey, String> providedMetaData )
    {
        final StreamEntry entry = new StreamEntry ( stream, name, providedMetaData );
        synchronized ( entries )
        {
            entries.add ( entry );
        }
        return createSubContext ( entry );
    }

    private ImportSubContext createSubContext ( final AbstractEntry parentEntry )
    {
        return new ImportSubContext () {

            @Override
            public ImportSubContext scheduleImport ( final Path file, final boolean deleteAfterImport, final String name, final Map<MetaKey, String> providedMetaData )
            {
                return scheduleFile ( parentEntry.getChildren (), file, deleteAfterImport, name, providedMetaData );
            }

            @Override
            public ImportSubContext scheduleImport ( final InputStream stream, final String name, final Map<MetaKey, String> providedMetaData )
            {
                return scheduleStream ( parentEntry.getChildren (), stream, name, providedMetaData );
            }
        };
    }

    public ImporterResult process () throws Exception
    {
        final ImporterResult result = new ImporterResult ();

        result.setChannelId ( getChannelId () );
        result.setTotalBytes ( processChildren ( result, null, null, this.entries ) );

        return result;
    }

    private long processChildren ( final ImporterResult result, final ArtifactInformation parent, final Entry parentEntry, final List<ImportEntry> children ) throws Exception
    {
        Exception err = null;
        long bytes = 0;

        for ( final ImportEntry entry : children )
        {
            if ( err == null )
            {
                try
                {
                    final ArtifactInformation art;

                    if ( parent == null )
                    {
                        art = performRootImport ( entry.openStream (), entry.getName (), entry.getProvidedMetaData () );
                    }
                    else
                    {
                        art = this.service.accessCall ( By.id ( this.channelId ), ModifiableChannel.class, channel -> {
                            return channel.getContext ().createArtifact ( parent.getId (), entry.openStream (), entry.getName (), entry.getProvidedMetaData () );
                        } );
                    }

                    bytes += art.getSize ();

                    final Entry newEntry = new Entry ( art.getId (), art.getName (), art.getSize () );
                    result.getEntries ().add ( newEntry );

                    bytes += processChildren ( result, art, newEntry, entry.getChildren () );
                }
                catch ( final Exception e )
                {
                    err = e;
                    final Entry newEntry = new Entry ( entry.getName (), ErrorInformation.createFrom ( e ) );
                    result.getEntries ().add ( newEntry );

                    skipChildren ( newEntry, entry.getChildren () );
                }
            }
            else
            {
                final Entry newEntry = new Entry ( entry.getName () );
                result.getEntries ().add ( newEntry );

                skipChildren ( newEntry, entry.getChildren () );
            }
        }

        if ( err != null )
        {
            throw err;
        }

        return bytes;
    }

    private void skipChildren ( final Entry parentEntry, final List<ImportEntry> children )
    {
        for ( final ImportEntry entry : children )
        {
            parentEntry.getChildren ().add ( new Entry ( entry.getName () ) );
        }
    }

    protected abstract String getChannelId ();

    protected abstract ArtifactInformation performRootImport ( InputStream stream, String name, Map<MetaKey, String> providedMetaData );

    @Override
    public void close () throws Exception
    {
        final LinkedList<Exception> errors = new LinkedList<> ();

        try
        {
            closeEntries ( this.entries );
        }
        catch ( final Exception e )
        {
            errors.add ( e );
        }

        for ( final CleanupTask task : this.cleanup )
        {
            try
            {
                task.cleanup ();
            }
            catch ( final Exception e )
            {
                errors.add ( e );
            }
        }

        final Exception first = errors.pollFirst ();
        if ( first != null )
        {
            for ( final Exception e : errors )
            {
                first.addSuppressed ( e );
            }
            throw first;
        }
    }

    protected static void closeEntries ( final List<ImportEntry> entries ) throws Exception
    {
        final LinkedList<Exception> errors = new LinkedList<> ();

        // close all

        for ( final ImportEntry entry : entries )
        {
            try
            {
                entry.close ();
            }
            catch ( final Exception e )
            {
                errors.add ( e );
            }
        }

        // throw later

        if ( !errors.isEmpty () )
        {
            final Exception first = errors.pollFirst ();
            for ( final Exception ex : errors )
            {
                first.addSuppressed ( ex );
            }
            throw first;
        }
    }

    @Override
    public void addCleanupTask ( final CleanupTask cleanup )
    {
        this.cleanup.add ( cleanup );
    }
}
