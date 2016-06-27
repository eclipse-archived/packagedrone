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

import static java.util.Comparator.comparing;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.ToLongFunction;

import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry;
import org.apache.commons.compress.archivers.cpio.CpioConstants;
import org.eclipse.packagedrone.utils.rpm.FileFlags;
import org.eclipse.packagedrone.utils.rpm.PathName;
import org.eclipse.packagedrone.utils.rpm.RpmLead;
import org.eclipse.packagedrone.utils.rpm.RpmTag;
import org.eclipse.packagedrone.utils.rpm.RpmVersion;
import org.eclipse.packagedrone.utils.rpm.Rpms;
import org.eclipse.packagedrone.utils.rpm.build.PayloadRecorder.Result;
import org.eclipse.packagedrone.utils.rpm.deps.Dependencies;
import org.eclipse.packagedrone.utils.rpm.deps.Dependency;
import org.eclipse.packagedrone.utils.rpm.deps.RpmDependencyFlags;
import org.eclipse.packagedrone.utils.rpm.header.Header;
import org.eclipse.packagedrone.utils.rpm.signature.SignatureProcessor;
import org.eclipse.packagedrone.utils.rpm.signature.SignatureProcessors;

/**
 * Build RPM files
 * <p>
 * This class takes care of most tasks building RPM files. The constructor only
 * requests the require attributes. There are a few more meta information
 * entries which can be set using the {@link PackageInformation} class and the
 * methods {@link #setInformation(PackageInformation)} and
 * {@link #getInformation()}.
 * </p>
 * <p>
 * In order to build an RPM file, create a new instance of the
 * {@link RpmBuilder} class, set package information, add files by using a
 * context created by {@link #newContext()} and finally call {@link #build()}.
 * The RPM file will only be built once the {@link #build()} method is called.
 * Closing the instance of {@link RpmBuilder} will <em>not</em> write the RPM
 * file, but simply clean up temporary files. Closing this instance will also
 * not delete target RPM file.
 * </p>
 * <p>
 * The implementation of this class uses the {@link PayloadRecorder} to create
 * the payload archive, {@link Header} class for the signature and package
 * header and the {@link RpmWriter} to finally write the RPM file.
 * </p>
 * <h2>Signature processors</h2>
 * <p>
 * The RPM builder uses a default set of {@link SignatureProcessor}s. In order
 * to add additional ones use the {@link #addDefaultSignatureProcessors()}. It
 * is possible to remove all already registered processors (including the
 * default ones) using {@link #removeAllSignatureProcessors()}.
 * </p>
 *
 * @author Jens Reimann
 */
public class RpmBuilder implements AutoCloseable
{
    @FunctionalInterface
    private interface RecorderFunction<T>
    {
        public Result record ( PayloadRecorder recorder, String targetName, T data, Consumer<CpioArchiveEntry> customizer ) throws IOException;
    }

    public static class FileEntry
    {
        private long size;

        private String user;

        private String group;

        private String linkTo;

        private short mode;

        private short rdevs;

        private int flags;

        private int modificationTime;

        private String digest;

        private int verifyFlags = -1;

        private String lang;

        private int device;

        private int inode;

        private PathName targetName;

        private long targetSize;

        public void setSize ( final long size )
        {
            this.size = size;
        }

        public long getSize ()
        {
            return this.size;
        }

        public void setUser ( final String user )
        {
            this.user = user;
        }

        public String getUser ()
        {
            return this.user;
        }

        public void setGroup ( final String group )
        {
            this.group = group;
        }

        public String getGroup ()
        {
            return this.group;
        }

        public void setLinkTo ( final String linkTo )
        {
            this.linkTo = linkTo;
        }

        public String getLinkTo ()
        {
            return this.linkTo;
        }

        public short getMode ()
        {
            return this.mode;
        }

        public void setMode ( final short mode )
        {
            this.mode = mode;
        }

        public short getRdevs ()
        {
            return this.rdevs;
        }

        public void setRdevs ( final short rdevs )
        {
            this.rdevs = rdevs;
        }

        public int getFlags ()
        {
            return this.flags;
        }

