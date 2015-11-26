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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.eclipse.packagedrone.repo.internal.Activator;
import org.eclipse.packagedrone.utils.xml.XmlToolsFactory;

/**
 * Helper class for file types
 */
public final class FileTypes
{
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
        final XmlToolsFactory xml = Activator.getXmlToolsFactory ();
        final XMLInputFactory xin = xml.newXMLInputFactory ();

        try ( InputStream stream = new BufferedInputStream ( Files.newInputStream ( path ) ) )
        {
            try
            {
                final XMLStreamReader reader = xin.createXMLStreamReader ( stream );
                reader.next ();
                return true;
            }
            catch ( final XMLStreamException e )
            {
                return false;
            }
        }
    }
}
