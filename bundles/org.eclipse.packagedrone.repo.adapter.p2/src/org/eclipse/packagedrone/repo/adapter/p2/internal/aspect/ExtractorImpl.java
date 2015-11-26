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
package org.eclipse.packagedrone.repo.adapter.p2.internal.aspect;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.packagedrone.repo.XmlHelper;
import org.eclipse.packagedrone.repo.adapter.p2.aspect.P2RepoConstants;
import org.eclipse.packagedrone.repo.aspect.Constants;
import org.eclipse.packagedrone.repo.aspect.extract.Extractor;
import org.eclipse.packagedrone.utils.xml.XmlToolsFactory;
import org.eclipse.scada.utils.str.StringHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ExtractorImpl implements Extractor
{
    static final String DELIM = ";";

    private final XmlToolsFactory xml;

    public ExtractorImpl ( final XmlToolsFactory xmlToolsFactory )
    {
        this.xml = xmlToolsFactory;
    }

    @Override
    public void extractMetaData ( final Extractor.Context context, final Map<String, String> metadata ) throws Exception
    {
        final DocumentBuilder db = this.xml.newDocumentBuilder ();

        try ( InputStream in = new BufferedInputStream ( Files.newInputStream ( context.getPath () ) ) )
        {
            db.setErrorHandler ( null );
            // use a stream, to prevent possible redirects to the file system
            final Document doc = db.parse ( in );
            if ( "artifacts".equals ( doc.getDocumentElement ().getTagName () ) )
            {
                processArtifacts ( context, metadata, doc );
            }
            else if ( "units".equals ( doc.getDocumentElement ().getTagName () ) )
            {
                processMetadata ( context, metadata, doc );
            }
        }
        catch ( final Exception e )
        {
            // ignore
        }
    }

    private void processArtifacts ( final Context context, final Map<String, String> metadata, final Document doc ) throws Exception
    {
        metadata.put ( "fragment", "true" );
        metadata.put ( P2RepoConstants.KEY_FRAGMENT_TYPE.getKey (), "artifacts" );
        metadata.put ( Constants.KEY_ARTIFACT_LABEL, "P2 Artifact Information" );

        int count = 0;

        final List<String> keys = new LinkedList<> ();
        final List<String> sums = new LinkedList<> ();

        try ( StringWriter sw = new StringWriter () )
        {
            for ( final Element ele : XmlHelper.iterElement ( doc.getDocumentElement (), "artifact" ) )
            {
                if ( count > 0 )
                {
                    sw.append ( DELIM );
                }
                count++;
                XmlHelper.write ( this.xml.newTransformerFactory (), ele, new StreamResult ( sw ), t -> {
                    t.setOutputProperty ( OutputKeys.OMIT_XML_DECLARATION, "yes" );
                } );
                keys.add ( makeKey ( ele ) );

                String md5 = "";
                for ( final Element props : XmlHelper.iterElement ( ele, "properties" ) )
                {
                    for ( final Element p : XmlHelper.iterElement ( props, "property" ) )
                    {
                        if ( "download.md5".equals ( p.getAttribute ( "name" ) ) )
                        {
                            md5 = p.getAttribute ( "value" );
                        }
                    }
                }
                sums.add ( md5 );
            }

            metadata.put ( P2RepoConstants.KEY_FRAGMENT_DATA.getKey (), sw.toString () );
            metadata.put ( P2RepoConstants.KEY_FRAGMENT_KEYS.getKey (), StringHelper.join ( keys, DELIM ) );
            metadata.put ( P2RepoConstants.KEY_FRAGMENT_MD5.getKey (), StringHelper.join ( sums, DELIM ) );
            metadata.put ( P2RepoConstants.KEY_FRAGMENT_COUNT.getKey (), "" + count );
        }
    }

    private void processMetadata ( final Context context, final Map<String, String> metadata, final Document doc ) throws Exception
    {
        metadata.put ( "fragment", "true" );
        metadata.put ( P2RepoConstants.KEY_FRAGMENT_TYPE.getKey (), "metadata" );
        metadata.put ( Constants.KEY_ARTIFACT_LABEL, "P2 Meta Data Fragment" );

        int count = 0;

        try ( StringWriter sw = new StringWriter () )
        {
            for ( final Element ele : XmlHelper.iterElement ( doc.getDocumentElement (), "unit" ) )
            {
                count++;
                XmlHelper.write ( this.xml.newTransformerFactory (), ele, new StreamResult ( sw ), t -> {
                    t.setOutputProperty ( OutputKeys.OMIT_XML_DECLARATION, "yes" );
                } );
            }

            metadata.put ( P2RepoConstants.KEY_FRAGMENT_DATA.getKey (), sw.toString () );
            metadata.put ( P2RepoConstants.KEY_FRAGMENT_COUNT.getKey (), "" + count );
        }
    }

    public static String makeKey ( final Element ele )
    {
        final String classifier = ele.getAttribute ( "classifier" );
        final String id = ele.getAttribute ( "id" );
        final String version = ele.getAttribute ( "version" );

        return String.format ( "%s::%s::%s", classifier, id, version );
    }

}