        public void setFlags ( final int flags )
        {
            this.flags = flags;
        }

        public void setModificationTime ( final int modificationTime )
        {
            this.modificationTime = modificationTime;
        }

        public int getModificationTime ()
        {
            return this.modificationTime;
        }

        public void setDigest ( final String digest )
        {
            this.digest = digest;
        }

        public String getDigest ()
        {
            return this.digest;
        }

        public void setVerifyFlags ( final int verifyFlags )
        {
            this.verifyFlags = verifyFlags;
        }

        public int getVerifyFlags ()
        {
            return this.verifyFlags;
        }

        public void setLang ( final String lang )
        {
            this.lang = lang;
        }

        public String getLang ()
        {
            return this.lang;
        }

        public void setDevice ( final int device )
        {
            this.device = device;
        }

        public int getDevice ()
        {
            return this.device;
        }

        public void setInode ( final int inode )
        {
            this.inode = inode;
        }

        public int getInode ()
        {
            return this.inode;
        }

        public void setTargetName ( final PathName targetName )
        {
            this.targetName = targetName;
        }

        public PathName getTargetName ()
        {
            return this.targetName;
        }

        public void setTargetSize ( final long targetSize )
        {
            this.targetSize = targetSize;
        }

        public long getTargetSize ()
        {
            return this.targetSize;
        }
    }

    public static class PackageInformation
    {
        private String distribution;

        private String packager;

        private String vendor;

        private String license = "unspecified";

        private String buildHost = "localhost";

        private String summary = "Unspecified";

        private String description = "Unspecified";

        private String group = "Unspecified";

        private String operatingSystem = "linux";

        private String url;

        public void setDistribution ( final String distribution )
        {
            this.distribution = distribution;
        }

        public String getDistribution ()
        {
            return this.distribution;
        }

        public String getPackager ()
        {
            return this.packager;
        }

        public void setPackager ( final String packager )
        {
            this.packager = packager;
        }

        public String getVendor ()
        {
            return this.vendor;
        }

        public void setVendor ( final String vendor )
        {
            this.vendor = vendor;
        }

        public void setLicense ( final String license )
        {
            this.license = license;
        }

        public String getLicense ()
        {
            return this.license;
        }

        public void setBuildHost ( final String buildHost )
        {
            this.buildHost = buildHost;
        }

        public String getBuildHost ()
        {
            return this.buildHost;
        }

        public void setDescription ( final String description )
        {
            this.description = description;
        }

        public String getDescription ()
        {
            return this.description;
        }

        public void setSummary ( final String summary )
        {
            this.summary = summary;
        }

        public String getSummary ()
        {
            return this.summary;
        }

        public void setGroup ( final String group )
        {
            this.group = group;
        }

        public String getGroup ()
        {
            return this.group;
        }

        public void setOperatingSystem ( final String operatingSystem )
        {
            this.operatingSystem = operatingSystem;
        }

        public String getOperatingSystem ()
        {
            return this.operatingSystem;
        }

        public void setUrl ( final String url )
        {
            this.url = url;
        }

        public String getUrl ()
        {
            return this.url;
        }
    }

    private static abstract class BuilderContextImpl implements BuilderContext
    {
        private FileInformationProvider<Object> defaultProvider = BuilderContext.defaultProvider ();

        @Override
        public void setDefaultInformationProvider ( final FileInformationProvider<Object> provider )
        {
            this.defaultProvider = provider;
        }

        @Override
        public FileInformationProvider<Object> getDefaultInformationProvider ()
        {
            return this.defaultProvider;
        }

        protected <T> FileInformation makeInformation ( final T source, final PayloadEntryType type, final FileInformationProvider<T> provider ) throws IOException
        {
            if ( provider != null )
            {
                return provider.provide ( source, type );
            }
            if ( this.defaultProvider != null )
            {
                return this.defaultProvider.provide ( source, type );
            }

            throw new IllegalStateException ( "There was neither a default provider nor a specfic provider set" );
        }

        private static String getNotEmptyOrDefault ( final String value, final String defaultValue )
        {
            if ( value == null || value.isEmpty () )
            {
                return defaultValue;
            }
            return value;
        }

