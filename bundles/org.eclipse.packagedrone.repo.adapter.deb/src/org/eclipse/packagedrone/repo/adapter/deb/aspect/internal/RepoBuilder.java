/*******************************************************************************
 * Copyright (c) 2015, 2016 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.adapter.deb.aspect.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.eclipse.packagedrone.repo.Severity;
import org.eclipse.packagedrone.repo.adapter.deb.aspect.DistributionInformation;
import org.eclipse.packagedrone.repo.signing.SigningService;
import org.eclipse.packagedrone.repo.utils.HashHelper;
import org.eclipse.packagedrone.utils.deb.Packages;
import org.eclipse.scada.utils.str.StringHelper;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

/**
 * Build a repository <br/>
 * Before packages are added with
 * {@link #addPackage(String, String, String, PackageInformation)} all
 * distributions must be created with
 * {@link #addDistribution(String, DistributionInformation)}
 */
public class RepoBuilder
{

    private static final DateFormat DATE_FORMAT;

    static
    {
        DATE_FORMAT = new SimpleDateFormat ( "EEE, dd MMM YYYY HH:mm:ss z", Locale.US );
        DATE_FORMAT.setTimeZone ( TimeZone.getTimeZone ( "UTC" ) );
    }

    public static class Distribution
    {
        private final String name;

        private final DistributionInformation information;

        private final Map<String, Component> components = new TreeMap<> ();

        public Distribution ( final String name, final DistributionInformation information )
        {
            this.name = name;
            this.information = information;

            // populate components
            for ( final String component : information.getComponents () )
            {
                this.components.put ( component, new Component ( component, information.getArchitectures () ) );
            }
        }

        public DistributionInformation getInformation ()
        {
            return this.information;
        }

        public Map<String, Component> getComponents ()
        {
            return Collections.unmodifiableMap ( this.components );
        }

        public String getName ()
        {
            return this.name;
        }

        public void addPackage ( final String component, final String architecture, final PackageInformation packageInfo, final ValidationListener validationListener )
        {
            final Component comp = this.components.get ( component );
            if ( comp == null )
            {
                return;
            }

            if ( "all".equals ( architecture ) )
            {
                for ( final String arch : this.information.getArchitectures () )
                {
                    comp.addPackage ( "binary-" + arch, arch, packageInfo );
                }
            }
            else
            {
                if ( !this.information.getArchitectures ().contains ( architecture ) )
                {
                    if ( validationListener != null )
                    {
                        validationListener.validationMessage ( Severity.WARNING, String.format ( "Architecture '%s' is not configured. Package will be ignored.", architecture ) );
                    }
                    return;
                }

                comp.addPackage ( "binary-" + architecture, architecture, packageInfo );
            }
        }
    }

    public static class Component
    {
        private final String name;

        private final Map<String, SubComponent> subComponents = new HashMap<> ();

        public Component ( final String name, final SortedSet<String> archs )
        {
            this.name = name;

            for ( final String arch : archs )
            {
                final String subName = "binary-" + arch;
                this.subComponents.put ( subName, new SubComponent ( subName, arch ) );
            }
        }

        public String getName ()
        {
            return this.name;
        }

        public void addPackage ( final String subComponent, final String architecture, final PackageInformation packageInfo )
        {
            final SubComponent sub = this.subComponents.get ( subComponent );
            if ( sub == null )
            {
                return;
            }

            sub.addPackage ( packageInfo );
        }

        public Map<String, SubComponent> getSubComponents ()
        {
            return Collections.unmodifiableMap ( this.subComponents );
        }
    }

    public static class SubComponent
    {
        private final String name;

        private final StringWriter sw = new StringWriter ();

        private final PrintWriter pw = new PrintWriter ( this.sw );

        private final String architecture;

        public SubComponent ( final String name, final String architecture )
        {
            this.name = name;
            this.architecture = architecture;
        }

        public String getName ()
        {
            return this.name;
        }

        public String getArchitecture ()
        {
            return this.architecture;
        }

        public void addPackage ( final PackageInformation packageInfo )
        {
            final Map<String, String> values = new HashMap<> ( packageInfo.getControl () );

            values.put ( "Filename", packageInfo.getPoolName () );
            values.put ( "Size", "" + packageInfo.getFileSize () );

            if ( !values.containsKey ( "Description-md5" ) )
            {
                values.put ( "Description-md5", Packages.makeDescriptionMd5 ( values.get ( "Description" ) ) );
            }

            // add checksum entries

            for ( final Map.Entry<String, String> entry : packageInfo.getChecksums ().entrySet () )
            {
                final String v = entry.getValue ();
                if ( v != null )
                {
                    values.put ( entry.getKey (), v );
                }
            }

            try
            {
                Packages.writeBinaryPackageValues ( this.pw, values );
            }
            catch ( final IOException e )
            {
                throw new RuntimeException ( "Failed to write package stream", e );
            }

            this.pw.print ( "\n" );
        }

