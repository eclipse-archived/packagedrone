/*******************************************************************************
 * Copyright (c) 2015 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.signing.pgp.internal.managed;

import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.bouncycastle.openpgp.PGPSecretKey;
import org.eclipse.packagedrone.repo.signing.SigningService;
import org.eclipse.packagedrone.repo.signing.pgp.ManagedKey;
import org.eclipse.packagedrone.repo.signing.pgp.ManagedPgpConfiguration;
import org.eclipse.packagedrone.utils.Suppressed;
import org.eclipse.packagedrone.utils.security.pgp.PgpHelper;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;

public class Entry
{
    private final Configuration cfg;

    private final List<ServiceRegistration<?>> regs = new LinkedList<> ();

    private final BundleContext context;

    private ManagedPgpConfigurationImpl configuration;

    public Entry ( final BundleContext context, final Configuration cfg )
    {
        this.context = context;
        this.cfg = cfg;
        this.configuration = new ManagedPgpConfigurationImpl ( this.cfg, null, Collections.emptyList () );
    }

    public ManagedPgpConfiguration getConfiguration ()
    {
        return this.configuration;
    }

    public Configuration getRawConfiguration ()
    {
        return this.cfg;
    }

    public void start ()
    {
        List<ManagedKey> keys = Collections.emptyList ();
        Exception error = null;
        try ( Suppressed<Exception> s = new Suppressed<> ( "Failed to load key", Exception::new ) )
        {
            keys = activate ( s );
        }
        catch ( final Exception e )
        {
            error = e;
        }

        this.configuration = new ManagedPgpConfigurationImpl ( this.cfg, error, Collections.unmodifiableList ( keys ) );
    }

    private List<ManagedKey> activate ( final Suppressed<Exception> sup ) throws Exception
    {
        final List<ManagedKey> keys = new LinkedList<> ();
        try ( final Stream<PGPSecretKey> s = PgpHelper.streamSecretKeys ( PgpHelper.fromString ( this.cfg.getSecretKey () ) ).filter ( PGPSecretKey::isSigningKey ) )
        {
            s.forEach ( key -> sup.run ( () -> processKey ( keys, key ) ) );
        }
        return keys;
    }

    private void processKey ( final List<ManagedKey> keys, final PGPSecretKey key ) throws Exception
    {
        final String keyId = String.format ( "%016X", key.getKeyID () );

        @SuppressWarnings ( "unchecked" )
        final Stream<?> s = StreamSupport.stream ( Spliterators.spliteratorUnknownSize ( key.getUserIDs (), Spliterator.ORDERED ), false );
        final List<String> users = s.map ( Object::toString ).collect ( Collectors.toList () );

        final int bits = key.getPublicKey ().getBitStrength ();

        final ManagedKey mkey = new ManagedKey ( keyId, users, !key.isMasterKey (), bits );
        keys.add ( mkey );

        registerKey ( key, users );
    }

    protected void registerKey ( final PGPSecretKey key, final List<String> users ) throws Exception
    {
        final String keyId = String.format ( "%016X", key.getKeyID () );

        final SigningService service = new ManagedSigningService ( key, this.cfg.getPassphrase () );
        final Dictionary<String, Object> properties = new Hashtable<> ( 1 );
        properties.put ( Constants.SERVICE_PID, "pgp." + keyId );

        final String usersString = users.stream ().collect ( Collectors.joining ( "; " ) );

        if ( !users.isEmpty () )
        {
            properties.put ( Constants.SERVICE_DESCRIPTION, String.format ( "Managed PGP key (%s) %s: %s", keyId, !key.isMasterKey () ? "(sub)" : "", usersString ) );
        }
        else
        {
            properties.put ( Constants.SERVICE_DESCRIPTION, String.format ( "Managed PGP key (%s) %s", keyId, !key.isMasterKey () ? "(sub)" : "" ) );
        }

        this.regs.add ( this.context.registerService ( SigningService.class, service, properties ) );
    }

    public void stop ()
    {
        this.regs.forEach ( ServiceRegistration::unregister );
        this.regs.clear ();
    }
}