        protected void customizeCommon ( final FileEntry entry, final FileInformation information )
        {
            entry.setUser ( getNotEmptyOrDefault ( information.getUser (), BuilderContext.DEFAULT_USER ) );
            entry.setGroup ( getNotEmptyOrDefault ( information.getGroup (), BuilderContext.DEFAULT_GROUP ) );
            // modes are set in specific add methods
        }

        protected void customizeDirectory ( final FileEntry entry, final FileInformation information )
        {
            customizeCommon ( entry, information );
        }

        protected void customizeFile ( final FileEntry entry, final FileInformation information )
        {
            customizeCommon ( entry, information );
            if ( information.isConfiguration () )
            {
                entry.setFlags ( entry.getFlags () | FileFlags.CONFIGURATION.getValue () );
            }
        }

        protected void customizeSymbolicLink ( final FileEntry entry, final FileInformation information )
        {
            customizeCommon ( entry, information );
        }
    }

    private static final String DEFAULT_INTERPRETER = "/bin/sh";

    protected final Header<RpmTag> header = new Header<> ();

    private final String name;

    private final RpmVersion version;

    private final String architecture;

    protected final PayloadRecorder recorder;

    private final Path targetFile;

    private final Set<Dependency> provides = new HashSet<> ();

    private final Set<Dependency> requirements = new HashSet<> ();

    private final Set<Dependency> conflicts = new HashSet<> ();

    private final Set<Dependency> obsoletes = new HashSet<> ();

    private final Map<String, FileEntry> files = new HashMap<> ();

    private PackageInformation information = new PackageInformation ();

    private boolean hasBuilt;

    private int currentInode = 1;

    private final List<SignatureProcessor> signatureProcessors = new LinkedList<> ();

    private final BuilderOptions options;

    public RpmBuilder ( final String name, final String version, final String release, final Path target ) throws IOException
    {
        this ( name, version, release, "noarch", target );
    }

    public RpmBuilder ( final String name, final RpmVersion version, final String architecture, final Path targetFile, final OpenOption... openOptions ) throws IOException
    {
        this ( name, version, architecture, targetFile, makeBuilderOptions ( openOptions ) );
    }

    private static BuilderOptions makeBuilderOptions ( final OpenOption[] openOptions )
    {
        final BuilderOptions options = new BuilderOptions ();
        options.setOpenOptions ( openOptions );
        return options;
    }

    public RpmBuilder ( final String name, final RpmVersion version, final String architecture, final Path targetFile, final BuilderOptions options ) throws IOException
    {
        this.name = name;
        this.version = version;
        this.architecture = architecture;

        this.options = options == null ? new BuilderOptions () : new BuilderOptions ( options );

        this.targetFile = makeTargetFile ( targetFile );

        this.recorder = new PayloadRecorder ( true );

        addDefaultSignatureProcessors ();
    }

    public RpmBuilder ( final String name, final String version, final String release, final String architecture, final Path target, final OpenOption... openOptions ) throws IOException
    {
        this ( name, new RpmVersion ( null, version, release ), architecture, target, openOptions );
    }

    public void addSignatureProcessor ( final SignatureProcessor processor )
    {
        this.signatureProcessors.add ( processor );
    }

    public void removeAllSignatureProcessors ()
    {
        this.signatureProcessors.clear ();
    }

    public void addDefaultSignatureProcessors ()
    {
        addSignatureProcessor ( SignatureProcessors.size () );
        addSignatureProcessor ( SignatureProcessors.md5 () );
        addSignatureProcessor ( SignatureProcessors.sha1Header () );
        addSignatureProcessor ( SignatureProcessors.payloadSize () );
    }

    private void fillRequirements ()
    {
        this.requirements.add ( new Dependency ( "rpmlib(PayloadFilesHavePrefix)", "4.0-1", RpmDependencyFlags.LESS, RpmDependencyFlags.EQUAL, RpmDependencyFlags.RPMLIB ) );
        this.requirements.add ( new Dependency ( "rpmlib(CompressedFileNames)", "3.0.4-1", RpmDependencyFlags.LESS, RpmDependencyFlags.EQUAL, RpmDependencyFlags.RPMLIB ) );
    }

