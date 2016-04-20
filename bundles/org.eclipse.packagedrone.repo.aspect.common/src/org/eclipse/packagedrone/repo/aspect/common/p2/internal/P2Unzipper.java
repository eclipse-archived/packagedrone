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
package org.eclipse.packagedrone.repo.aspect.common.p2.internal;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpression;

import org.apache.commons.io.input.CloseShieldInputStream;
import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.XmlHelper;
import org.eclipse.packagedrone.repo.aspect.common.p2.P2UnzipAspectFactory;
import org.eclipse.packagedrone.repo.aspect.virtual.Virtualizer;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.eclipse.packagedrone.utils.xml.XmlToolsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.io.ByteStreams;

public class P2Unzipper implements Virtualizer
{
    private final static Logger logger = LoggerFactory.getLogger ( P2Unzipper.class );

    private static final String FILENAME_CONTENT_JAR = "content.jar";

    private static final String FILENAME_ARTIFACTS_JAR = "artifacts.jar";

    private static final String FILENAME_CONTENT_XML = "content.xml";

    private static final String FILENAME_ARTIFACTS_XML = "artifacts.xml";

    private static final String XPATH_EXTRACT_CONTENT = "/repository/units";

    private static final String XPATH_EXTRACT_ARTIFACTS = "/repository/artifacts";

    private static final MetaKey KEY_REUSE_METADATA = new MetaKey ( "p2.unzip", "reuse-metadata" );

    private final XmlToolsFactory xml;

    public P2Unzipper ( final XmlToolsFactory xml )
    {
        this.xml = xml;
    }

    @Override
    public void virtualize ( final Context context )
    {
        final ArtifactInformation ai = context.getArtifactInformation ();

        if ( !isZip ( ai ) )
        {
            return;
        }

        final boolean reuseMetadata = Boolean.parseBoolean ( context.getProvidedChannelMetaData ().get ( KEY_REUSE_METADATA ) );

        try ( ZipInputStream zis = new ZipInputStream ( new BufferedInputStream ( new FileInputStream ( context.getFile ().toFile () ) ) ) )
        {
            ZipEntry entry;
            while ( ( entry = zis.getNextEntry () ) != null )
            {
                if ( entry.isDirectory () )
                {
                    // skip directories
                    continue;
                }
                if ( entry.getName ().startsWith ( "features/" ) )
                {
                    processEntry ( context, entry, zis );
                }
                else if ( entry.getName ().startsWith ( "plugins/" ) )
                {
                    processEntry ( context, entry, zis );
                }
                else if ( reuseMetadata && entry.getName ().equals ( FILENAME_ARTIFACTS_XML ) )
                {
                    try
                    {
                        processMetaData ( context, zis, FILENAME_ARTIFACTS_XML, XPATH_EXTRACT_ARTIFACTS );
                    }
                    catch ( final Exception e )
                    {
                        // simply don't write this file
                        logger.warn ( "Failed to extract artifacts meta data", e );
                    }
                }
                else if ( reuseMetadata && entry.getName ().equals ( FILENAME_CONTENT_XML ) )
                {
                    try
                    {
                        processMetaData ( context, zis, FILENAME_CONTENT_XML, XPATH_EXTRACT_CONTENT );
                    }
                    catch ( final Exception e )
                    {
                        // simply don't write this file
                        logger.warn ( "Failed to extract content meta data", e );
                    }
                }
                else if ( reuseMetadata && entry.getName ().equals ( FILENAME_ARTIFACTS_JAR ) )
                {
                    try
                    {
                        processZippedMetaData ( context, zis, FILENAME_ARTIFACTS_XML, XPATH_EXTRACT_ARTIFACTS );
                    }
                    catch ( final Exception e )
                    {
                        // simply don't write this file
                        logger.warn ( "Failed to extract artifacts meta data", e );
                    }
                }
                else if ( reuseMetadata && entry.getName ().equals ( FILENAME_CONTENT_JAR ) )
                {
                    try
                    {
                        processZippedMetaData ( context, zis, FILENAME_CONTENT_XML, XPATH_EXTRACT_CONTENT );
                    }
                    catch ( final Exception e )
                    {
                        // simply don't write this file
                        logger.warn ( "Failed to extract content meta data", e );
                    }
                }
            }
        }
        catch ( final IOException e )
        {
            logger.debug ( "Failed to unzip", e );
            // we need to throw, since we might have already created virtual artifacts
            throw new RuntimeException ( "Failed to unzip", e );
        }
    }

    private void processZippedMetaData ( final Context context, final ZipInputStream zis, final String filename, final String xpath ) throws Exception
    {
        final JarInputStream jin = new JarInputStream ( zis, false );
        ZipEntry entry;
        while ( ( entry = jin.getNextEntry () ) != null )
        {
            if ( entry.getName ().equals ( filename ) )
            {
                processMetaData ( context, jin, filename, xpath );
            }
        }
    }

    private void processMetaData ( final Context context, final InputStream in, final String filename, final String xpath ) throws Exception
    {
        // parse input
        final Document doc = this.xml.newDocumentBuilder ().parse ( new CloseShieldInputStream ( in ) );
        final XPathExpression path = this.xml.newXPathFactory ().newXPath ().compile ( xpath );

        // filter
        final NodeList result = XmlHelper.executePath ( doc, path );

        // write filtered output
        final Document fragmentDoc = this.xml.newDocumentBuilder ().newDocument ();
        Node node = result.item ( 0 );
        node = fragmentDoc.adoptNode ( node );
        fragmentDoc.appendChild ( node );

        // create artifact
        context.createVirtualArtifact ( filename, out -> {
            try
            {
                XmlHelper.write ( this.xml.newTransformerFactory (), fragmentDoc, new StreamResult ( out ) );
            }
            catch ( final Exception e )
            {
                throw new IOException ( e );
            }
        }, null );
    }

    private boolean isZip ( final ArtifactInformation ai )
    {
        if ( ai.getName ().toLowerCase ().endsWith ( ".zip" ) )
        {
            return true;
        }

        return false;
    }

    private void processEntry ( final Context context, final ZipEntry entry, final InputStream in )
    {
        final String segs[] = entry.getName ().split ( "\\/" );
        final String name = segs[segs.length - 1];

        final Map<MetaKey, String> metaData = new HashMap<> ( 1 );
        metaData.put ( P2UnzipAspectFactory.MK_FULL_NAME, entry.getName () );

        context.createVirtualArtifact ( name, out -> ByteStreams.copy ( in, out ), metaData );
    }

}
