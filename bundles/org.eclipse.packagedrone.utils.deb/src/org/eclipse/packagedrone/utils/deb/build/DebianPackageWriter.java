/*******************************************************************************
 * Copyright (c) 2014, 2016 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *     M-Ezzat - code cleanup - squid:S2131, squid:S1943
 *******************************************************************************/
package org.eclipse.packagedrone.utils.deb.build;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.archivers.ar.ArArchiveEntry;
import org.apache.commons.compress.archivers.ar.ArArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.util.encoders.Hex;
import org.eclipse.packagedrone.utils.deb.ControlFileWriter;
import org.eclipse.packagedrone.utils.deb.control.BinaryPackageControlFile;
import org.eclipse.packagedrone.utils.deb.internal.ChecksumInputStream;

public class DebianPackageWriter implements AutoCloseable, BinaryPackageBuilder
{
    public static final Charset CHARSET = Charset.forName ( "UTF-8" );

    private static final int AR_ARCHIVE_DEFAULT_MODE = 33188; // see ArArchive

    private final ArArchiveOutputStream ar;

    private final byte[] binaryHeader = "2.0\n".getBytes ( StandardCharsets.UTF_8 );

    private final Supplier<Instant> timestampSupplier;

    private final File dataTemp;

    private final TarArchiveOutputStream dataStream;

    private final BinaryPackageControlFile packageControlFile;

    private long installedSize = 0;

    private final Map<String, String> checkSums = new TreeMap<> ();

    private final Set<String> confFiles = new TreeSet<> ();

    private final Set<String> paths = new HashSet<> ();

    private ContentProvider preinstScript;

    private ContentProvider postinstScript;

    private ContentProvider prermScript;

    private ContentProvider postrmScript;

    public DebianPackageWriter ( final OutputStream stream, final BinaryPackageControlFile packageControlFile ) throws IOException
    {
        this ( stream, packageControlFile, Instant::now );
    }

    public DebianPackageWriter ( final OutputStream stream, final BinaryPackageControlFile packageControlFile, final Supplier<Instant> timestampSupplier ) throws IOException
    {
        Objects.requireNonNull ( timestampSupplier );

        this.timestampSupplier = timestampSupplier;
        this.packageControlFile = packageControlFile;
        BinaryPackageControlFile.validate ( packageControlFile );

        this.ar = new ArArchiveOutputStream ( stream );

        this.ar.putArchiveEntry ( new ArArchiveEntry ( "debian-binary", this.binaryHeader.length, 0, 0, AR_ARCHIVE_DEFAULT_MODE, timestampSupplier.get ().getEpochSecond () ) );
        this.ar.write ( this.binaryHeader );
        this.ar.closeArchiveEntry ();

        this.dataTemp = File.createTempFile ( "data", null );

        this.dataStream = new TarArchiveOutputStream ( new GZIPOutputStream ( new FileOutputStream ( this.dataTemp ) ) );
        this.dataStream.setLongFileMode ( TarArchiveOutputStream.LONGFILE_GNU );
    }

    public void addFile ( final File file, final String fileName, final EntryInformation entryInformation ) throws IOException
    {
        addFile ( new FileContentProvider ( file ), fileName, entryInformation, Optional.of ( (Supplier<Instant>) () -> {
            return file == null || !file.canRead () ? null : Instant.ofEpochMilli ( file.lastModified () );
        } ) );
    }

    public void addFile ( final File file, final String fileName, final EntryInformation entryInformation, final Optional<Supplier<Instant>> timestampSupplier ) throws IOException
    {
        addFile ( new FileContentProvider ( file ), fileName, entryInformation, timestampSupplier );
    }

    public void addFile ( final byte[] content, final String fileName, final EntryInformation entryInformation ) throws IOException
    {
        addFile ( new StaticContentProvider ( content ), fileName, entryInformation );
    }

    public void addFile ( final byte[] content, final String fileName, final EntryInformation entryInformation, final Optional<Supplier<Instant>> timestampSupplier ) throws IOException
    {
        addFile ( new StaticContentProvider ( content ), fileName, entryInformation, timestampSupplier );
    }

    public void addFile ( final String content, final String fileName, final EntryInformation entryInformation ) throws IOException
    {
        addFile ( new StaticContentProvider ( content ), fileName, entryInformation );
    }

    public void addFile ( final String content, final String fileName, final EntryInformation entryInformation, final Optional<Supplier<Instant>> timestampSupplier ) throws IOException
    {
        addFile ( new StaticContentProvider ( content ), fileName, entryInformation, timestampSupplier );
    }