    private void fillProvides ()
    {
        this.provides.add ( new Dependency ( this.name, this.version.toString (), RpmDependencyFlags.EQUAL ) );
    }

    private void fillHeader ()
    {
        this.header.putString ( RpmTag.PAYLOAD_FORMAT, "cpio" );
        this.header.putString ( RpmTag.PAYLOAD_CODING, "gzip" );
        this.header.putString ( RpmTag.PAYLOAD_FLAGS, "9" );
        this.header.putStringArray ( 100, "C" );

        this.header.putString ( RpmTag.NAME, this.name );
        this.header.putString ( RpmTag.VERSION, this.version.getVersion () );
        if ( this.version.getRelease ().isPresent () )
        {
            this.header.putString ( RpmTag.RELEASE, this.version.getRelease ().get () );
        }
        if ( this.version.getEpoch ().isPresent () )
        {
            this.header.putInt ( RpmTag.EPOCH, this.version.getEpoch ().get () );
        }

        this.header.putString ( RpmTag.LICENSE, this.information.getLicense () );
        this.header.putStringOptional ( RpmTag.DISTRIBUTION, this.information.getDistribution () );
        this.header.putStringOptional ( RpmTag.PACKAGER, this.information.getPackager () );
        this.header.putStringOptional ( RpmTag.VENDOR, this.information.getVendor () );
        this.header.putStringOptional ( RpmTag.URL, this.information.getUrl () );

        this.header.putInt ( RpmTag.BUILDTIME, (int) ( System.currentTimeMillis () / 1000 ) );
        this.header.putString ( RpmTag.BUILDHOST, this.information.getBuildHost () );

        this.header.putI18nString ( RpmTag.SUMMARY, this.information.getSummary () );
        this.header.putI18nString ( RpmTag.DESCRIPTION, this.information.getDescription () );

        this.header.putI18nString ( RpmTag.GROUP, this.information.getGroup () );
        this.header.putString ( RpmTag.ARCH, this.architecture );
        this.header.putString ( RpmTag.OS, this.information.getOperatingSystem () );

        Dependencies.putProvides ( this.header, this.provides );
        Dependencies.putRequirements ( this.header, this.requirements );
        Dependencies.putConflicts ( this.header, this.conflicts );
        Dependencies.putObsoletes ( this.header, this.obsoletes );

        if ( !this.files.isEmpty () )
        {
            final FileEntry[] files = this.files.values ().toArray ( new FileEntry[this.files.size ()] );
            Arrays.sort ( files, comparing ( FileEntry::getTargetName ) );

            final long installedSize = Arrays.stream ( files ).mapToLong ( FileEntry::getTargetSize ).sum ();
            this.header.putSize ( installedSize, RpmTag.SIZE, RpmTag.LONGSIZE );

            final Collection<FileEntry> filesList = Arrays.asList ( files );

            // TODO: implement LONG file sizes
            Header.putIntFields ( this.header, filesList, RpmTag.FILE_SIZES, entry -> (int)entry.getSize () );
            Header.putShortFields ( this.header, filesList, RpmTag.FILE_MODES, FileEntry::getMode );
            Header.putShortFields ( this.header, filesList, RpmTag.FILE_RDEVS, FileEntry::getRdevs );
            Header.putIntFields ( this.header, filesList, RpmTag.FILE_MTIMES, FileEntry::getModificationTime );
            Header.putFields ( this.header, filesList, RpmTag.FILE_DIGESTS, String[]::new, FileEntry::getDigest, Header::putStringArray );
            Header.putFields ( this.header, filesList, RpmTag.FILE_LINKTO, String[]::new, FileEntry::getLinkTo, Header::putStringArray );
            Header.putIntFields ( this.header, filesList, RpmTag.FILE_FLAGS, FileEntry::getFlags );
            Header.putFields ( this.header, filesList, RpmTag.FILE_USERNAME, String[]::new, FileEntry::getUser, Header::putStringArray );
            Header.putFields ( this.header, filesList, RpmTag.FILE_GROUPNAME, String[]::new, FileEntry::getGroup, Header::putStringArray );

            Header.putIntFields ( this.header, filesList, RpmTag.FILE_VERIFYFLAGS, FileEntry::getVerifyFlags );

            putNumber ( this.options.getLongMode (), this.header, filesList, RpmTag.FILE_DEVICES, FileEntry::getDevice );
            putNumber ( this.options.getLongMode (), this.header, filesList, RpmTag.FILE_INODES, FileEntry::getInode );

            Header.putFields ( this.header, filesList, RpmTag.FILE_LANGS, String[]::new, FileEntry::getLang, Header::putStringArray );

            Header.putFields ( this.header, filesList, RpmTag.BASENAMES, String[]::new, fe -> fe.getTargetName ().getBasename (), Header::putStringArray );

            {
                // compress file names

                String currentDirName = null;
                final List<String> dirnames = new ArrayList<> ();
                final int[] dirIndexes = new int[files.length];
                int pos = -1;
                int i = 0;
                for ( final FileEntry f : files )
                {
                    final String dirname = f.getTargetName ().getDirname ();
                    if ( currentDirName == null || !currentDirName.equals ( dirname ) )
                    {
                        currentDirName = dirname;
                        if ( !dirname.isEmpty () )
                        {
                            dirnames.add ( "/" + dirname + "/" );
                        }
                        else
                        {
                            dirnames.add ( "/" );
                        }
                        pos++;
                    }
                    dirIndexes[i] = pos;
                    i++;
                }
                this.header.putInt ( RpmTag.DIR_INDEXES, dirIndexes );
                this.header.putStringArray ( RpmTag.DIRNAMES, dirnames.toArray ( new String[dirnames.size ()] ) );
            }
        }
    }

