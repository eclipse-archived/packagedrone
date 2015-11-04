/*******************************************************************************
 * Copyright (c) 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Rathgeb - initial API and implementation
 *     Jens Reimann - adapt to new API
 *******************************************************************************/
package org.eclipse.packagedrone.repo.aspect.mvnosgi.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.aspect.common.osgi.OsgiAspectFactory;
import org.eclipse.packagedrone.repo.aspect.virtual.Virtualizer;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.eclipse.packagedrone.repo.utils.osgi.bundle.BundleInformation;
import org.osgi.framework.Version;

public class VirtualizerImpl implements Virtualizer
{
    //private final static Logger logger = LoggerFactory.getLogger ( VirtualizerImpl.class );

    @Override
    public void virtualize ( final Context context )
    {
        try
        {
            processVirtualize ( context );
        }
        catch ( final Exception e )
        {
            throw new RuntimeException ( e );
        }
    }

    private void processVirtualize ( final Context context ) throws Exception
    {
        // The group ID will be set using the provided channel meta data or the default one.
        String groupId = null;

        // Extract some informations (e.g. group ID) from the provided channel meta data.
        final Map<MetaKey, String> channelMetaData = context.getProvidedChannelMetaData ();
        for ( final Entry<MetaKey, String> entry : channelMetaData.entrySet () )
        {
            final MetaKey metaKey = entry.getKey ();

            if ( metaKey.getNamespace ().equals ( Constants.METADATA_NAMESPACE ) )
            {
                switch ( metaKey.getKey () )
                {
                    case Constants.METADATA_KEY_GROUPID:
                        groupId = entry.getValue ();
                        break;
                    default:
                        break;
                }
            }
        }

        // If the group ID is not set or empty, use the default one.
        if ( groupId == null || groupId.isEmpty () )
        {
            groupId = Constants.DEFAULT_GROUPID;
        }

        final ArtifactInformation art = context.getArtifactInformation ();

        final BundleInformation bi = OsgiAspectFactory.fetchBundleInformation ( art.getMetaData () );
        if ( bi != null )
        {
            final Version version = bi.getVersion ();
            final Pom pom = new Pom ( groupId, bi.getId (), version.toString () );
            createArtifact ( context, pom );
            return;
        }

        //final FeatureInformation fi = OsgiAspectFactory.fetchFeatureInformation ( art.getMetaData () );
    }

    private void createArtifact ( final Context context, final Pom pom ) throws IOException
    {
        try ( final InputStream inputStream = pom.getInputStream () )
        {
            final String name = makeName ( context.getArtifactInformation ().getName () );
            context.createVirtualArtifact ( name, inputStream, null );
        }
    }

    private String makeName ( final String name )
    {
        if ( name == null )
        {
            return Constants.DEFAULT_POM_NAME;
        }

        final int idx = name.lastIndexOf ( '.' );
        if ( idx < 0 || idx >= name.length () )
        {
            return Constants.DEFAULT_POM_NAME;
        }

        return name.substring ( 0, idx ) + Constants.DEFAULT_POM_POSTIFX;
    }

}
