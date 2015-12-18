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
package org.eclipse.packagedrone.repo.adapter.rpm.yum.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.adapter.rpm.Constants;
import org.eclipse.packagedrone.repo.adapter.rpm.RpmInformation;
import org.eclipse.packagedrone.repo.adapter.rpm.yum.RepositoryCreator;
import org.eclipse.packagedrone.repo.aspect.aggregate.AggregationContext;
import org.eclipse.packagedrone.repo.aspect.aggregate.ChannelAggregator;
import org.eclipse.packagedrone.repo.aspect.common.spool.ChannelCacheTarget;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.eclipse.packagedrone.repo.signing.SigningService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

public class YumChannelAggregator implements ChannelAggregator
{
    private static final MetaKey KEY_SIGNING_ID = new MetaKey ( "yum", "signingServiceId" );

    private static final MetaKey KEY_SHA1 = new MetaKey ( "hasher", "sha1" );

    private final BundleContext context;

    public YumChannelAggregator ()
    {
        this.context = FrameworkUtil.getBundle ( YumChannelAggregator.class ).getBundleContext ();
    }

    @Override
    public Map<String, String> aggregateMetaData ( final AggregationContext context ) throws Exception
    {
        final String signingServiceId = context.getChannelMetaData ().get ( KEY_SIGNING_ID );
        ServiceReference<SigningService> ssref = null;
        SigningService signingService = null;
        if ( signingServiceId != null && !signingServiceId.isEmpty () )
        {
            final Collection<ServiceReference<SigningService>> services = this.context.getServiceReferences ( SigningService.class, String.format ( "(%s=%s)", org.osgi.framework.Constants.SERVICE_PID, signingServiceId ) );

            if ( services == null || services.isEmpty () )
            {
                throw new IllegalStateException ( String.format ( "Unable to find configured signing service: %s", signingServiceId ) );
            }

            ssref = services.iterator ().next ();
            signingService = this.context.getService ( ssref );
        }

        try
        {
            final RepositoryCreator creator = new RepositoryCreator ( new ChannelCacheTarget ( context ), signingService );

            final Map<String, String> result = new HashMap<> ();

            creator.process ( repoContext -> {
                for ( final ArtifactInformation art : context.getArtifacts () )
                {
                    final RpmInformation info = RpmInformation.fromJson ( art.getMetaData ().get ( Constants.KEY_INFO ) );

                    if ( info == null )
                    {
                        continue;
                    }

                    final String sha1 = art.getMetaData ().get ( KEY_SHA1 );

                    repoContext.addPackage ( sha1, art, info );
                }
            } );

            return result;
        }
        finally
        {
            if ( signingService != null && ssref != null )
            {
                this.context.ungetService ( ssref );
            }
        }
    }
}
