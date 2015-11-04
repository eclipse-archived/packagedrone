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
package org.eclipse.packagedrone.sec.service.admin;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.packagedrone.sec.UserInformation;
import org.eclipse.packagedrone.sec.service.LoginException;
import org.eclipse.packagedrone.sec.service.UserService;
import org.eclipse.packagedrone.sec.service.common.Users;
import org.eclipse.scada.utils.ExceptionHelper;

public class GodModeService implements UserService
{
    private static final String PROP_BASE = "package.drone";

    private static final String PROP_NOT_POSIX = PROP_BASE + ".admin.announce.file.notPosix";

    private final String adminSalt;

    private final String adminTokenHash;

    private static final String NAME = System.getProperty ( PROP_BASE + ".admin.user", "admin" );

    private static final Set<String> ROLES;

    private static final boolean ANNOUNCE_CONSOLE = !Boolean.getBoolean ( PROP_BASE + ".admin.disableAnnounce.console" );

    private static final File ANNOUNCE_FILE;

    private static final boolean ANNOUNCE_FILE_POSIX = !Boolean.getBoolean ( PROP_NOT_POSIX );

    private static final boolean ENABLED = !Boolean.getBoolean ( PROP_BASE + ".admin.disabled" );

    private static final String EXTERNAL_ADMIN_TOKEN_HASH;

    private static final String EXTERNAL_ADMIN_TOKEN_SALT;

    static
    {
        final String externalAdminToken = System.getProperty ( PROP_BASE + ".admin.token", System.getenv ( "PACKAGE_DRONE_ADMIN_TOKEN" ) );
        if ( externalAdminToken != null && !externalAdminToken.isEmpty () )
        {
            EXTERNAL_ADMIN_TOKEN_SALT = Users.createToken ( 32 );
            EXTERNAL_ADMIN_TOKEN_HASH = Users.hashIt ( EXTERNAL_ADMIN_TOKEN_SALT, externalAdminToken );
        }
        else
        {
            EXTERNAL_ADMIN_TOKEN_HASH = EXTERNAL_ADMIN_TOKEN_SALT = null;
        }
    }

    static
    {
        // roles

        ROLES = Collections.unmodifiableSet ( new HashSet<> ( Arrays.asList ( System.getProperty ( PROP_BASE + ".admin.roles", "ADMIN, USER" ).split ( "\\s*,\\s*" ) ) ) );

        // announce file

        final String file = System.getProperty ( PROP_BASE + ".admin.announce.file" );
        final String userDir = System.getProperty ( "user.home" );

        if ( file != null )
        {
            ANNOUNCE_FILE = new File ( file );
        }
        else if ( userDir != null )
        {
            ANNOUNCE_FILE = new File ( new File ( userDir ), ".drone-admin-token" );
        }
        else
        {
            ANNOUNCE_FILE = null;
        }
    }

    public GodModeService ()
    {
        final String adminToken = Users.createToken ( 32 );
        this.adminSalt = Users.createToken ( 32 );
        this.adminTokenHash = Users.hashIt ( this.adminSalt, adminToken );

        if ( ENABLED )
        {
            announce ( adminToken );
        }
    }

    @Override
    public UserInformation checkCredentials ( final String username, final String credentials, final boolean rememberMe ) throws LoginException
    {
        if ( !ENABLED )
        {
            return null;
        }

        if ( !NAME.equals ( username ) )
        {
            return null;
        }

        final String hashedPassword = Users.hashIt ( this.adminSalt, credentials );

        // check generated token
        if ( hashedPassword.equals ( this.adminTokenHash ) )
        {
            return getUserInformation ();
        }

        // check external token
        if ( EXTERNAL_ADMIN_TOKEN_HASH != null && Users.hashIt ( EXTERNAL_ADMIN_TOKEN_SALT, credentials ).equals ( EXTERNAL_ADMIN_TOKEN_HASH ) )
        {
            return getUserInformation ();
        }

        return null;
    }

    private UserInformation getUserInformation ()
    {
        return new UserInformation ( NAME, ROLES );
    }

    @Override
    public UserInformation refresh ( final UserInformation user )
    {
        return null;
    }

    private void announce ( final String adminToken )
    {
        if ( ANNOUNCE_CONSOLE )
        {
            System.out.println ( "=================== Admin>> ===================" );
            System.out.println ( "     User: " + NAME );
            System.out.println ( " Password: " + adminToken );
            System.out.println ( "=================== <<Admin ===================" );
        }
        if ( ANNOUNCE_FILE != null )
        {
            writeAnnounceFile ( adminToken );
        }
    }

    /**
     * Write out the announce file <br>
     * Writing a file like this seems a little bit strange from a Java
     * perspective. However, there seems to be no other way to create a file
     * with a provided set of initial file attributes other than
     * {@link Files#newByteChannel(java.nio.file.Path, Set, FileAttribute...)}.
     * All other methods don't access file attributes.
     *
     * @param adminToken
     *            The admin token to write out
     */
    protected void writeAnnounceFile ( final String adminToken )
    {
        final Path path = ANNOUNCE_FILE.toPath ();

        /*
         * Try to delete the file first. We don't care about the result
         * since the next operation will try to create a new file anyway.
         *
         * However by deleting it first, we try to ensure that the initial
         * file permissions are set. If the file exists these would not be
         * changed.
         */

        ANNOUNCE_FILE.delete ();

        // posix

        final FileAttribute<?>[] attrs;
        if ( ANNOUNCE_FILE_POSIX )
        {
            final Set<PosixFilePermission> perms = EnumSet.of ( PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE );
            attrs = new FileAttribute<?>[] { PosixFilePermissions.asFileAttribute ( perms ) };
        }
        else
        {
            attrs = new FileAttribute<?>[0];
        }

        final Set<? extends OpenOption> options = EnumSet.of ( StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING );

        try ( SeekableByteChannel sbc = Files.newByteChannel ( path, options, attrs ) )
        {
            final StringWriter sw = new StringWriter ();
            final PrintWriter writer = new PrintWriter ( sw );
            writer.format ( "user=%s%n", NAME );
            writer.format ( "password=%s%n", adminToken );
            writer.close ();

            final ByteBuffer data = ByteBuffer.wrap ( sw.toString ().getBytes ( StandardCharsets.UTF_8 ) );
            while ( data.hasRemaining () )
            {
                sbc.write ( data );
            }
        }
        catch ( final UnsupportedOperationException e )
        {
            System.err.format ( "WARNING: Failed to write out announce file with secured posix permissions. If you are on a non-posix platform (e.g. Windows), you might need to set the system property '%s' to 'true'%n", PROP_NOT_POSIX );
        }
        catch ( final IOException e )
        {
            System.err.println ( "WARNING: Unable to write announce file: " + ExceptionHelper.getMessage ( e ) );
        }
    }

    @Override
    public boolean hasUserBase ()
    {
        // we don't count as user base
        return false;
    }
}