        public byte[] toReleaseFile ( final Distribution dist, final Component comp )
        {
            final StringWriter sw = new StringWriter ();

            final DistributionInformation info = dist.getInformation ();

            writeOptional ( sw, "Version", info.getVersion () );
            writeOptional ( sw, "Origin", info.getOrigin () );
            writeOptional ( sw, "Label", info.getLabel () );
            writeOptional ( sw, "Archive", info.getSuite () );

            sw.write ( "Component: " + comp.getName () + "\n" );
            sw.write ( "Architecture: " + this.architecture + "\n" );

            return sw.toString ().getBytes ( StandardCharsets.UTF_8 );
        }

        public byte[] toPackageFile ()
        {
            return this.sw.toString ().getBytes ( StandardCharsets.UTF_8 );
        }
    }

    public static class PackageInformation
    {
        private final String poolName;

        private final Map<String, String> control;

        private final long fileSize;

        private final Map<String, String> checksums;

        public PackageInformation ( final String poolName, final long fileSize, final Map<String, String> control, final Map<String, String> checksums )
        {
            this.poolName = poolName;
            this.fileSize = fileSize;
            this.control = control;
            this.checksums = checksums;
        }

        public long getFileSize ()
        {
            return this.fileSize;
        }

        public Map<String, String> getControl ()
        {
            return this.control;
        }

        public String getPoolName ()
        {
            return this.poolName;
        }

        public Map<String, String> getChecksums ()
        {
            return this.checksums;
        }
    }

    private final Map<String, Distribution> distributions = new HashMap<> ();

    private final SigningService signingService;

    public RepoBuilder ( final SigningService signingService )
    {
        this.signingService = signingService;
    }

    public void addDistribution ( final String name, final DistributionInformation information )
    {
        this.distributions.put ( name, new Distribution ( name, information ) );
    }

    public void addPackage ( final String distribution, final String component, final String architecture, final PackageInformation packageInfo )
    {
        addPackage ( distribution, component, architecture, packageInfo, null );
    }

    public void addPackage ( final String distribution, final String component, final String architecture, final PackageInformation packageInfo, final ValidationListener validationListener )
    {
        final Distribution dist = this.distributions.get ( distribution );
        if ( dist == null )
        {
            return; // ignore
        }

        dist.addPackage ( component, architecture, packageInfo, validationListener );
    }

    private static class Checksums
    {
        private final Map<String, HashCode> codes;

        private final int size;

        public Checksums ( final Map<String, HashCode> result, final int length )
        {
            this.codes = result;
            this.size = length;
        }

        public Map<String, HashCode> getCodes ()
        {
            return this.codes;
        }

        public int getSize ()
        {
            return this.size;
        }

        public static Checksums create ( final byte[] data ) throws IOException
        {
            final Map<String, HashFunction> functions = new HashMap<> ();

            functions.put ( "MD5Sum", Hashing.md5 () );
            functions.put ( "SHA1", Hashing.sha1 () );
            functions.put ( "SHA256", Hashing.sha256 () );

            final Map<String, HashCode> result = HashHelper.createChecksums ( new ByteArrayInputStream ( data ), functions );

            return new Checksums ( result, data.length );
        }
    }

    public void spoolOut ( final SpoolOutHandler handler ) throws IOException
    {
        for ( final Distribution dist : this.distributions.values () )
        {
            final SortedMap<String, Checksums> checksums = new TreeMap<> ();

            for ( final Component comp : dist.getComponents ().values () )
            {
                for ( final SubComponent sub : comp.getSubComponents ().values () )
                {
                    spoolOutFile ( checksums, "dists/" + dist.getName (), String.format ( "%s/%s/Release", comp.getName (), sub.getName () ), "text/plain", sub.toReleaseFile ( dist, comp ), handler );

                    final byte[] pkgData = sub.toPackageFile ();

                    spoolOutFile ( checksums, "dists/" + dist.getName (), String.format ( "%s/%s/Packages", comp.getName (), sub.getName () ), "text/plain", pkgData, handler );
                    spoolOutFile ( checksums, "dists/" + dist.getName (), String.format ( "%s/%s/Packages.gz", comp.getName (), sub.getName () ), "application/x-gzip", compressGzip ( pkgData ), handler );
                    spoolOutFile ( checksums, "dists/" + dist.getName (), String.format ( "%s/%s/Packages.bz2", comp.getName (), sub.getName () ), "application/x-bzip2", compressBzip2 ( pkgData ), handler );
                }
            }

            spoolOutDistRelease ( dist, checksums, handler );
        }
    }

    private byte[] compressGzip ( final byte[] data ) throws IOException
    {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream ();
        final GZIPOutputStream gos = new GZIPOutputStream ( bos );

        gos.write ( data );

        gos.close ();
        return bos.toByteArray ();
    }