    @Override
    public void addFile ( final ContentProvider contentProvider, String fileName, EntryInformation entryInformation, final Optional<Supplier<Instant>> timestampSupplier ) throws IOException
    {
        Objects.requireNonNull ( timestampSupplier );

        if ( entryInformation == null )
        {
            entryInformation = EntryInformation.DEFAULT_FILE;
        }

        try
        {
            fileName = cleanupPath ( fileName );

            if ( entryInformation.isConfigurationFile () )
            {
                this.confFiles.add ( fileName.substring ( 1 ) ); // without the leading dot
            }

            final TarArchiveEntry entry = new TarArchiveEntry ( fileName );
            entry.setSize ( contentProvider.getSize () );
            // in case the content provider supplies a modification time itself, use that one
            applyInfo ( entry, entryInformation );
            // if the modification time should be overridden, then do it here
            applyTimestamp ( entry, timestampSupplier );

            checkCreateParents ( fileName, timestampSupplier );

            this.dataStream.putArchiveEntry ( entry );

            final Map<String, byte[]> results = new HashMap<> ();
            try ( final ChecksumInputStream in = new ChecksumInputStream ( contentProvider.createInputStream (), results, MessageDigest.getInstance ( "MD5" ) ) )
            {
                this.installedSize += IOUtils.copyLarge ( in, this.dataStream );
            }

            this.dataStream.closeArchiveEntry ();

            // record the checksum
            recordChecksum ( fileName, results.get ( "MD5" ) );
        }
        catch ( final Exception e )
        {
            throw new IOException ( e );
        }
    }

    /**
     * clean up the path so that is looks like "./usr/local/file"
     */
    private String cleanupPath ( String fileName )
    {
        if ( fileName == null )
        {
            return null;
        }

        fileName = fileName.replace ( "\\", "/" ); // just in case we get windows paths
        fileName = fileName.replace ( "/+", "/" );

        if ( fileName.startsWith ( "./" ) )
        {
            return fileName;
        }
        if ( fileName.startsWith ( "/" ) )
        {
            return "." + fileName;
        }
        return "./" + fileName;
    }

    @Override
    public void addDirectory ( String directory, final EntryInformation entryInformation, final Optional<Supplier<Instant>> timestampSupplier ) throws IOException
    {
        directory = cleanupPath ( directory );
        if ( !directory.endsWith ( "/" ) )
        {
            directory += Character.toString ( '/' );
        }
        checkCreateParents ( directory, timestampSupplier );
        internalAddDirectory ( directory, entryInformation, timestampSupplier );
    }

    protected void internalAddDirectory ( final String path, final EntryInformation entryInformation, final Optional<Supplier<Instant>> timestampSupplier ) throws IOException
    {
        final TarArchiveEntry entry = new TarArchiveEntry ( path );
        applyInfo ( entry, entryInformation );
        applyTimestamp ( entry, timestampSupplier );

        this.dataStream.putArchiveEntry ( entry );
        this.dataStream.closeArchiveEntry ();

        this.paths.add ( path );
    }

    private static void applyTimestamp ( final TarArchiveEntry entry, final Optional<Supplier<Instant>> timestampSupplier )
    {
        timestampSupplier.map ( Supplier::get ).map ( Instant::toEpochMilli ).ifPresent ( entry::setModTime );
    }

    private static void applyInfo ( final TarArchiveEntry entry, final EntryInformation entryInformation )
    {
        if ( entryInformation == null )
        {
            return;
        }

        if ( entryInformation.getUser () != null )
        {
            entry.setUserName ( entryInformation.getUser () );
        }
        if ( entryInformation.getGroup () != null )
        {
            entry.setGroupName ( entryInformation.getGroup () );
        }
        entry.setMode ( entryInformation.getMode () );
    }

    private void checkCreateParents ( final String fileName, final Optional<Supplier<Instant>> timestampSupplier ) throws IOException
    {
        final String toks[] = fileName.split ( "/+" );

        String current = "";

        for ( int i = 0; i < toks.length - 1; i++ )
        {
            if ( toks[i].isEmpty () )
            {
                continue;
            }

            current += toks[i] + "/";
            if ( !this.paths.contains ( current ) )
            {
                internalAddDirectory ( current, EntryInformation.DEFAULT_DIRECTORY, timestampSupplier );
            }
        }
    }

    private void recordChecksum ( final String fileName, final byte[] bs )
    {
        this.checkSums.put ( fileName, Hex.toHexString ( bs ) );
    }

    @Override
    public void close () throws IOException
    {
        try
        {
            try
            {
                buildAndAddControlFile ( this.timestampSupplier );
                this.dataStream.close ();
                addArFile ( this.dataTemp, "data.tar.gz", this.timestampSupplier );
            }
            finally
            {
                this.ar.close ();
            }
        }
        finally
        {
            this.dataTemp.delete ();
        }
    }

