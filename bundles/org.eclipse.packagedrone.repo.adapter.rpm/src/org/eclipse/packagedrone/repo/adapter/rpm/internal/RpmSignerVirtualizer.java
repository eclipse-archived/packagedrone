/*******************************************************************************
 * Copyright (c) 2019 Trident Systems, Inc.
 * This software was developed with U.S government funding in support of the above
 * contract.  Trident grants unlimited rights to modify, distribute and incorporate
 * our contributions to Eclipse Package Drone bound by the overall restrictions from
 * the parent Eclipse Public License v1.0 available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Walker Funk - Trident Systems Inc. - initial implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.adapter.rpm.internal;

import java.io.DataInputStream;
import java.io.BufferedInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.utils.HashHelper;
import org.eclipse.packagedrone.repo.signing.SigningService;
import org.eclipse.packagedrone.repo.aspect.virtual.Virtualizer;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.eclipse.packagedrone.repo.adapter.rpm.Constants;
import org.eclipse.packagedrone.repo.adapter.rpm.yum.internal.YumChannelAggregator;
import org.eclipse.packagedrone.utils.Exceptions;
import org.eclipse.packagedrone.utils.rpm.parse.RpmParserStream;

import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.google.common.io.ByteStreams;
import com.google.common.hash.Hashing;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpmSignerVirtualizer implements Virtualizer
{
    private final static Logger logger = LoggerFactory.getLogger ( RpmSignerVirtualizer.class );

    private static final MetaKey KEY_SIGNING_ID = new MetaKey ( "yum", "signingServiceId" );

    private final BundleContext bundleContext = FrameworkUtil.getBundle ( YumChannelAggregator.class ).getBundleContext ();

    @Override
    public void virtualize ( final Context context )
    {
        Exceptions.wrapException ( () -> processVirtualize ( context ) );
    }

    private void processVirtualize ( final Context context ) throws Exception
    {
        final Path path = context.getFile ();
        final ArtifactInformation art = context.getArtifactInformation ();
        final String name = art.getName ();
        Map<MetaKey, String> metaData = new HashMap<> ( art.getMetaData () );

        if ( metaData.containsKey ( Constants.KEY_RSA ) )
        {
            return;
        }

        final String signingServiceId = context.getProvidedChannelMetaData ().get ( KEY_SIGNING_ID );
        ServiceReference<SigningService> ssref = null;
        SigningService signingService = null;

        if ( signingServiceId != null && !signingServiceId.isEmpty () )
        {
            final Collection<ServiceReference<SigningService>> services = bundleContext.getServiceReferences ( SigningService.class, String.format( "(%s=%s)", org.osgi.framework.Constants.SERVICE_PID, signingServiceId ) );

            if ( services == null || services.isEmpty () )
            {
                throw new IllegalStateException ( String.format ( "Unable to find configured signing service: %s", signingServiceId ) );
            }

            ssref = services.iterator ().next ();
            signingService = bundleContext.getService ( ssref );

            try ( RpmParserStream preIn = new RpmParserStream ( new BufferedInputStream ( Files.newInputStream ( path, StandardOpenOption.READ ) ) ); )
            {
                signingService.signRpm ( path, preIn );

                Map<String, HashFunction> functions = new HashMap<> ();

                functions.put ( "md5", Hashing.md5 () );
                functions.put ( "sha1", Hashing.sha1 () );
                functions.put ( "sha256", Hashing.sha256 () );
                functions.put ( "sha512", Hashing.sha512 () );

                final Map<String, HashCode> result = HashHelper.createChecksums ( path, functions );
                for ( final Map.Entry<String, HashCode> entry : result.entrySet () )
                {
                    metaData.replace ( new MetaKey ("hasher", entry.getKey () ), entry.getValue ().toString () );
                }

                DataInputStream postIn = new DataInputStream ( Files.newInputStream( path, StandardOpenOption.READ ) );
                context.createVirtualArtifact ( name, out -> ByteStreams.copy ( postIn, out ), metaData );
            }
            catch ( final Exception e )
            {
                logger.debug ( "Failed to sign RPM", e );
            }
        }
    }
}