    private byte[] compressBzip2 ( final byte[] data ) throws IOException
    {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream ();
        final BZip2CompressorOutputStream b2os = new BZip2CompressorOutputStream ( bos );

        b2os.write ( data );

        b2os.close ();
        return bos.toByteArray ();
    }

    private void spoolOutDistRelease ( final Distribution dist, final SortedMap<String, Checksums> checksums, final SpoolOutHandler handler ) throws IOException
    {
        final StringWriter sw = new StringWriter ();

        final DistributionInformation info = dist.getInformation ();

        writeOptional ( sw, "Origin", info.getOrigin () );
        writeOptional ( sw, "Label", info.getLabel () );
        writeOptional ( sw, "Suite", info.getSuite () );
        writeOptional ( sw, "Version", info.getVersion () );
        writeOptional ( sw, "Codename", info.getCodename () );
        write ( sw, "Date", DATE_FORMAT.format ( new Date () ) );
        write ( sw, "Components", StringHelper.join ( dist.getComponents ().keySet (), " " ) );
        write ( sw, "Architectures", StringHelper.join ( info.getArchitectures (), " " ) );
        writeOptional ( sw, "Description", info.getDescription () );

        {
            // create checksum fields

            final StringWriter md5 = new StringWriter ();
            final StringWriter sha1 = new StringWriter ();
            final StringWriter sha256 = new StringWriter ();
            final PrintWriter md5Pw = new PrintWriter ( md5 );
            final PrintWriter sha1Pw = new PrintWriter ( sha1 );
            final PrintWriter sha256Pw = new PrintWriter ( sha256 );

            for ( final Map.Entry<String, Checksums> entry : checksums.entrySet () )
            {
                addChecksum ( md5Pw, entry.getKey (), entry.getValue (), "MD5Sum" );
                addChecksum ( sha1Pw, entry.getKey (), entry.getValue (), "SHA1" );
                addChecksum ( sha256Pw, entry.getKey (), entry.getValue (), "SHA256" );
            }

            write ( sw, "MD5Sum", md5.toString () );
            write ( sw, "SHA1", sha1.toString () );
            write ( sw, "SHA256", sha256.toString () );
        }

        final byte[] data = sw.toString ().getBytes ( StandardCharsets.UTF_8 );
        handler.spoolOut ( String.format ( "dists/%s/Release", dist.getName () ), "text/plain", new ByteArrayInputStream ( data ) );

        if ( this.signingService != null )
        {
            handler.spoolOut ( String.format ( "dists/%s/Release.gpg", dist.getName () ), "text/plain", new ByteArrayInputStream ( sign ( data, false ) ) );
            handler.spoolOut ( String.format ( "dists/%s/InRelease", dist.getName () ), "text/plain", new ByteArrayInputStream ( sign ( data, true ) ) );

            final ByteArrayOutputStream bos = new ByteArrayOutputStream ();
            this.signingService.printPublicKey ( bos );
            bos.close ();

            handler.spoolOut ( "GPG-KEY", "text/plain", new ByteArrayInputStream ( bos.toByteArray () ) );
        }
    }

    private byte[] sign ( final byte[] data, final boolean inline ) throws IOException
    {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream ();
        try
        {
            this.signingService.sign ( new ByteArrayInputStream ( data ), bos, inline );
        }
        catch ( final Exception e )
        {
            throw new RuntimeException ( "Failed to sign", e );
        }
        finally
        {
            bos.close ();
        }

        return bos.toByteArray ();
    }

    private void addChecksum ( final PrintWriter writer, final String name, final Checksums chk, final String alg )
    {
        final HashCode hashCode = chk.getCodes ().get ( alg );
        writer.format ( "\n %s %16d %s", hashCode.toString (), chk.getSize (), name );
    }

    private void spoolOutFile ( final Map<String, Checksums> checksums, final String prefix, final String fileName, final String mimeType, final byte[] data, final SpoolOutHandler handler ) throws IOException
    {
        checksums.put ( fileName, Checksums.create ( data ) );
        handler.spoolOut ( prefix + "/" + fileName, mimeType, new ByteArrayInputStream ( data ) );
    }

    /**
     * Write field only when the value is set
     *
     * @param writer
     *            the writer to use
     * @param fieldName
     *            the name of field
     * @param value
     *            the value
     */
    protected static void writeOptional ( final StringWriter writer, final String fieldName, final String value )
    {
        if ( value != null )
        {
            write ( writer, fieldName, value );
        }
    }

    /**
     * Write a field
     *
     * @param writer
     *            the writer to use
     * @param fieldName
     *            the field name
     * @param value
     *            the value, should not be <code>null</code> since this would
     *            cause the string
     *            <q>null</q> in the file.
     */
    protected static void write ( final StringWriter writer, final String fieldName, final String value )
    {
        writer.write ( fieldName + ": " + value + "\n" );
    }
}
