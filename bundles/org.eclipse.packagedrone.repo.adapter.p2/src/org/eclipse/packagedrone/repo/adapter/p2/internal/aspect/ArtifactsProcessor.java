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

import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ArtifactsProcessor extends AbstractRepositoryProcessor
{
    private final static Logger logger = LoggerFactory.getLogger ( ArtifactsProcessor.class );

    private final Element artifacts;

    private final Document doc;

    private final XPathExpression artifactsExpression;

    public ArtifactsProcessor ( final String title, final boolean compressed, final DocumentCache cache, final XPathFactory pathFactory, final Map<String, String> additionalProperties )
    {
        super ( title, "artifacts", compressed, cache, additionalProperties );

        this.doc = initRepository ( "artifactRepository", "org.eclipse.equinox.p2.artifact.repository.simpleRepository" );
        final Element root = this.doc.getDocumentElement ();

        addProperties ( root );
        addMappings ( root );

        this.artifacts = addElement ( root, "artifacts" );

        try
        {
            this.artifactsExpression = pathFactory.newXPath ().compile ( "//artifact" );
        }
        catch ( final XPathExpressionException e )
        {
            throw new RuntimeException ( e );
        }
    }

    private void addMappings ( final Element root )
    {
        final Element mappings = addElement ( root, "mappings" );

        addMapping ( mappings, "(& (classifier=osgi.bundle))", "${repoUrl}/plugins/${id}/${version}/${id}_${version}.jar" );
        addMapping ( mappings, "(& (classifier=binary))", "${repoUrl}/binary/${id}/${version}/${id}_${version}" );
        addMapping ( mappings, "(& (classifier=org.eclipse.update.feature))", "${repoUrl}/features/${id}/${version}/${id}_${version}.jar" );

        fixSize ( mappings );
    }

    private void addMapping ( final Element mappings, final String rule, final String output )
    {
        final Element m = addElement ( mappings, "rule" );
        m.setAttribute ( "filter", rule );
        m.setAttribute ( "output", output );
    }

    @Override
    public boolean process ( final ArtifactInformation artifact, final ArtifactStreamer streamer, final Map<String, Object> context ) throws Exception
    {
        final String ft = artifact.getMetaData ().get ( ChannelStreamer.MK_FRAGMENT_TYPE );

        if ( "artifacts".equals ( ft ) )
        {
            attachP2Artifact ( artifact, streamer, context );
        }

        return true;
    }

    private void attachP2Artifact ( final ArtifactInformation artifact, final ArtifactStreamer streamer, final Map<String, Object> context ) throws Exception
    {
        this.cache.stream ( artifact, streamer, ( info, doc ) -> {
            for ( final Node node : iter ( executePath ( doc, this.artifactsExpression ) ) )
            {
                if ( ! ( node instanceof Element ) )
                {
                    continue;
                }

                final String key = ChecksumValidatorProcessor.makeKey ( (Element)node );
                if ( ChecksumValidatorProcessor.shouldSkip ( context, key ) )
                {
                    logger.trace ( "IU {} of artifact {} should be skipped", key, artifact.getId () );
                    continue;
                }

                final Node nn = this.artifacts.getOwnerDocument ().adoptNode ( node.cloneNode ( true ) );
                this.artifacts.appendChild ( nn );
            }
        } );
    }

    @Override
    public void write ( final OutputStream stream ) throws IOException
    {
        fixSize ( this.artifacts );
        write ( this.doc, stream );
    }

}
