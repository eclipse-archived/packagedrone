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
package org.eclipse.packagedrone.repo.importer.aether.web;

import static org.eclipse.packagedrone.repo.XmlHelper.getText;

import java.io.StringReader;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;

import org.eclipse.packagedrone.repo.XmlHelper;
import org.eclipse.packagedrone.repo.importer.aether.MavenCoordinates;
import org.eclipse.packagedrone.utils.xml.XmlToolsFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public final class Helper
{
    private Helper ()
    {
    }

    public static Collection<MavenCoordinates> parse ( final String string, final XmlToolsFactory xml )
    {
        try
        {
            return parseCoordinates ( string );
        }
        catch ( final Exception e )
        {
        }
        try
        {
            return parseXml ( string, xml );
        }
        catch ( final Exception e )
        {
        }

        // format unrecognized

        return null;
    }

    private static Collection<MavenCoordinates> parseCoordinates ( final String string )
    {
        final String[] toks = string.split ( "[\\n\\r]+" );

        final Collection<MavenCoordinates> result = new LinkedList<> ();

        for ( String tok : toks )
        {
            tok = tok.trim ();
            if ( tok.isEmpty () )
            {
                // ignore empty lines
                continue;
            }
            result.add ( MavenCoordinates.fromString ( tok ) );
        }

        return result;
    }

    private static Collection<MavenCoordinates> parseXml ( final String string, final XmlToolsFactory xml ) throws Exception
    {
        final DocumentBuilder db = xml.newDocumentBuilder ();
        db.setErrorHandler ( null );

        Document doc;

        try
        {
            doc = db.parse ( new InputSource ( new StringReader ( string ) ) );
        }
        catch ( final SAXException e )
        {
            // retry with root element
            doc = db.parse ( new InputSource ( new StringReader ( "<dependencies>" + string + "</dependencies>" ) ) );
        }

        final Element root = doc.getDocumentElement ();

        if ( "dependencies".equals ( root.getTagName () ) )
        {
            return parseDependenciesXml ( root );
        }
        else if ( "dependency".equals ( root.getTagName () ) )
        {
            return Collections.singletonList ( parseDependencyXml ( root ) );
        }

        throw new IllegalStateException ();
    }

    private static Collection<MavenCoordinates> parseDependenciesXml ( final Element root )
    {
        final Collection<MavenCoordinates> result = new LinkedList<> ();

        for ( final Node node : XmlHelper.iter ( root.getChildNodes () ) )
        {
            if ( ! ( node instanceof Element ) )
            {
                continue;
            }

            final Element ele = (Element)node;
            if ( !"dependency".equals ( ele.getTagName () ) )
            {
                throw new IllegalStateException ();
            }

            result.add ( parseDependencyXml ( ele ) );
        }

        return result;
    }

    private static MavenCoordinates parseDependencyXml ( final Element ele )
    {
        final String groupId = getText ( ele, "groupId" );
        final String artifactId = getText ( ele, "artifactId" );
        final String version = getText ( ele, "version" );
        final String classifier = getText ( ele, "classifier " );
        final String extension = getText ( ele, "extension" );

        if ( groupId == null || artifactId == null || version == null )
        {
            throw new IllegalArgumentException ();
        }

        final MavenCoordinates coords = new MavenCoordinates ( groupId, artifactId, version );
        coords.setExtension ( extension );
        coords.setClassifier ( classifier );
        return coords;
    }
}
