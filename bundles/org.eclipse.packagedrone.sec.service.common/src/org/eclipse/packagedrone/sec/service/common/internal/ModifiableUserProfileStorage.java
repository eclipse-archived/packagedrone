/*******************************************************************************
 * Copyright (c) 2016 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.sec.service.common.internal;

import java.security.Principal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.packagedrone.repo.utils.Tokens;
import org.eclipse.packagedrone.sec.service.AccessToken;

public class ModifiableUserProfileStorage implements UserProfileStorage
{
    public final class AccessTokenPrincipal implements Principal
    {
        private final AccessToken accessToken;

        public AccessTokenPrincipal ( final AccessToken accessToken )
        {
            this.accessToken = accessToken;
        }

        @Override
        public String getName ()
        {
            return this.accessToken.getId ();
        }
    }

    /**
     * Map ID to information
     */
    private final Map<String, AccessToken> tokensById;

    /**
     * Map token to information
     */
    private final Map<String, AccessToken> tokensByToken;

    private final List<AccessToken> tokenList;

    public ModifiableUserProfileStorage ()
    {
        this ( (List<AccessToken>)null );
    }

    public ModifiableUserProfileStorage ( final List<AccessToken> tokens )
    {
        if ( tokens != null )
        {
            this.tokensById = new HashMap<> ( tokens.size () );
            this.tokensByToken = new HashMap<> ( tokens.size () );
            this.tokenList = new ArrayList<> ( tokens );
            for ( final AccessToken token : tokens )
            {
                this.tokensById.put ( token.getId (), token );
                this.tokensByToken.put ( token.getToken (), token );
            }
        }
        else
        {
            this.tokensById = new HashMap<> ();
            this.tokensByToken = new HashMap<> ();
            this.tokenList = new ArrayList<> ();
        }
    }

    public ModifiableUserProfileStorage ( final ModifiableUserProfileStorage other )
    {
        this.tokensById = new HashMap<> ( other.tokensById );
        this.tokensByToken = new HashMap<> ( other.tokensByToken );
        this.tokenList = new ArrayList<> ( other.tokenList );
    }

    public AccessToken createAccessToken ( final String description )
    {
        /*
         * The token is a combination of 64 random characters and the timestamp.
         * The timestamp is not for security reasons, but to improve the uniqueness of the access token.
         */

        final String accessToken = Tokens.createToken ( 32 ) + Long.toHexString ( System.currentTimeMillis () );

        final AccessToken token = new AccessToken ( UUID.randomUUID ().toString (), accessToken, description, Instant.now () );

        this.tokensById.put ( token.getId (), token );
        this.tokensByToken.put ( token.getToken (), token );
        this.tokenList.add ( token );

        Collections.sort ( this.tokenList, Comparator.comparing ( AccessToken::getId ) );

        return token;
    }

    public void deleteAccessToken ( final String id )
    {
        final AccessToken accessToken = this.tokensById.remove ( id );
        if ( accessToken != null )
        {
            this.tokensByToken.remove ( accessToken.getToken () );
            this.tokenList.remove ( accessToken );
        }
    }

    @Override
    public Optional<AccessToken> getToken ( final String id )
    {
        return Optional.ofNullable ( this.tokensById.get ( id ) );
    }

    @Override
    public Optional<Principal> getPrincipalFromAccessToken ( final String accessToken )
    {
        return Optional.ofNullable ( this.tokensByToken.get ( accessToken ) ).map ( AccessTokenPrincipal::new );
    }

    @Override
    public List<AccessToken> list ()
    {
        return Collections.unmodifiableList ( this.tokenList );
    }

    public void editAccessToken ( final String id, final String description )
    {
        final AccessToken token = this.tokensById.get ( id );
        if ( token == null )
        {
            throw new NoSuchElementException ();
        }

        final AccessToken newToken = new AccessToken ( token.getId (), token.getToken (), description, token.getCreationTimestamp () );

        this.tokensById.put ( token.getId (), newToken );
        this.tokensByToken.put ( token.getToken (), newToken );

        // replace token - no need to re-sort since the ID did not change

        final ListIterator<AccessToken> i = this.tokenList.listIterator ();
        while ( i.hasNext () )
        {
            if ( i.next () == token )
            {
                i.set ( newToken );
            }
        }
    }
}
