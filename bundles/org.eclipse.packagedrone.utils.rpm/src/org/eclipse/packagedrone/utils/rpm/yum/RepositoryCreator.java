/*******************************************************************************
 * Copyright (c) 2015, 2016 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *     M-Ezzat - code cleanup - squid:S2131
 *******************************************************************************/
package org.eclipse.packagedrone.utils.rpm.yum;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.eclipse.packagedrone.utils.io.IOConsumer;
import org.eclipse.packagedrone.utils.io.OutputSpooler;
import org.eclipse.packagedrone.utils.io.SpoolOutTarget;
import org.eclipse.packagedrone.utils.rpm.RpmVersion;
import org.eclipse.packagedrone.utils.rpm.deps.RpmDependencyFlags;
import org.eclipse.packagedrone.utils.rpm.info.RpmInformation;
import org.eclipse.packagedrone.utils.rpm.info.RpmInformation.Changelog;
import org.eclipse.packagedrone.utils.rpm.info.RpmInformation.Dependency;
import org.eclipse.packagedrone.utils.security.pgp.SigningStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class RepositoryCreator
{
    private static final String MD_NAME = "SHA-256";

    private static final String MD_TAG = "sha256";

    private final XmlContext xml;

    private final OutputSpooler primaryStreamBuilder;

    private final OutputSpooler filelistsStreamBuilder;

    private final OutputSpooler otherStreamBuilder;

    private final OutputSpooler mdStreamBuilder;

    private final List<Pattern> primaryFiles;

    private final List<Pattern> primaryDirs;

    private final String primaryUniqueName;

    private final String filelistsUniqueName;

    private final String otherUniqueName;

    public interface XmlContext
    {
        public void write ( Document primary, OutputStream primaryStream ) throws IOException;

        public Document createDocument ();
    }

    public static class DefaultXmlContext implements XmlContext
    {
        private final DocumentBuilderFactory documentBuilderFactory;

        private final TransformerFactory transformerFactory;

        public DefaultXmlContext ()
        {
            this.documentBuilderFactory = DocumentBuilderFactory.newInstance ();
            this.documentBuilderFactory.setNamespaceAware ( true );

            this.transformerFactory = TransformerFactory.newInstance ();
        }

        public DefaultXmlContext ( final DocumentBuilderFactory documentBuilderFactory, final TransformerFactory transformerFactory )
        {
            Objects.requireNonNull ( documentBuilderFactory );
            Objects.requireNonNull ( transformerFactory );

            if ( !documentBuilderFactory.isNamespaceAware () )
            {
                throw new IllegalArgumentException ( "The provided DocumentBuilderFactory must be namespace aware" );
            }

            this.documentBuilderFactory = DocumentBuilderFactory.newInstance ();
            this.documentBuilderFactory.setNamespaceAware ( true );

            this.transformerFactory = TransformerFactory.newInstance ();
        }

        @Override
        public Document createDocument ()
        {
            try
            {
                return this.documentBuilderFactory.newDocumentBuilder ().newDocument ();
            }
            catch ( final ParserConfigurationException e )
            {
                throw new RuntimeException ( e );
            }
        }

        @Override
        public void write ( final Document doc, final OutputStream outputStream ) throws IOException
        {
            try
            {
                final Transformer transformer = this.transformerFactory.newTransformer ();
                final DOMSource source = new DOMSource ( doc );
                final Result result = new StreamResult ( outputStream );
                transformer.setOutputProperty ( OutputKeys.INDENT, "yes" );
                transformer.setOutputProperty ( OutputKeys.ENCODING, "UTF-8" );
                transformer.setOutputProperty ( "{http://xml.apache.org/xslt}indent-amount", "2" );

                transformer.transform ( source, result );
            }
            catch ( final TransformerException e )
            {
                throw new IOException ( e );
            }
        }
    }

    public interface Context
    {
        public void addPackage ( FileInformation fileInformation, RpmInformation rpmInformation, Map<ChecksumType, String> checksums, ChecksumType idType );
    }

    public static class FileInformation
    {
        private final Instant timestamp;

        private final long size;

        private final String location;

        public FileInformation ( final Instant timestamp, final long size, final String location )
        {
            this.timestamp = timestamp;
            this.size = size;
            this.location = location;
        }

        public Instant getTimestamp ()
        {
            return this.timestamp;
        }

        public long getSize ()
        {
            return this.size;
        }

        public String getLocation ()
        {
            return this.location;
        }
    }

    private class ContextImpl implements Context
    {
        private final OutputStream primaryStream;

        private final OutputStream filelistsStream;

        private final OutputStream otherStream;

        private final XmlContext xml;

        private final Document primary;

        private final Document filelists;

        private final Document other;

        private final Element primaryRoot;

        private final Element filelistsRoot;

        private final Element otherRoot;

        private long count;

        public ContextImpl ( final OutputStream primaryStream, final OutputStream filelistsStream, final OutputStream otherStream, final XmlContext xml )
        {
            this.primaryStream = primaryStream;
            this.filelistsStream = filelistsStream;
            this.otherStream = otherStream;

            this.xml = xml;

            this.primary = xml.createDocument ();
            this.primaryRoot = this.primary.createElementNS ( "http://linux.duke.edu/metadata/common", "metadata" );
            this.primaryRoot.setAttribute ( "xmlns:rpm", "http://linux.duke.edu/metadata/rpm" );
            this.primary.appendChild ( this.primaryRoot );

            this.filelists = xml.createDocument ();
            this.filelistsRoot = this.filelists.createElementNS ( "http://linux.duke.edu/metadata/filelists", "filelists" );
            this.filelists.appendChild ( this.filelistsRoot );

            this.other = xml.createDocument ();
            this.otherRoot = this.other.createElementNS ( "http://linux.duke.edu/metadata/other", "otherdata" );
            this.other.appendChild ( this.otherRoot );
        }

        @Override
        public void addPackage ( final FileInformation fileInformation, final RpmInformation info, final Map<ChecksumType, String> checksums, final ChecksumType idType )
        {
            Objects.requireNonNull ( fileInformation );
            Objects.requireNonNull ( info );
            Objects.requireNonNull ( checksums );
            Objects.requireNonNull ( idType );

            final String id = checksums.get ( idType );
            if ( id == null || id.isEmpty () )
            {
                throw new IllegalArgumentException ( String.format ( "Checksums map did not contain a value for the ID type: %s", idType ) );
            }

            this.count++;

            // insert to primary

            insertToPrimary ( fileInformation, info, checksums, idType );

            // insert to "filelists"

            {
                final Element pkg = createPackage ( this.filelistsRoot, id, info );
                appendFiles ( info, pkg, null, null );
            }

            // insert to "other"

            {
                final Element pkg = createPackage ( this.otherRoot, id, info );
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

        private void insertToPrimary ( final FileInformation fileInformation, final RpmInformation info, final Map<ChecksumType, String> checksums, final ChecksumType idType )
        {
            final Element pkg = addElement ( this.primaryRoot, "package" );
            pkg.setAttribute ( "type", "rpm" );

            addElement ( pkg, "name", info.getName () );
            addElement ( pkg, "arch", info.getArchitecture () );

            addVersion ( pkg, info.getVersion () );

            for ( final Map.Entry<ChecksumType, String> entry : checksums.entrySet () )
            {
                final Element checksum = addElement ( pkg, "checksum", entry.getValue () );
                checksum.setAttribute ( "type", entry.getKey ().getId () );
                if ( entry.getKey () == idType )
                {
                    checksum.setAttribute ( "pkgid", "YES" );
                }
            }

            addElement ( pkg, "summary", info.getSummary () );
            addElement ( pkg, "description", info.getDescription () );
            addElement ( pkg, "packager", info.getPackager () );
            addElement ( pkg, "url", info.getUrl () );

            // time

            final Element time = addElement ( pkg, "time" );
            time.setAttribute ( "file", "" + fileInformation.getTimestamp ().getEpochSecond () );
            if ( info.getBuildTimestamp () != null )
            {
                time.setAttribute ( "build", "" + info.getBuildTimestamp () );
            }

            // size

            final Element size = addElement ( pkg, "size" );
            size.setAttribute ( "package", "" + fileInformation.getSize () );
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
            location.setAttribute ( "href", fileInformation.getLocation () );

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
            this.primaryRoot.setAttribute ( "packages", Long.toString ( this.count ) );
            this.filelistsRoot.setAttribute ( "packages", Long.toString ( this.count ) );
            this.otherRoot.setAttribute ( "packages", Long.toString ( this.count ) );

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

    public static class Builder
    {
        private SpoolOutTarget target;

        private XmlContext xmlContext;

        private Function<OutputStream, OutputStream> signingStreamCreator;

        public Builder ()
        {
        }

        public Builder setTarget ( final SpoolOutTarget target )
        {
            this.target = target;
            return this;
        }

        public Builder setXmlContext ( final XmlContext xmlContext )
        {
            this.xmlContext = xmlContext;
            return this;
        }

        public Builder setSigning ( final Function<OutputStream, OutputStream> signingStreamCreator )
        {
            this.signingStreamCreator = signingStreamCreator;
            return this;
        }

        public Builder setSigning ( final PGPPrivateKey privateKey )
        {
            return setSigning ( privateKey, HashAlgorithmTags.SHA1 );
        }

        public Builder setSigning ( final PGPPrivateKey privateKey, final int digestAlgorithm )
        {
            if ( privateKey != null )
            {
                this.signingStreamCreator = output -> new SigningStream ( output, privateKey, digestAlgorithm, false );
            }
            else
            {
                this.signingStreamCreator = null;
            }
            return this;
        }

        public RepositoryCreator build ()
        {
            return new RepositoryCreator ( this.target, this.xmlContext == null ? new DefaultXmlContext () : this.xmlContext, this.signingStreamCreator );
        }
    }

    private RepositoryCreator ( final SpoolOutTarget target, final XmlContext xml, final Function<OutputStream, OutputStream> signingStreamCreator )
    {
        Objects.requireNonNull ( target );
        Objects.requireNonNull ( xml );

        // xml

        this.xml = xml;

        // filters

        final String dirFilter = System.getProperty ( "drone.rpm.yum.primaryDirs", "bin/,^/etc/" );
        final String fileFilter = System.getProperty ( "drone.rpm.yum.primaryFiles", dirFilter );

        this.primaryFiles = Arrays.stream ( fileFilter.split ( "," ) ).map ( re -> Pattern.compile ( re ) ).collect ( Collectors.toList () );
        this.primaryDirs = Arrays.stream ( dirFilter.split ( "," ) ).map ( re -> Pattern.compile ( re ) ).collect ( Collectors.toList () );

        this.primaryUniqueName = UUID.randomUUID ().toString ().replace ( "-", "" );
        this.filelistsUniqueName = UUID.randomUUID ().toString ().replace ( "-", "" );
        this.otherUniqueName = UUID.randomUUID ().toString ().replace ( "-", "" );

        // primary

        this.primaryStreamBuilder = new OutputSpooler ( target );

        this.primaryStreamBuilder.addDigest ( MD_NAME );

        this.primaryStreamBuilder.addOutput ( String.format ( "repodata/%s-primary.xml", this.primaryUniqueName ), "application/xml" );
        this.primaryStreamBuilder.addOutput ( String.format ( "repodata/%s-primary.xml.gz", this.primaryUniqueName ), "application/x-gzip", output -> new GZIPOutputStream ( output ) );

        // filelists

        this.filelistsStreamBuilder = new OutputSpooler ( target );

        this.filelistsStreamBuilder.addDigest ( MD_NAME );

        this.filelistsStreamBuilder.addOutput ( String.format ( "repodata/%s-filelists.xml", this.filelistsUniqueName ), "application/xml" );
        this.filelistsStreamBuilder.addOutput ( String.format ( "repodata/%s-filelists.xml.gz", this.filelistsUniqueName ), "application/x-gzip", output -> new GZIPOutputStream ( output ) );

        // other

        this.otherStreamBuilder = new OutputSpooler ( target );

        this.otherStreamBuilder.addDigest ( MD_NAME );

        this.otherStreamBuilder.addOutput ( String.format ( "repodata/%s-other.xml", this.otherUniqueName ), "application/xml" );
        this.otherStreamBuilder.addOutput ( String.format ( "repodata/%s-other.xml.gz", this.otherUniqueName ), "application/x-gzip", output -> new GZIPOutputStream ( output ) );

        // md

        this.mdStreamBuilder = new OutputSpooler ( target );

        this.mdStreamBuilder.addOutput ( "repodata/repomd.xml", "application/xml" );
        if ( signingStreamCreator != null )
        {
            this.mdStreamBuilder.addOutput ( "repodata/repomd.xml.asc", "text/plain", signingStreamCreator::apply );
        }
    }

    private boolean matches ( final String pathName, final List<Pattern> filterList )
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
        final Document doc = this.xml.createDocument ();

        final Element root = doc.createElementNS ( "http://linux.duke.edu/metadata/repo", "repomd" );
        doc.appendChild ( root );

        root.setAttribute ( "revision", Long.toString ( now / 1000 ) );

        addDataFile ( root, this.primaryStreamBuilder, this.primaryUniqueName, "primary", now );
        addDataFile ( root, this.filelistsStreamBuilder, this.filelistsUniqueName, "filelists", now );
        addDataFile ( root, this.otherStreamBuilder, this.otherUniqueName, "other", now );

        try
        {
            this.xml.write ( doc, stream );
        }
        catch ( final Exception e )
        {
            throw new IOException ( e );
        }
    }

    private void addDataFile ( final Element root, final OutputSpooler spooler, final String unique, final String baseName, final long now )
    {
        final String filename = "repodata/" + unique + "-" + baseName + ".xml";
        final Element data = addElement ( root, "data" );

        data.setAttribute ( "type", baseName );

        final Element checksum = addElement ( data, "checksum", spooler.getChecksum ( filename + ".gz", MD_NAME ) );
        checksum.setAttribute ( "type", MD_TAG );

        final Element openChecksum = addElement ( data, "open-checksum", spooler.getChecksum ( filename, MD_NAME ) );
        openChecksum.setAttribute ( "type", MD_TAG );

        final Element location = addElement ( data, "location" );
        location.setAttribute ( "href", filename + ".gz" );
        addElement ( data, "timestamp", now / 1000 );

        addElement ( data, "size", "" + spooler.getSize ( filename + ".gz" ) );
        addElement ( data, "open-size", "" + spooler.getSize ( filename ) );
    }

    private static void addOptionalElement ( final Element parent, final String name, final Object value )
    {
        if ( value == null )
        {
            return;
        }

        addElement ( parent, name, value );
    }

    private static Element addElement ( final Element parent, final String name )
    {
        return addElement ( parent, name, null );
    }

    private static Element addElement ( final Element parent, final String name, final Object value )
    {
        final Document doc = parent.getOwnerDocument ();
        final Element result = doc.createElement ( name );
        parent.appendChild ( result );
        if ( value != null )
        {
            result.appendChild ( doc.createTextNode ( value.toString () ) );
        }
        return result;
    }
}
