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

import static org.eclipse.packagedrone.repo.XmlHelper.addElement;
import static org.eclipse.packagedrone.repo.XmlHelper.executePath;
import static org.eclipse.packagedrone.repo.XmlHelper.fixSize;
import static org.eclipse.packagedrone.repo.XmlHelper.iter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;

public class MetaDataProcessor extends AbstractRepositoryProcessor
{
    private final Element units;

    private final Document doc;

    private final XPathExpression unitExpression;

    public MetaDataProcessor ( final String title, final boolean compressed, final DocumentCache cache, final DocumentBuilder documentBuilder, final XPathFactory pathFactory, final Map<String, String> additionalProperties )
    {
        super ( title, "content", compressed, cache, additionalProperties );

        this.doc = documentBuilder.newDocument ();

        final ProcessingInstruction pi = this.doc.createProcessingInstruction ( "metadataRepository", "version=\"1.1.0\"" );
        this.doc.appendChild ( pi );

        final Element root = this.doc.createElement ( "repository" );
        this.doc.appendChild ( root );
        root.setAttribute ( "name", title );
        root.setAttribute ( "type", "org.eclipse.equinox.internal.p2.metadata.repository.LocalMetadataRepository" );
        root.setAttribute ( "version", "1" );

        addProperties ( root );

        this.units = addElement ( root, "units" );

        try
        {
            this.unitExpression = pathFactory.newXPath ().compile ( "//unit" );
        }
        catch ( final XPathExpressionException e )
        {
            throw new RuntimeException ( e );
        }
    }

    @Override
    public boolean process ( final ArtifactInformation artifact, final ArtifactStreamer streamer, final Map<String, Object> context ) throws Exception
    {
        final String ft = artifact.getMetaData ().get ( ChannelStreamer.MK_FRAGMENT_TYPE );

        if ( "metadata".equals ( ft ) )
        {
            attachP2Artifact ( artifact, streamer );
        }

        return true;
    }

    private void attachP2Artifact ( final ArtifactInformation artifact, final ArtifactStreamer streamer ) throws Exception
    {
        this.cache.stream ( artifact, streamer, ( info, doc ) -> {
            for ( final Node node : iter ( executePath ( doc, this.unitExpression ) ) )
            {
                final Node nn = this.units.getOwnerDocument ().adoptNode ( node.cloneNode ( true ) );
                this.units.appendChild ( nn );
            }
        } );
    }

    @Override
    public void write ( final OutputStream stream ) throws IOException
    {
        fixSize ( this.units );
        write ( this.doc, stream );
    }

}