    private static void putNumber ( final LongMode longMode, final Header<RpmTag> header, final Collection<FileEntry> files, final RpmTag tag, final ToLongFunction<FileEntry> func )
    {
        boolean useLong;
        if ( longMode == LongMode.FORCE_64BIT )
        {
            // no need to check, got with 64bit
            useLong = true;
        }
        else
        {
            // check if we need 64bit
            final boolean needLong = needLong ( files, func );
            if ( longMode == LongMode.FORCE_32BIT )
            {
                if ( needLong )
                {
                    // we need it but are forced to 32bit
                    throw new IllegalStateException ( "Requested 32bit mode, but 64bit is necessary" );
                }
                else
                {
                    // we don't need it, so we can accept 32bit
                    useLong = false;
                }
            }
            else // everything else is DEFAULT
            {
                // we can choose, so choose
                useLong = needLong;
            }
        }

        if ( useLong )
        {
            // write as 64bit
            Header.putLongFields ( header, files, tag, func );
        }
        else
        {
            // write as 32bit
            Header.putIntFields ( header, files, tag, file -> (int)func.applyAsLong ( file ) );
        }
    }

    private static boolean needLong ( final Collection<FileEntry> files, final ToLongFunction<FileEntry> func )
    {
        return files.stream ().anyMatch ( file -> func.applyAsLong ( file ) > Integer.MAX_VALUE );
    }

    private Path makeTargetFile ( final Path target )
    {
        if ( Files.isDirectory ( target ) )
        {
            return target.resolve ( makeDefaultFileName () );
        }
        else
        {
            return target;
        }
    }

    private String makeDefaultFileName ()
    {
        final StringBuilder sb = new StringBuilder ( RpmLead.toLeadName ( this.name, this.version ) );
        sb.append ( '-' ).append ( this.architecture ).append ( ".rpm" );
        return sb.toString ();
    }

    /**
     * Get the current package information
     *
     * @return the current package information. <em>Never</em> returns
     *         {@code null}.
     */
    public PackageInformation getInformation ()
    {
        return this.information;
    }

    /**
     * Completely set the current package information
     *
     * @param information
     *            the new package information, may be {@code null}, in which
     *            case the package information is reset to its defaults.
     */
    public void setInformation ( final PackageInformation information )
    {
        this.information = information != null ? information : new PackageInformation ();
    }

    public Path getTargetFile ()
    {
        return this.targetFile;
    }

