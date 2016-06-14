/*******************************************************************************
 * Copyright (c) 2014, 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.generator.p2;

import static org.eclipse.packagedrone.repo.MetaKeys.getString;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.eclipse.packagedrone.repo.channel.ChannelArtifactInformation;
import org.eclipse.packagedrone.repo.generator.ArtifactGenerator;
import org.eclipse.packagedrone.repo.generator.GenerationContext;
import org.eclipse.packagedrone.repo.xml.XmlHelper;
import org.eclipse.packagedrone.web.LinkTarget;
import org.w3c.dom.Element;

public class CategoryGenerator implements ArtifactGenerator
{
    public static final String ID = "p2.category";

    @Override
    public LinkTarget getAddTarget ()
    {
        return LinkTarget.createFromController ( GeneratorController.class, "createCategory" );
    }

    @Override
    public LinkTarget getEditTarget ( final ChannelArtifactInformation artifact )
    {
        final Map<String, String> model = new HashMap<> ( 2 );

        model.put ( "channelId", artifact.getChannelId ().getId () );
        model.put ( "artifactId", artifact.getId () );

        final String url = LinkTarget.createFromController ( GeneratorController.class, "editCategory" ).render ( model );
        return new LinkTarget ( url );
    }

    @Override
    public void generate ( final GenerationContext context ) throws Exception
    {
        final String id = getString ( context.getArtifactInformation ().getMetaData (), ID, "id" );

        context.createVirtualArtifact ( String.format ( "%s-p2metadata.xml", id ), out -> {
            createMetaDataXml ( out, context.getArtifactInformation ().getMetaData (), context );
        } , null );

    }

    private void createMetaDataXml ( final OutputStream out, final Map<MetaKey, String> map, final GenerationContext context ) throws IOException
    {
        final String id = getString ( map, ID, "id" );
        final String name = getString ( map, ID, "name" );
        final String description = getString ( map, ID, "description" );

        final String version = getString ( map, ID, "version", Helper.makeDefaultVersion () );

        Helper.createFragmentFile ( out, ( units ) -> {

            Helper.createCategoryUnit ( units, id, name, description, version, ( unit ) -> {

                final Set<String> ctx = new HashSet<> ();

                final Element reqs = XmlHelper.addElement ( unit, "requires" );
                for ( final ArtifactInformation a : context.getChannelArtifacts () )
                {
                    if ( Helper.isFeature ( a.getMetaData () ) )
                    {
                        Helper.addFeatureRequirement ( ctx, reqs, a );
                    }
                }
                XmlHelper.fixSize ( reqs );
            } );

        } );
    }

    @Override
    public boolean shouldRegenerate ( final Object event )
    {
        return Helper.shouldRegenerateCategory ( event );
    }

}
