/*******************************************************************************
 * Copyright (c) 2015, 2016 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *     Walker Funk - Trident Systems Inc. - limit repo to only signed rpms when signing enabled
 *******************************************************************************/
package org.eclipse.packagedrone.repo.adapter.rpm.yum.internal;

import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.adapter.rpm.Constants;
import org.eclipse.packagedrone.repo.adapter.rpm.RpmInformationsJson;
import org.eclipse.packagedrone.repo.aspect.aggregate.AggregationContext;
import org.eclipse.packagedrone.repo.aspect.aggregate.ChannelAggregator;
import org.eclipse.packagedrone.repo.aspect.common.spool.ChannelCacheTarget;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.eclipse.packagedrone.repo.signing.SigningService;
import org.eclipse.packagedrone.utils.rpm.HashAlgorithm;
import org.eclipse.packagedrone.utils.rpm.info.RpmInformation;
import org.eclipse.packagedrone.utils.rpm.yum.RepositoryCreator;
import org.eclipse.packagedrone.utils.rpm.yum.RepositoryCreator.DefaultXmlContext;
import org.eclipse.packagedrone.utils.xml.XmlToolsFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

public class YumChannelAggregator implements ChannelAggregator
{
    private static final MetaKey KEY_SIGNING_ID = new MetaKey ( "yum", "signingServiceId" );

    private static final MetaKey KEY_SHA1 = new MetaKey ( "hasher", "sha1" );

    private final BundleContext context;

    private final XmlToolsFactory xml;

    private boolean isSigning = false;

    public YumChannelAggregator ( final XmlToolsFactory xml )
    {
        this.xml = xml;
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
            this.isSigning = true;
        }

        try
        {
            final DefaultXmlContext xmlCtx = makeXmlContext ();
            final Function<OutputStream, OutputStream> signingStreamCreator = makeSigningStreamCreator ( signingService );

            final RepositoryCreator.Builder builder = new RepositoryCreator.Builder ();
            builder.setTarget ( new ChannelCacheTarget ( context ) );
            builder.setXmlContext ( xmlCtx );
            builder.setSigning ( signingStreamCreator );

            final RepositoryCreator creator = builder.build ();

            final Map<String, String> result = new HashMap<> ();

            creator.process ( repoContext -> {
                for ( final ArtifactInformation art : context.getArtifacts () )
                {
                    if ( ( this.isSigning && art.getMetaData ().containsKey ( Constants.KEY_RSA ) ) || !this.isSigning )
                    {
                        final RpmInformation info = RpmInformationsJson.fromJson(art.getMetaData().get(Constants.KEY_INFO));

                        if (info == null) {
                            continue;
                        }

                        final String sha1 = art.getMetaData().get(KEY_SHA1);
                        final Map<HashAlgorithm, String> checksums = Collections.singletonMap(HashAlgorithm.SHA1, sha1);

                        final String location = String.format("pool/%s/%s", art.getId(), art.getName());
                        final RepositoryCreator.FileInformation file = new RepositoryCreator.FileInformation(art.getCreationInstant(), art.getSize(), location);

                        repoContext.addPackage(file, info, checksums, HashAlgorithm.SHA1);
                    }
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

    private Function<OutputStream, OutputStream> makeSigningStreamCreator ( final SigningService signingService )
    {
        Function<OutputStream, OutputStream> signingStreamCreator = null;
        if ( signingService != null )
        {
            final SigningService signingServiceFinal = signingService;
            signingStreamCreator = output -> signingServiceFinal.signingStream ( output, false );
        }
        return signingStreamCreator;
    }

    private DefaultXmlContext makeXmlContext ()
    {
        final DocumentBuilderFactory dbf = this.xml.newDocumentBuilderFactory ();
        dbf.setNamespaceAware ( true );
        final DefaultXmlContext xmlCtx = new DefaultXmlContext ( dbf, this.xml.newTransformerFactory () );
        return xmlCtx;
    }
}