    public String getArchitecture ()
    {
        return this.architecture;
    }

    public RpmVersion getVersion ()
    {
        return this.version;
    }

    public String getName ()
    {
        return this.name;
    }

    /**
     * Actually build the RPM file
     * <p>
     * <strong>Note: </strong> this method may only be called once per instance
     * </p>
     *
     * @throws IOException
     *             in case of any IO error
     */
    public void build () throws IOException
    {
        if ( this.hasBuilt )
        {
            throw new IllegalStateException ( "RPM file has already been built. Can only be built once." );
        }

        this.hasBuilt = true;

        fillProvides ();
        fillRequirements ();

        fillHeader ();

        final LeadBuilder leadBuilder = new LeadBuilder ( this.name, this.version );
        leadBuilder.fillFlagsFromHeader ( this.header );

        try ( RpmWriter writer = new RpmWriter ( this.targetFile, leadBuilder, this.header, this.options.getOpenOptions () ) )
        {
            writer.addAllSignatureProcessors ( this.signatureProcessors );
            writer.setPayload ( this.recorder );
        }
    }

    @Override
    public void close () throws IOException
    {
        this.recorder.close ();
    }

    public void addRequirement ( final String name, final String version, final RpmDependencyFlags... flags )
    {
        this.requirements.add ( new Dependency ( name, version, flags ) );
    }

    public void addProvides ( final String name, final String version, final RpmDependencyFlags... flags )
    {
        this.provides.add ( new Dependency ( name, version, flags ) );
    }

    public void addConflicts ( final String name, final String version, final RpmDependencyFlags... flags )
    {
        this.conflicts.add ( new Dependency ( name, version, flags ) );
    }

    public void addObsoletes ( final String name, final String version, final RpmDependencyFlags... flags )
    {
        this.obsoletes.add ( new Dependency ( name, version, flags ) );
    }

    private void addFile ( final String targetName, final Path sourcePath, final int mode, final Instant mtime, final Consumer<FileEntry> customizer ) throws IOException
    {
        addFile ( targetName, sourcePath, customizer, mode, mtime, PayloadRecorder::addFile );
    }

    private <T> void addFile ( final String targetName, final T sourcePath, final Consumer<FileEntry> customizer, final int mode, final Instant fileModificationInstant, final RecorderFunction<T> func ) throws IOException
    {
        final PathName pathName = PathName.parse ( targetName );

        final long mtime = fileModificationInstant.getEpochSecond ();
        final int inode = this.currentInode++;

        final short smode = (short) ( mode | CpioConstants.C_ISREG );

        final Result result = func.record ( this.recorder, "./" + pathName.toString (), sourcePath, cpioCustomizer ( mtime, inode, smode ) );

        Consumer<FileEntry> c = this::initEntry;
        c = c.andThen ( entry -> {
            entry.setModificationTime ( (int)mtime );
            entry.setInode ( inode );
            entry.setMode ( smode );
        } );

        if ( customizer != null )
        {
            c = c.andThen ( customizer );
        }

        addResult ( pathName, result, c );
    }

    private void addDirectory ( final String targetName, final int mode, final Instant modInstant, final Consumer<FileEntry> customizer ) throws IOException
    {
        final PathName pathName = PathName.parse ( targetName );

        final long mtime = modInstant.getEpochSecond ();
        final int inode = this.currentInode++;

        final short smode = (short) ( mode | CpioConstants.C_ISDIR );

        final Result result = this.recorder.addDirectory ( "./" + pathName.toString (), cpioCustomizer ( mtime, inode, smode ) );

        Consumer<FileEntry> c = this::initEntry;
        c = c.andThen ( entry -> {
            entry.setModificationTime ( (int)mtime );
            entry.setInode ( inode );
            entry.setMode ( smode );
            entry.setSize ( 0 );
            entry.setTargetSize ( 4096 );
        } );

        if ( customizer != null )
        {
            c = c.andThen ( customizer );
        }

        addResult ( pathName, result, c );
    }