    private void buildAndAddControlFile ( final Supplier<Instant> timestampSupplier ) throws IOException, FileNotFoundException
    {
        final File controlFile = File.createTempFile ( "control", null );
        try
        {
            try ( GZIPOutputStream gout = new GZIPOutputStream ( new FileOutputStream ( controlFile ) );
                  TarArchiveOutputStream tout = new TarArchiveOutputStream ( gout ) )
            {
                tout.setLongFileMode ( TarArchiveOutputStream.LONGFILE_GNU );

                addControlContent ( tout, "control", createControlContent (), -1, timestampSupplier );
                addControlContent ( tout, "md5sums", createChecksumContent (), -1, timestampSupplier );
                addControlContent ( tout, "conffiles", createConfFilesContent (), -1, timestampSupplier );
                addControlContent ( tout, "preinst", this.preinstScript, EntryInformation.DEFAULT_FILE_EXEC.getMode (), timestampSupplier );
                addControlContent ( tout, "prerm", this.prermScript, EntryInformation.DEFAULT_FILE_EXEC.getMode (), timestampSupplier );
                addControlContent ( tout, "postinst", this.postinstScript, EntryInformation.DEFAULT_FILE_EXEC.getMode (), timestampSupplier );
                addControlContent ( tout, "postrm", this.postrmScript, EntryInformation.DEFAULT_FILE_EXEC.getMode (), timestampSupplier );
            }
            addArFile ( controlFile, "control.tar.gz", timestampSupplier );
        }
        finally
        {
            controlFile.delete ();
        }
    }

    private void addControlContent ( final TarArchiveOutputStream out, final String name, final ContentProvider content, final int mode, final Supplier<Instant> timestampSupplier ) throws IOException
    {
        if ( content == null || !content.hasContent () )
        {
            return;
        }

        final TarArchiveEntry entry = new TarArchiveEntry ( name );
        if ( mode >= 0 )
        {
            entry.setMode ( mode );
        }

        entry.setUserName ( "root" );
        entry.setGroupName ( "root" );
        entry.setSize ( content.getSize () );
        entry.setModTime ( timestampSupplier.get ().toEpochMilli () );
        out.putArchiveEntry ( entry );
        try ( InputStream stream = content.createInputStream () )
        {
            IOUtils.copy ( stream, out );
        }
        out.closeArchiveEntry ();
    }

    protected ContentProvider createControlContent () throws IOException
    {
        this.packageControlFile.set ( BinaryPackageControlFile.Fields.INSTALLED_SIZE, Long.toString ( this.installedSize ) );

        final StringWriter sw = new StringWriter ();
        final ControlFileWriter writer = new ControlFileWriter ( sw, BinaryPackageControlFile.FORMATTERS );
        writer.writeEntries ( this.packageControlFile.getValues () );
        sw.close ();

        return new StaticContentProvider ( sw.toString () );
    }

    protected ContentProvider createChecksumContent () throws IOException
    {
        if ( this.checkSums.isEmpty () )
        {
            return ContentProvider.NULL_CONTENT;
        }

        final StringWriter sw = new StringWriter ();

        for ( final Map.Entry<String, String> entry : this.checkSums.entrySet () )
        {
            final String filename = entry.getKey ().substring ( 2 ); // without the leading dot and slash

            sw.append ( entry.getValue () );
            sw.append ( "  " );
            sw.append ( filename );
            sw.append ( '\n' );
        }

        sw.close ();

        return new StaticContentProvider ( sw.toString () );
    }

    protected ContentProvider createConfFilesContent () throws IOException
    {
        if ( this.confFiles.isEmpty () )
        {
            return ContentProvider.NULL_CONTENT;
        }

        final StringWriter sw = new StringWriter ();

        for ( final String confFile : this.confFiles )
        {
            sw.append ( confFile ).append ( '\n' );
        }

        sw.close ();
        return new StaticContentProvider ( sw.toString () );
    }

    private void addArFile ( final File file, final String entryName, final Supplier<Instant> timestampSupplier ) throws IOException
    {
        final ArArchiveEntry entry = new ArArchiveEntry ( entryName, file.length (), 0, 0, AR_ARCHIVE_DEFAULT_MODE, timestampSupplier.get ().getEpochSecond () );
        this.ar.putArchiveEntry ( entry );

        IOUtils.copy ( new FileInputStream ( file ), this.ar );

        this.ar.closeArchiveEntry ();
    }

    public void setPostinstScript ( final ContentProvider postinstScript )
    {
        this.postinstScript = postinstScript;
    }

    public void setPostrmScript ( final ContentProvider postrmScript )
    {
        this.postrmScript = postrmScript;
    }

    public void setPreinstScript ( final ContentProvider preinstScript )
    {
        this.preinstScript = preinstScript;
    }

    public void setPrermScript ( final ContentProvider prermScript )
    {
        this.prermScript = prermScript;
    }

}
