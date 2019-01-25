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
package org.eclipse.packagedrone.repo.adapter.maven.upload;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.CharStreams;

/**
 * An upload helper for maven
 */
public class Uploader
{
    private final static Logger logger = LoggerFactory.getLogger ( Uploader.class );

    public static enum NoParentMode
    {
        IGNORE,
        ADD,
        FAIL;
    }

    private final Options options;

    private final UploadTarget target;

    public Uploader ( final UploadTarget target, final Options options )
    {
        this.target = target;

        this.options = new Options ( options );
    }

    public boolean receive ( final String path, final InputStream stream ) throws ChecksumValidationException, IOException
    {
        final Coordinates c = Coordinates.parse ( path );

        if ( c == null )
        {
            // unable to parse coordinates

            logger.info ( "Unable to parse maven coordinates from path: {}", path );
            return false;
        }

        if ( this.options.getIgnoreExtensions ().contains ( c.getExtension () ) )
        {
            return false;
        }

        final String alg = isCheckSum ( c );
        if ( alg != null )
        {
            // process checksum

            this.target.validateChecksum ( c.replaceExtension ( c.getExtension ().substring ( 0, c.getExtension ().length () - ( alg.length () + 1 ) ) ), alg, toChecksumString ( stream ) );
            return false;
        }

        if ( c.getClassifier () == null )
        {
            // primary

            return this.target.createArtifact ( c, stream, null ) != null;
        }
        else
        {
            // secondary

            Coordinates cp = c.makeUnclassified ();

            if ( !"jar".equals ( cp.getExtension () ) )
            {
                cp = cp.replaceExtension ( "jar" );
            }

            final Set<String> parents = this.target.findArtifacts ( cp );
            if ( parents.isEmpty () )
            {
                switch ( this.options.getNoParentMode () )
                {
                    case ADD:
                        return this.target.createArtifact ( c, stream, null ) != null;
                    case IGNORE:
                        return false;
                    case FAIL:
                        throw new IOException ( String.format ( "No parent found for: %s (parent: %s)", c, cp ) );
                    default:
                        throw new RuntimeException ( String.format ( "Unknown mode: %s", this.options.getNoParentMode () ) );
                }
            }
            else if ( parents.size () > 1 )
            {
                throw new IOException ( String.format ( "Duplicate parents detected: %s", parents ) );
            }
            else
            {
                return this.target.createArtifact ( parents.iterator ().next (), c, stream, null ) != null;
            }
        }
    }

    private String toChecksumString ( final InputStream stream ) throws IOException
    {
        if ( stream == null )
        {
            return "";
        }
        return CharStreams.toString ( new InputStreamReader ( stream, StandardCharsets.UTF_8 ) );
    }

    /**
     * Check if the uploaded artifact is actually a checksum file
     *
     * @param c
     *            the extracted coordinates
     * @return {@code true} if the upload is definitely a checksum file,
     *         {@code false} otherwise
     */
    private String isCheckSum ( final Coordinates c )
    {
        final String cext = c.getExtension ();

        if ( cext == null )
        {
            return null;
        }

        for ( final String ext : this.options.getChecksumExtensions () )
        {
            if ( cext.endsWith ( "." + ext ) )
            {
                return ext;
            }
        }

        return null;
    }

}
