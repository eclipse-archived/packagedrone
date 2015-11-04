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

import static org.eclipse.packagedrone.repo.FileTypes.isXml;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;

import org.eclipse.packagedrone.repo.XmlHelper;
import org.eclipse.packagedrone.repo.aspect.Constants;
import org.eclipse.packagedrone.repo.aspect.extract.Extractor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ExtractorImpl implements Extractor
{
    private final XmlHelper xml = new XmlHelper ();

    @Override
    public void extractMetaData ( final Extractor.Context context, final Map<String, String> metadata ) throws Exception
    {
        if ( !isXml ( context.getPath () ) )
        {
            return;
        }

        if ( isArtifacts ( context.getPath () ) )
        {
            metadata.put ( "fragment", "true" );
            metadata.put ( "fragment-type", "artifacts" );
            metadata.put ( Constants.KEY_ARTIFACT_LABEL, "P2 Artifact Information" );
        }
        else if ( isMetaData ( context.getPath () ) )
        {
            metadata.put ( "fragment", "true" );
            metadata.put ( "fragment-type", "metadata" );
            metadata.put ( Constants.KEY_ARTIFACT_LABEL, "P2 Meta Data Fragment" );
        }
    }

    private boolean isArtifacts ( final Path file )
    {
        try
        {
            try ( InputStream in = new BufferedInputStream ( new FileInputStream ( file.toFile () ) ) )
            {
                final Document doc = this.xml.parse ( in );
                final Element root = doc.getDocumentElement ();
                if ( root.getNodeName ().equals ( "artifacts" ) )
                {
                    return true;
                }
            }
        }
        catch ( final Exception e )
        {
        }

        return false;
    }

    private boolean isMetaData ( final Path file ) throws Exception
    {
        try
        {
            try ( InputStream in = new BufferedInputStream ( new FileInputStream ( file.toFile () ) ) )
            {
                final Document doc = this.xml.parse ( in );
                final Element root = doc.getDocumentElement ();
                if ( root.getNodeName ().equals ( "units" ) )
                {
                    return true;
                }
            }
        }
        catch ( final Exception e )
        {
        }

        return false;
    }

}
