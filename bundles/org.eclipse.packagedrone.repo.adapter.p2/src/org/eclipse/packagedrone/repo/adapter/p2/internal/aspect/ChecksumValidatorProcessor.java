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

import static org.eclipse.packagedrone.repo.XmlHelper.getElementValue;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.packagedrone.repo.XmlHelper;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class ChecksumValidatorProcessor extends AbstractDocumentProcessor
{
    public static String CTX_KEY_SKIP_SET = ChecksumValidatorProcessor.class.getName () + ".skipSet";

    private final static Logger logger = LoggerFactory.getLogger ( ChecksumValidatorProcessor.class );

    private final Set<String> installableUnits = new HashSet<> ();

    private final Multimap<String, String> checksums = HashMultimap.create ();

    private final Multimap<String, String> checksumArtifacts = HashMultimap.create ();

    private final XPathExpression artifactExpression;

    private final XPathExpression md5Expression;

    public ChecksumValidatorProcessor ( final DocumentCache cache, final XPathFactory pathFactory ) throws XPathExpressionException
    {
        super ( cache );

        final XPath path = pathFactory.newXPath ();
        this.artifactExpression = path.compile ( "//artifact" );
        this.md5Expression = path.compile ( "./properties/property[@name='download.md5']/@value" );
    }

    @Override
    public boolean process ( final ArtifactInformation artifact, final ArtifactStreamer streamer, final Map<String, Object> context ) throws Exception
    {
        final String ft = artifact.getMetaData ().get ( ChannelStreamer.MK_FRAGMENT_TYPE );

        if ( "artifacts".equals ( ft ) )
        {
            processP2Artifact ( artifact, streamer, context );
        }

        return true;
    }

    private void processP2Artifact ( final ArtifactInformation artifact, final ArtifactStreamer streamer, final Map<String, Object> context ) throws Exception
    {
        this.cache.stream ( artifact, streamer, ( info, doc ) -> {
            for ( final Node node : XmlHelper.iter ( XmlHelper.executePath ( doc, this.artifactExpression ) ) )
            {
                if ( ! ( node instanceof Element ) )
                {
                    continue;
                }

                recordArtifact ( artifact, (Element)node, context );
            }
        } );
    }

    /**
     * Record the artifact for duplicate detection
     *
     * @param artifact
     *            the current artifact
     * @param context
     * @param node
     *            the artifact node
     * @return <code>true</code> if the artifact does not cause any problems,
     *         <code>false</code> otherwise
     * @throws Exception
     */
    private void recordArtifact ( final ArtifactInformation artifact, final Element ele, final Map<String, Object> context ) throws Exception
    {
        final String key = makeKey ( ele );

        final String value = getElementValue ( ele, this.md5Expression );

        if ( value == null || value.isEmpty () )
        {
            logger.debug ( "Artifact {} did not have a checksum", key );
        }

        final boolean result = this.installableUnits.add ( key );

        if ( !result )
        {
            markSkip ( context, key );
        }

        this.checksums.put ( key, value );
        this.checksumArtifacts.put ( fullKey ( key, value ), artifact.getId () );

        logger.debug ( "Recording artifact - id: {}, md5: {}, artifact: {} -> result: {}", key, value, artifact.getId (), result );
    }

    /**
     * Make a list of all installable units which have conflicting MD5 checksums
     *
     * @return a map of the conflicting MD5 checksums, each set in the list is
     *         one group of artifacts which share the same key but have
     *         different checksums. The key is the combination of classifier, id
     *         and version.
     */
    public Map<String, Set<String>> checkDuplicates ()
    {
        final Map<String, Set<String>> result = new HashMap<> ();

        for ( final Map.Entry<String, Collection<String>> entry : this.checksums.asMap ().entrySet () )
        {
            if ( entry.getValue ().size () < 2 )
            {
                continue;
            }

            final Set<String> artifacts = new HashSet<> ();

            for ( final String value : entry.getValue () )
            {
                artifacts.addAll ( this.checksumArtifacts.get ( fullKey ( entry.getKey (), value ) ) );
            }

            result.put ( entry.getKey (), artifacts );
        }

        return result;
    }

    @SuppressWarnings ( "unchecked" )
    private static void markSkip ( final Map<String, Object> context, final String key )
    {
        Object value = context.get ( CTX_KEY_SKIP_SET );
        if ( value == null || ! ( value instanceof Set ) )
        {
            value = new HashSet<String> ();
            context.put ( CTX_KEY_SKIP_SET, value );
        }

        final Set<String> set = (Set<String>)value;
        set.add ( key );
    }

    public static boolean shouldSkip ( final Map<String, Object> context, final String key )
    {
        final Object value = context.get ( CTX_KEY_SKIP_SET );
        if ( value == null || ! ( value instanceof Set ) )
        {
            return false;
        }

        return ( (Set<?>)value ).contains ( key );
    }

    public static String makeKey ( final Element ele )
    {
        final String classifier = ele.getAttribute ( "classifier" );
        final String id = ele.getAttribute ( "id" );
        final String version = ele.getAttribute ( "version" );

        return String.format ( "%s::%s::%s", classifier, id, version );
    }

    private static String fullKey ( final String key, final String checksum )
    {
        return key + "::" + checksum;
    }

    @Override
    public void write ( final OutputStream stream ) throws IOException
    {
    }

    @Override
    public String getId ()
    {
        return null;
    }

}