    private void addSymbolicLink ( final String targetName, final String linkTo, final int mode, final Instant modInstant, final Consumer<FileEntry> customizer ) throws IOException
    {
        final PathName pathName = PathName.parse ( targetName );

        final long mtime = modInstant.getEpochSecond ();
        final int inode = this.currentInode++;

        final short smode = (short) ( mode | CpioConstants.C_ISLNK );

        final Result result = this.recorder.addSymbolicLink ( "./" + pathName.toString (), linkTo, cpioCustomizer ( mtime, inode, smode ) );

        Consumer<FileEntry> c = this::initEntry;
        c = c.andThen ( entry -> {
            entry.setModificationTime ( (int)mtime );
            entry.setInode ( inode );
            entry.setMode ( smode );
            entry.setSize ( result.getSize () );
            entry.setTargetSize ( result.getSize () );
            entry.setLinkTo ( linkTo );
        } );

        if ( customizer != null )
        {
            c = c.andThen ( customizer );
        }

        addResult ( pathName, result, c );
    }

    private void addFile ( final String targetName, final InputStream stream, final int mode, final Instant modInstant, final Consumer<FileEntry> customizer ) throws IOException
    {
        addFile ( targetName, stream, customizer, mode, modInstant, PayloadRecorder::addFile );
    }

    private void addFile ( final String targetName, final ByteBuffer data, final int mode, final Instant modInstant, final Consumer<FileEntry> customizer ) throws IOException
    {
        addFile ( targetName, data, customizer, mode, modInstant, PayloadRecorder::addFile );
    }

    private Consumer<CpioArchiveEntry> cpioCustomizer ( final long mtime, final int inode, final short mode )
    {
        return entry -> {
            entry.setTime ( mtime );
            entry.setInode ( inode );
            entry.setMode ( mode & 0xFFFF );
            entry.setDeviceMaj ( 8 );
            entry.setDeviceMin ( 17 );
        };
    }

    private void initEntry ( final FileEntry entry )
    {
        entry.setDevice ( 1 );
        entry.setModificationTime ( (int) ( System.currentTimeMillis () / 1000 ) );
    }

    private void addResult ( final PathName targetName, final Result result, final Consumer<FileEntry> customizer )
    {
        final FileEntry entry = new FileEntry ();

        // set basic file attributes

        entry.setSize ( result.getSize () );
        entry.setTargetSize ( result.getSize () );
        entry.setDigest ( result.getSha1 () != null ? Rpms.toHex ( result.getSha1 () ).toLowerCase () : "" );
        entry.setTargetName ( targetName );

        // run customizer

        if ( customizer != null )
        {
            customizer.accept ( entry );
        }

        // record file entry

        this.files.put ( targetName.toString (), entry );
    }

    public BuilderContext newContext ()
    {
        return new BuilderContextImpl () {

            @Override
            public void addFile ( final String targetName, final Path source, final FileInformationProvider<? super Path> provider ) throws IOException
            {
                if ( !Files.isRegularFile ( source ) )
                {
                    throw new IllegalArgumentException ( String.format ( "'%s' is not a regular file", source ) );
                }
                final FileInformation info = makeInformation ( source, PayloadEntryType.FILE, provider );
                RpmBuilder.this.addFile ( targetName, source, info.getMode (), info.getTimestamp (), entry -> customizeFile ( entry, info ) );
            }

            @Override
            public void addFile ( final String targetName, final InputStream source, final FileInformationProvider<Object> provider ) throws IOException
            {
                final FileInformation info = makeInformation ( source, PayloadEntryType.FILE, provider );
                RpmBuilder.this.addFile ( targetName, source, info.getMode (), info.getTimestamp (), entry -> customizeFile ( entry, info ) );
            }

            @Override
            public void addFile ( final String targetName, final ByteBuffer source, final FileInformationProvider<Object> provider ) throws IOException
            {
                final FileInformation info = makeInformation ( source, PayloadEntryType.FILE, provider );
                RpmBuilder.this.addFile ( targetName, source, info.getMode (), info.getTimestamp (), entry -> customizeFile ( entry, info ) );
            }

            @Override
            public void addDirectory ( final String targetName, final FileInformationProvider<? super Directory> provider ) throws IOException
            {
                final FileInformation info = makeInformation ( BuilderContext.DIRECTORY, PayloadEntryType.DIRECTORY, provider );
                RpmBuilder.this.addDirectory ( targetName, info.getMode (), info.getTimestamp (), entry -> customizeDirectory ( entry, info ) );
            }

            @Override
            public void addSymbolicLink ( final String targetName, final String linkTo, final FileInformationProvider<? super SymbolicLink> provider ) throws IOException
            {
                final FileInformation info = makeInformation ( BuilderContext.SYMBOLIC_LINK, PayloadEntryType.SYMBOLIC_LINK, provider );
                RpmBuilder.this.addSymbolicLink ( targetName, linkTo, info.getMode (), info.getTimestamp (), entry -> customizeSymbolicLink ( entry, info ) );
            }
        };
    }

