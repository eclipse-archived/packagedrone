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
package org.eclipse.packagedrone.repo.aspect.common.p2.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.function.Supplier;

import javax.xml.stream.XMLOutputFactory;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.MetaKeys;
import org.eclipse.packagedrone.repo.aspect.common.osgi.OsgiAspectFactory;
import org.eclipse.packagedrone.repo.aspect.common.p2.P2MetaDataInformation;
import org.eclipse.packagedrone.repo.aspect.virtual.Virtualizer;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.eclipse.packagedrone.repo.utils.osgi.bundle.BundleInformation;
import org.eclipse.packagedrone.repo.utils.osgi.feature.FeatureInformation;
import org.eclipse.packagedrone.utils.Exceptions;
import org.eclipse.packagedrone.utils.io.IOConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class P2Virtualizer implements Virtualizer
{
    static final MetaKey KEY_MD5 = new MetaKey ( "hasher", "md5" );

    private final static Logger logger = LoggerFactory.getLogger ( P2Virtualizer.class );

    private final Supplier<XMLOutputFactory> factoryProvider;

    public P2Virtualizer ( final Supplier<XMLOutputFactory> factoryProvider )
    {
        this.factoryProvider = factoryProvider;
    }

    @Override
    public void virtualize ( final Context context )
    {
        Exceptions.wrapException ( () -> processVirtualize ( context ) );
    }

    private void processVirtualize ( final Context context ) throws Exception
    {
        final ArtifactInformation art = context.getArtifactInformation ();

        final Map<MetaKey, String> metaData = context.getProvidedChannelMetaData ();
        final P2MetaDataInformation info = new P2MetaDataInformation ();
        MetaKeys.bind ( info, metaData );

        logger.debug ( "Process virtualize - artifactId: {} / {}", art.getId (), art.getName () );

        final Creator creator = new Creator ( new Creator.Context () {

            @Override
            public void create ( final String name, final IOConsumer<OutputStream> producer ) throws IOException
            {
                context.createVirtualArtifact ( name, producer, null );
            }
        }, this.factoryProvider );

        final BundleInformation bi = OsgiAspectFactory.fetchBundleInformation ( art.getMetaData () );
        if ( bi != null )
        {
            logger.debug ( "Process as bundle: {} ({})- {}", art.getName (), art.getId (), bi );
            creator.createBundleP2MetaData ( info, art, bi );
            creator.createBundleP2Artifacts ( art, bi );
            return;
        }

        final FeatureInformation fi = OsgiAspectFactory.fetchFeatureInformation ( art.getMetaData () );
        if ( fi != null )
        {
            logger.debug ( "Process as feature: {} ({}) - {}", art.getName (), art.getId (), fi );
            creator.createFeatureP2MetaData ( art, fi );
            creator.createFeatureP2Artifacts ( art, fi );
            return;
        }
    }

}
