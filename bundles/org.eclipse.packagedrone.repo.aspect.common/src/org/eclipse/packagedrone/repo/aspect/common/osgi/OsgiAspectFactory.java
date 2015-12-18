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
package org.eclipse.packagedrone.repo.aspect.common.osgi;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.aspect.ChannelAspect;
import org.eclipse.packagedrone.repo.aspect.ChannelAspectFactory;
import org.eclipse.packagedrone.repo.aspect.extract.Extractor;
import org.eclipse.packagedrone.repo.utils.osgi.bundle.BundleInformation;
import org.eclipse.packagedrone.repo.utils.osgi.feature.FeatureInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OsgiAspectFactory implements ChannelAspectFactory
{
    private final static Logger logger = LoggerFactory.getLogger ( OsgiAspectFactory.class );

    public static final @NonNull String ID = "osgi";

    private static class ChannelAspectImpl implements ChannelAspect
    {
        @Override
        public @NonNull Extractor getExtractor ()
        {
            return new OsgiExtractor ();
        }

        @Override
        public @NonNull String getId ()
        {
            return ID;
        }
    }

    @Override
    public @NonNull ChannelAspect createAspect ()
    {
        return new ChannelAspectImpl ();
    }

    public static <T extends BundleInformation> @Nullable T fetchBundleInformation ( @NonNull final Map<MetaKey, String> metadata, @NonNull final Class<T> clazz )
    {
        final String string = metadata.get ( OsgiExtractor.KEY_BUNDLE_INFORMATION );
        if ( string == null )
        {
            return null;
        }

        try
        {
            return BundleInformation.fromJson ( string, clazz );
        }
        catch ( final Exception e )
        {
            logger.debug ( "Failed to parse bundle information", e );
            return null;
        }
    }

    public static <T extends FeatureInformation> @Nullable T fetchFeatureInformation ( @NonNull final Map<MetaKey, String> metadata, @NonNull final Class<T> clazz )
    {
        final String string = metadata.get ( OsgiExtractor.KEY_FEATURE_INFORMATION );
        if ( string == null )
        {
            return null;
        }

        try
        {
            return FeatureInformation.fromJson ( string, clazz );
        }
        catch ( final Exception e )
        {
            logger.debug ( "Failed to parse feature information", e );
            return null;
        }
    }

    public static BundleInformation fetchBundleInformation ( @NonNull final Map<MetaKey, String> metadata )
    {
        return fetchBundleInformation ( metadata, BundleInformation.class );
    }

    public static FeatureInformation fetchFeatureInformation ( @NonNull final Map<MetaKey, String> metadata )
    {
        return fetchFeatureInformation ( metadata, FeatureInformation.class );
    }

}