    public void setPreInstallationScript ( final String interpreter, final String script )
    {
        addRequirement ( interpreter, null, RpmDependencyFlags.INTERPRETER );
        addRequirement ( interpreter, null, RpmDependencyFlags.SCRIPT_PRE, RpmDependencyFlags.INTERPRETER );
        setScript ( RpmTag.PREINSTALL_SCRIPT_PROG, RpmTag.PREINSTALL_SCRIPT, interpreter, script );
    }

    public void setPreInstallationScript ( final String script )
    {
        setPreInstallationScript ( DEFAULT_INTERPRETER, script );
    }

    public void setPostInstallationScript ( final String interpreter, final String script )
    {
        addRequirement ( interpreter, null, RpmDependencyFlags.INTERPRETER );
        addRequirement ( interpreter, null, RpmDependencyFlags.SCRIPT_POST, RpmDependencyFlags.INTERPRETER );
        setScript ( RpmTag.POSTINSTALL_SCRIPT_PROG, RpmTag.POSTINSTALL_SCRIPT, interpreter, script );
    }

    public void setPostInstallationScript ( final String script )
    {
        setPostInstallationScript ( DEFAULT_INTERPRETER, script );
    }

    public void setPreRemoveScript ( final String interpreter, final String script )
    {
        addRequirement ( interpreter, null, RpmDependencyFlags.INTERPRETER );
        addRequirement ( interpreter, null, RpmDependencyFlags.SCRIPT_PREUN, RpmDependencyFlags.INTERPRETER );
        setScript ( RpmTag.PREREMOVE_SCRIPT_PROG, RpmTag.PREREMOVE_SCRIPT, interpreter, script );
    }

    public void setPreRemoveScript ( final String script )
    {
        setPreRemoveScript ( DEFAULT_INTERPRETER, script );
    }

    public void setPostRemoveScript ( final String interpreter, final String script )
    {
        addRequirement ( interpreter, null, RpmDependencyFlags.INTERPRETER );
        addRequirement ( interpreter, null, RpmDependencyFlags.SCRIPT_POSTUN, RpmDependencyFlags.INTERPRETER );
        setScript ( RpmTag.POSTREMOVE_SCRIPT_PROG, RpmTag.POSTREMOVE_SCRIPT, interpreter, script );
    }

    public void setPostRemoveScript ( final String script )
    {
        setPostRemoveScript ( DEFAULT_INTERPRETER, script );
    }

    public void setVerifyScript ( final String interpreter, final String script )
    {
        addRequirement ( interpreter, null, RpmDependencyFlags.INTERPRETER );
        addRequirement ( interpreter, null, RpmDependencyFlags.SCRIPT_VERIFY, RpmDependencyFlags.INTERPRETER );
        setScript ( RpmTag.VERIFY_SCRIPT_PROG, RpmTag.VERIFY_SCRIPT, interpreter, script );
    }

    public void setVerifyScript ( final String script )
    {
        setVerifyScript ( DEFAULT_INTERPRETER, script );
    }

    private void setScript ( final RpmTag interpreterTag, final RpmTag scriptTag, final String interpreter, final String script )
    {
        if ( interpreter == null || script == null )
        {
            this.header.remove ( interpreterTag );
            this.header.remove ( scriptTag );
        }
        else
        {
            this.header.putString ( interpreterTag, interpreter );
            this.header.putString ( scriptTag, script );
        }
    }
}
