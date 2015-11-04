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
package org.eclipse.packagedrone.repo.adapter.rpm.yum;

import static org.eclipse.packagedrone.repo.XmlHelper.addElement;
import static org.eclipse.packagedrone.repo.XmlHelper.addOptionalElement;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

import org.eclipse.packagedrone.repo.XmlHelper;
import org.eclipse.packagedrone.repo.adapter.rpm.RpmInformation;
import org.eclipse.packagedrone.repo.adapter.rpm.RpmInformation.Changelog;
import org.eclipse.packagedrone.repo.adapter.rpm.RpmInformation.Dependency;
import org.eclipse.packagedrone.repo.aspect.common.spool.OutputSpooler;
import org.eclipse.packagedrone.repo.aspect.common.spool.SpoolOutTarget;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.eclipse.packagedrone.repo.utils.IOConsumer;
import org.eclipse.packagedrone.utils.rpm.RpmDependencyFlags;
import org.eclipse.packagedrone.utils.rpm.RpmVersion;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class RepositoryCreator
{
    private final XmlHelper xml = new XmlHelper ();

    private final OutputSpooler primaryStreamBuilder;

    private final OutputSpooler filelistsStreamBuilder;

    private final OutputSpooler otherStreamBuilder;

    private final OutputSpooler mdStreamBuilder;

    private final List<Pattern> primaryFiles;

    private final List<Pattern> primaryDirs;

    public interface Context
    {
        public void addPackage ( final String sha1, final ArtifactInformation artifact, final RpmInformation info );
    }

    public class ContextImpl implements Context
    {
        private final OutputStream primaryStream;

        private final OutputStream filelistsStream;

        private final OutputStream otherStream;

        private final Document primary;

        private final Document filelists;

        private final Document other;

        private final XmlHelper xml;

        private final Element primaryRoot;

        private final Element filelistsRoot;

        private final Element otherRoot;

        private long count;

        public ContextImpl ( final OutputStream primaryStream, final OutputStream filelistsStream, final OutputStream otherStream, final XmlHelper xml )
        {
            this.primaryStream = primaryStream;
            this.filelistsStream = filelistsStream;
            this.otherStream = otherStream;

            this.primary = xml.createNs ();
            this.primaryRoot = this.primary.createElementNS ( "http://linux.duke.edu/metadata/common", "metadata" );
            this.primaryRoot.setAttribute ( "xmlns:rpm", "http://linux.duke.edu/metadata/rpm" );
            this.primary.appendChild ( this.primaryRoot );

            this.filelists = xml.createNs ();
            this.filelistsRoot = this.filelists.createElementNS ( "http://linux.duke.edu/metadata/filelists", "filelists" );
            this.filelists.appendChild ( this.filelistsRoot );

            this.other = xml.createNs ();
            this.otherRoot = this.other.createElementNS ( "http://linux.duke.edu/metadata/other", "otherdata" );
            this.other.appendChild ( this.otherRoot );

            this.xml = xml;
        }

        @Override
        public void addPackage ( final String sha1, final ArtifactInformation artifact, final RpmInformation info )
        {
            if ( info == null )
            {
                return;
            }

            this.count++;

            // insert to primary

            insertToPrimary ( sha1, artifact, info );

            // insert to "filelists"

            {
                final Element pkg = createPackage ( this.filelistsRoot, sha1, info );
                appendFiles ( info, pkg, null, null );
            }

            // insert to "other"

            {
                final Element pkg = createPackage ( this.otherRoot, sha1, info );
                for ( final Changelog log : info.getChangelog () )
                {
                    final Element cl = addElement ( pkg, "changelog", log.getText () );
                    cl.setAttribute ( "author", log.getAuthor () );
                    cl.setAttribute ( "date", "" + log.getTimestamp () );
                }
            }
        }

        private void appendFiles ( final RpmInformation info, final Element pkg, final Predicate<String> fileFilter, final Predicate<String> dirFilter )
        {
            for ( final String file : new TreeSet<> ( info.getFiles () ) )
            {
                if ( fileFilter == null || fileFilter.test ( file ) )
                {
                    addElement ( pkg, "file", file );
                }
            }
            for ( final String dir : new TreeSet<> ( info.getDirectories () ) )
            {
                if ( dirFilter == null || dirFilter.test ( dir ) )
                {
                    final Element ele = addElement ( pkg, "file", dir );
                    ele.setAttribute ( "type", "dir" );
                }
            }
        }

        private void insertToPrimary ( final String sha1, final ArtifactInformation artifact, final RpmInformation info )
        {
            final Element pkg = addElement ( this.primaryRoot, "package" );
            pkg.setAttribute ( "type", "rpm" );

            addElement ( pkg, "name", info.getName () );
            addElement ( pkg, "arch", info.getArchitecture () );

            addVersion ( pkg, info.getVersion () );

            final Element checksum = addElement ( pkg, "checksum", sha1 );
            checksum.setAttribute ( "type", "sha" );
            checksum.setAttribute ( "pkgid", "YES" );

            addElement ( pkg, "summary", info.getSummary () );
            addElement ( pkg, "description", info.getDescription () );
            addElement ( pkg, "packager", info.getPackager () );
            addElement ( pkg, "url", info.getUrl () );

            // time

            final Element time = addElement ( pkg, "time" );
            time.setAttribute ( "file", "" + artifact.getCreationTimestamp ().getTime () / 1000 );
            if ( info.getBuildTimestamp () != null )
            {
                time.setAttribute ( "build", "" + info.getBuildTimestamp () );
            }

            // size

            final Element size = addElement ( pkg, "size" );
            size.setAttribute ( "package", "" + artifact.getSize () );
            if ( info.getInstalledSize () != null )
            {
                size.setAttribute ( "installed", "" + info.getInstalledSize () );
            }
            if ( info.getArchiveSize () != null )
            {
                size.setAttribute ( "archive", "" + info.getArchiveSize () );
            }

            // location

            final Element location = addElement ( pkg, "location" );
            location.setAttribute ( "href", String.format ( "pool/%s/%s", artifact.getId (), artifact.getName () ) );

            // add format section

            final Element fmt = addElement ( pkg, "format" );
            addOptionalElement ( fmt, "rpm:license", info.getLicense () );
            addOptionalElement ( fmt, "rpm:vendor", info.getVendor () );
            addOptionalElement ( fmt, "rpm:group", info.getGroup () );
            addOptionalElement ( fmt, "rpm:buildhost", info.getBuildHost () );
            addOptionalElement ( fmt, "rpm:sourcerpm", info.getSourcePackage () );

            // add header range

            final Element rng = addElement ( fmt, "rpm:header-range" );
            rng.setAttribute ( "start", "" + info.getHeaderStart () );
            rng.setAttribute ( "end", "" + info.getHeaderEnd () );

            addDependencies ( fmt, "rpm:provides", info.getProvides () );
            addDependencies ( fmt, "rpm:requires", info.getRequires () );
            addDependencies ( fmt, "rpm:conflicts", info.getConflicts () );
            addDependencies ( fmt, "rpm:obsoletes", info.getObsoletes () );

            // add primary files

            appendFiles ( info, pkg, file -> matches ( file, RepositoryCreator.this.primaryFiles ), dir -> matches ( dir, RepositoryCreator.this.primaryDirs ) );
        }

        private void addDependencies ( final Element fmt, final String elementName, final List<Dependency> deps )
        {
            final Element ele = addElement ( fmt, elementName );

            for ( final Dependency dep : deps )
            {
                final EnumSet<RpmDependencyFlags> flags = RpmDependencyFlags.parse ( dep.getFlags () );
                if ( flags.contains ( RpmDependencyFlags.RPMLIB ) )
                {
                    continue;
                }

                final Element entry = addElement ( ele, "rpm:entry" );
                entry.setAttribute ( "name", dep.getName () );
                if ( dep.getVersion () != null )
                {
                    final RpmVersion version = RpmVersion.valueOf ( dep.getVersion () );
                    entry.setAttribute ( "epoch", "" + version.getEpoch ().orElse ( 0 ) );
                    entry.setAttribute ( "ver", version.getVersion () );
                    if ( version.getRelease ().isPresent () )
                    {
                        entry.setAttribute ( "rel", version.getRelease ().get () );
                    }
                }

                final boolean eq = flags.contains ( RpmDependencyFlags.EQUAL );

                if ( flags.contains ( RpmDependencyFlags.GREATER ) )
                {
                    entry.setAttribute ( "flags", eq ? "GE" : "GT" );
                }
                else if ( flags.contains ( RpmDependencyFlags.LESS ) )
                {
                    entry.setAttribute ( "flags", eq ? "LE" : "LT" );
                }
                else if ( eq )
                {
                    entry.setAttribute ( "flags", "EQ" );
                }

                final boolean pre = flags.contains ( RpmDependencyFlags.PREREQ ) || flags.contains ( RpmDependencyFlags.SCRIPT_PRE ) || flags.contains ( RpmDependencyFlags.SCRIPT_POST );
                if ( pre )
                {
                    entry.setAttribute ( "pre", "1" );
                }
            }
        }

        private Element createPackage ( final Element root, final String id, final RpmInformation info )
        {
            final Element pkg = addElement ( root, "package" );
            pkg.setAttribute ( "pkgid", id );
            pkg.setAttribute ( "name", info.getName () );
            pkg.setAttribute ( "arch", info.getArchitecture () );

            addVersion ( pkg, info.getVersion () );

            return pkg;
        }

        private Element addVersion ( final Element pkg, final RpmInformation.Version version )
        {
            if ( version == null )
            {
                return null;
            }

            final Element ver = addElement ( pkg, "version" );

            if ( version.getEpoch () == null || version.getEpoch ().isEmpty () )
            {
                ver.setAttribute ( "epoch", "0" );
            }
            else
            {
                ver.setAttribute ( "epoch", version.getEpoch () );
            }
            ver.setAttribute ( "ver", version.getVersion () );
            ver.setAttribute ( "rel", version.getRelease () );

            return ver;
        }

        public void close () throws IOException
        {
            this.primaryRoot.setAttribute ( "packages", "" + this.count );
            this.filelistsRoot.setAttribute ( "packages", "" + this.count );
            this.otherRoot.setAttribute ( "packages", "" + this.count );

            try
            {
                this.xml.write ( this.primary, this.primaryStream );
                this.xml.write ( this.filelists, this.filelistsStream );
                this.xml.write ( this.other, this.otherStream );
            }
            catch ( final IOException e )
            {
                throw e;
            }
            catch ( final Exception e )
            {
                throw new IOException ( e );
            }
        }
    }

    public RepositoryCreator ( final SpoolOutTarget target )
    {
        // filters

        final String dirFilter = System.getProperty ( "drone.rpm.yum.primaryDirs", "bin/,^/etc/" );
        final String fileFilter = System.getProperty ( "drone.rpm.yum.primaryFiles", dirFilter );

        this.primaryFiles = Arrays.stream ( fileFilter.split ( "," ) ).map ( re -> Pattern.compile ( re ) ).collect ( Collectors.toList () );
        this.primaryDirs = Arrays.stream ( dirFilter.split ( "," ) ).map ( re -> Pattern.compile ( re ) ).collect ( Collectors.toList () );

        // primary

        this.primaryStreamBuilder = new OutputSpooler ( target );

        this.primaryStreamBuilder.addDigest ( "SHA1" );

        this.primaryStreamBuilder.addOutput ( "repodata/primary.xml", "application/xml" );
        this.primaryStreamBuilder.addOutput ( "repodata/primary.xml.gz", "application/x-gzip", output -> new GZIPOutputStream ( output ) );

        // filelists

        this.filelistsStreamBuilder = new OutputSpooler ( target );

        this.filelistsStreamBuilder.addDigest ( "SHA1" );

        this.filelistsStreamBuilder.addOutput ( "repodata/filelists.xml", "application/xml" );
        this.filelistsStreamBuilder.addOutput ( "repodata/filelists.xml.gz", "application/x-gzip", output -> new GZIPOutputStream ( output ) );

        // other

        this.otherStreamBuilder = new OutputSpooler ( target );

        this.otherStreamBuilder.addDigest ( "SHA1" );

        this.otherStreamBuilder.addOutput ( "repodata/other.xml", "application/xml" );
        this.otherStreamBuilder.addOutput ( "repodata/other.xml.gz", "application/x-gzip", output -> new GZIPOutputStream ( output ) );

        // md

        this.mdStreamBuilder = new OutputSpooler ( target );

        this.mdStreamBuilder.addOutput ( "repodata/repomd.xml", "application/xml" );
    }

    public boolean matches ( final String pathName, final List<Pattern> filterList )
    {
        for ( final Pattern p : filterList )
        {
            if ( p.matcher ( pathName ).find () )
            {
                return true;
            }
        }
        return false;
    }

    public void process ( final IOConsumer<Context> consumer ) throws IOException
    {
        final long now = System.currentTimeMillis ();

        this.primaryStreamBuilder.open ( primaryStream -> {
            this.filelistsStreamBuilder.open ( filelistsStream -> {
                this.otherStreamBuilder.open ( otherStream -> {
                    final ContextImpl ctx = makeContext ( primaryStream, filelistsStream, otherStream );
                    consumer.accept ( ctx );
                    ctx.close ();
                } );
            } );
        } );

        this.mdStreamBuilder.open ( stream -> {
            writeRepoMd ( stream, now );
        } );

    }

    private ContextImpl makeContext ( final OutputStream primaryStream, final OutputStream filelistsStream, final OutputStream otherStream )
    {
        return new ContextImpl ( primaryStream, filelistsStream, otherStream, this.xml );
    }

    private void writeRepoMd ( final OutputStream stream, final long now ) throws IOException
    {
        final Document doc = this.xml.createNs ();

        final Element root = doc.createElementNS ( "http://linux.duke.edu/metadata/repo", "repomd" );
        doc.appendChild ( root );

        root.setAttribute ( "revision", "" + now / 1000 );

        addDataFile ( root, this.primaryStreamBuilder, "primary", now );
        addDataFile ( root, this.filelistsStreamBuilder, "filelists", now );
        addDataFile ( root, this.otherStreamBuilder, "other", now );

        try
        {
            this.xml.write ( doc, stream );
        }
        catch ( final Exception e )
        {
            throw new IOException ( e );
        }
    }

    private void addDataFile ( final Element root, final OutputSpooler spooler, final String baseName, final long now )
    {
        final Element data = addElement ( root, "data" );

        data.setAttribute ( "type", baseName );

        final Element checksum = addElement ( data, "checksum", spooler.getChecksum ( "repodata/" + baseName + ".xml.gz", "SHA1" ) );
        checksum.setAttribute ( "type", "sha" );

        final Element openChecksum = addElement ( data, "open-checksum", spooler.getChecksum ( "repodata/" + baseName + ".xml", "SHA1" ) );
        openChecksum.setAttribute ( "type", "sha" );

        final Element location = addElement ( data, "location" );
        location.setAttribute ( "href", "repodata/" + baseName + ".xml.gz" );
        addElement ( data, "timestamp", now / 1000 );

        addElement ( data, "size", "" + spooler.getSize ( "repodata/" + baseName + ".xml.gz" ) );
        addElement ( data, "open-size", "" + spooler.getSize ( "repodata/" + baseName + ".xml" ) );
    }
}
