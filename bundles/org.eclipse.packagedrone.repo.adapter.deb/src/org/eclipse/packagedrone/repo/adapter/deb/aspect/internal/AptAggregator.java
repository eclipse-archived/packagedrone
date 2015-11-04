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
package org.eclipse.packagedrone.repo.adapter.deb.aspect.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.MetaKeys;
import org.eclipse.packagedrone.repo.adapter.deb.ChannelConfiguration;
import org.eclipse.packagedrone.repo.adapter.deb.aspect.DebianAspectInformation;
import org.eclipse.packagedrone.repo.adapter.deb.aspect.DistributionInformation;
import org.eclipse.packagedrone.repo.adapter.deb.aspect.internal.RepoBuilder.PackageInformation;
import org.eclipse.packagedrone.repo.aspect.aggregate.AggregationContext;
import org.eclipse.packagedrone.repo.aspect.aggregate.ChannelAggregator;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.eclipse.packagedrone.repo.signing.SigningService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class AptAggregator implements ChannelAggregator
{
    private final BundleContext context;

    public AptAggregator ()
    {
        this.context = FrameworkUtil.getBundle ( AptAggregator.class ).getBundleContext ();
    }

    @Override
    public Map<String, String> aggregateMetaData ( final AggregationContext context ) throws Exception
    {
        final Map<String, String> result = new HashMap<> ();

        final Map<MetaKey, String> md = context.getChannelMetaData ();

        final ChannelConfiguration cfg = MetaKeys.bind ( new ChannelConfiguration (), md );

        if ( !cfg.isValid () )
        {
            return null;
        }

        final Gson gson = new GsonBuilder ().create ();

        ServiceReference<SigningService> serviceRef = null;
        SigningService signingService;
        if ( cfg.getSigningService () != null )
        {
            final Collection<ServiceReference<SigningService>> refs = this.context.getServiceReferences ( SigningService.class, String.format ( "(%s=%s)", Constants.SERVICE_PID, cfg.getSigningService () ) );
            if ( refs == null || refs.isEmpty () )
            {
                throw new IllegalStateException ( String.format ( "Signing service %s could not be found", cfg.getSigningService () ) );
            }
            serviceRef = refs.iterator ().next ();
            signingService = this.context.getService ( serviceRef );
        }
        else
        {
            signingService = null;
        }

        try
        {
            final RepoBuilder repo = new RepoBuilder ( signingService );

            final DistributionInformation info = new DistributionInformation ();
            info.setArchitectures ( new TreeSet<> ( cfg.getArchitectures () ) );
            info.setComponents ( new TreeSet<> ( Collections.singleton ( cfg.getDefaultComponent () ) ) );
            info.setVersion ( cfg.getVersion () );
            info.setDescription ( cfg.getDescription () );
            info.setLabel ( cfg.getLabel () );
            info.setSuite ( cfg.getSuite () );
            info.setCodename ( cfg.getCodename () );
            info.setOrigin ( cfg.getOrigin () );

            repo.addDistribution ( cfg.getDistribution (), info );

            for ( final ArtifactInformation art : context.getArtifacts () )
            {
                final String arch = art.getMetaData ().get ( DebianAspectInformation.KEY_ARCH );
                final String packageName = art.getMetaData ().get ( DebianAspectInformation.KEY_PACKAGE );
                final String version = art.getMetaData ().get ( DebianAspectInformation.KEY_VERSION );
                final String controlJson = art.getMetaData ().get ( DebianAspectInformation.KEY_CONTROL_JSON );

                final String md5 = art.getMetaData ().get ( new MetaKey ( "hasher", "md5" ) );
                final String sha1 = art.getMetaData ().get ( new MetaKey ( "hasher", "sha1" ) );
                final String sha256 = art.getMetaData ().get ( new MetaKey ( "hasher", "sha256" ) );

                final Map<String, String> checksums = new HashMap<> ( 3 );
                checksums.put ( "MD5sum", md5 );
                checksums.put ( "SHA1", sha1 );
                checksums.put ( "SHA256", sha256 );

                if ( arch == null || version == null || packageName == null || controlJson == null )
                {
                    continue;
                }

                final ControlInformation control = gson.fromJson ( controlJson, ControlInformation.class );

                final PackageInformation packageInfo = new PackageInformation ( makePoolName ( art, packageName, version, arch ), art.getSize (), control.getValues (), checksums );

                // TODO: implement a component selection mechanism

                repo.addPackage ( cfg.getDistribution (), cfg.getDefaultComponent (), arch, packageInfo, ( severity, msg ) -> {
                    context.validationMessage ( severity, msg, Collections.singleton ( art.getId () ) );
                } );
            }

            repo.spoolOut ( ( name, mimeType, stream ) -> {
                context.createCacheEntry ( name, name, mimeType, stream );
            } );
        }
        finally
        {
            if ( serviceRef != null )
            {
                this.context.ungetService ( serviceRef );
            }
        }

        return result;
    }

    private String makePoolName ( final ArtifactInformation art, final String packageName, final String version, final String arch )
    {
        return String.format ( "pool/%s/%s_%s_%s.deb", art.getId (), packageName, version, arch );
    }
}
