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
package org.eclipse.packagedrone.repo.adapter.maven.internal;

import static org.eclipse.packagedrone.repo.xml.XmlFiles.isXml;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.Map;

import org.eclipse.packagedrone.repo.aspect.extract.Extractor;
import org.eclipse.packagedrone.repo.xml.XmlHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class MavenInformationExtractor implements Extractor
{
    private static final String NS = "http://maven.apache.org/POM/4.0.0";

    private final XmlHelper xml = new XmlHelper ();

    @Override
    public void extractMetaData ( final Extractor.Context context, final Map<String, String> metadata ) throws Exception
    {
        if ( !isXml ( context.getPath () ) )
        {
            return;
        }

        try ( BufferedInputStream in = new BufferedInputStream ( new FileInputStream ( context.getPath ().toFile () ) ) )
        {
            final Document doc = this.xml.parse ( in );
            final Element root = doc.getDocumentElement ();

            if ( !root.getNodeName ().equals ( "project" ) )
            {
                return;
            }

            final String ns = root.getNamespaceURI ();
            if ( ns != null && !ns.equals ( NS ) )
            {
                context.validationInformation ( "Ignoring POM file: The namespace set but is not: " + NS );
                return;
            }

            String groupId = this.xml.getElementValue ( root, "./groupId" );
            if ( groupId == null )
            {
                groupId = this.xml.getElementValue ( root, "./parent/groupId" );
            }

            final String artifactId = this.xml.getElementValue ( root, "./artifactId" );

            String version = this.xml.getElementValue ( root, "./version" );
            if ( version == null )
            {
                version = this.xml.getElementValue ( root, "./parent/version" );
            }

            if ( groupId == null || groupId.isEmpty () )
            {
                context.validationInformation ( "Ignoring POM file: There is no group id" );
                return;
            }

            if ( artifactId == null || artifactId.isEmpty () )
            {
                context.validationInformation ( "Ignoring POM file: There is no artifact id" );
                return;
            }

            if ( version == null || version.isEmpty () )
            {
                context.validationInformation ( "Ignoring POM file: There is no version" );
                return;
            }

            metadata.put ( "groupId", groupId );
            metadata.put ( "artifactId", artifactId );
            metadata.put ( "version", version );
            metadata.put ( "extension", "pom" );
        }
        catch ( final Exception e )
        {
            // silently ignore
        }
    }

}
