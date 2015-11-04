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
package org.eclipse.packagedrone.repo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Helper class for file types
 */
public final class FileTypes
{
    private static final String OTHER_XML_TYPE = System.getProperty ( "drone.common.otherXmlType" );

    private FileTypes ()
    {
    }

    /**
     * Test if this file is an XML file
     *
     * @param path
     *            the path to check
     * @return <code>true</code> if this is an XML file
     * @throws IOException
     *             if the probing fails
     */
    public static boolean isXml ( final Path path ) throws IOException
    {
        final String probe = Files.probeContentType ( path );

        if ( "application/xml".equals ( probe ) )
        {
            return true;
        }

        if ( "text/xml".equals ( probe ) )
        {
            return true;
        }

        if ( OTHER_XML_TYPE != null && OTHER_XML_TYPE.equals ( probe ) )
        {
            return true;
        }

        return false;
    }
}
