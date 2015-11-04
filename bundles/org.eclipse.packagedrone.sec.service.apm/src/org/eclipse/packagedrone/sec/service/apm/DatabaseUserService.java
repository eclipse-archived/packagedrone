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
package org.eclipse.packagedrone.sec.service.apm;

import static org.eclipse.packagedrone.repo.utils.Tokens.createToken;
import static org.eclipse.packagedrone.sec.service.common.Users.hashIt;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.sec.CreateUser;
import org.eclipse.packagedrone.sec.DatabaseUserInformation;
import org.eclipse.packagedrone.sec.UserDetails;
import org.eclipse.packagedrone.sec.UserInformation;
import org.eclipse.packagedrone.sec.UserStorage;
import org.eclipse.packagedrone.sec.service.LoginException;
import org.eclipse.packagedrone.sec.service.UserService;
import org.eclipse.packagedrone.sec.service.apm.model.UserEntity;
import org.eclipse.packagedrone.sec.service.apm.model.UserModel;
import org.eclipse.packagedrone.sec.service.apm.model.UserStorageModelProvider;
import org.eclipse.packagedrone.sec.service.apm.model.UserWriteModel;
import org.eclipse.packagedrone.sec.service.common.SecurityMailService;
import org.eclipse.packagedrone.sec.service.password.BadPasswordException;
import org.eclipse.packagedrone.sec.service.password.PasswordChecker;
import org.eclipse.packagedrone.storage.apm.StorageManager;
import org.eclipse.packagedrone.storage.apm.StorageRegistration;
import org.eclipse.packagedrone.utils.scheduler.ScheduledTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseUserService implements UserService, UserStorage, ScheduledTask
{
    private static final MetaKey MODEL_KEY = new MetaKey ( "sec", "users" );

    private final static Logger logger = LoggerFactory.getLogger ( DatabaseUserService.class );

    private static final long MIN_EMAIL_DELAY = TimeUnit.MINUTES.toMillis ( 5 );

    private PasswordChecker passwordChecker;

    private SecurityMailService mailService;

    private StorageManager storageManager;

    private StorageRegistration handle;

    public void setSecurityMailService ( final SecurityMailService mailService )
    {
        this.mailService = mailService;
    }

    public void setPasswordChecker ( final PasswordChecker passwordChecker )
    {
        this.passwordChecker = passwordChecker;
    }

    public void setStorageManager ( final StorageManager storageManager )
    {
        this.storageManager = storageManager;
    }

    public void start ()
    {
        this.handle = this.storageManager.registerModel ( 1_000, MODEL_KEY, new UserStorageModelProvider () );
    }

    public void stop ()
    {
        if ( this.handle != null )
        {
            this.handle.unregister ();
            this.handle = null;
        }
    }

    @Override
    public List<DatabaseUserInformation> list ( final int position, final int size )
    {
        return this.storageManager.accessCall ( MODEL_KEY, UserModel.class, users -> {

            final List<DatabaseUserInformation> list = users.listAll ();
            final int end = Math.min ( position + size, list.size () );
            return users.listAll ().subList ( position, end );
        } );
    }

    @Override
    public DatabaseUserInformation createUser ( final CreateUser data, final boolean emailVerified )
    {
        return this.storageManager.modifyCall ( MODEL_KEY, UserWriteModel.class, users -> {

            if ( !emailVerified )
            {
                // check only for external signup
                checkPassword ( data.getPassword () );
            }

            if ( data.getEmail () != null && users.findByEmail ( data.getEmail () ).isPresent () )
            {
                throw new IllegalArgumentException ( String.format ( "A user with the e-mail '%s' already exists.", data.getEmail () ) );
            }

            final Date now = new Date ();

            final UserEntity user = new UserEntity ();

            user.setId ( UUID.randomUUID ().toString () );
            user.setEmail ( data.getEmail () );
            user.setName ( data.getName () );
            user.setRegistrationDate ( now );
            user.setLocked ( false );

            applyPassword ( user, data.getPassword () );

            final String token;
            if ( emailVerified )
            {
                token = null;
                user.setEmailVerified ( true );
            }
            else
            {
                user.setEmailVerified ( false );
                token = applyNewEmailToken ( now, user );
            }

            // update

            if ( token != null )
            {
                this.mailService.sendVerifyEmail ( data.getEmail (), user.getId (), token );
            }

            users.putUser ( user );

            return new DatabaseUserInformation ( user.getId (), null, user.getRoles (), Helper.toDetails ( user ) );
        } );
    }

    @Override
    public DatabaseUserInformation getUserDetails ( final String userId )
    {
        return this.storageManager.accessCall ( MODEL_KEY, UserModel.class, users -> users.getUser ( userId ).orElse ( null ) );
    }

    @Override
    public DatabaseUserInformation updateUser ( final String userId, final UserDetails data )
    {
        return this.storageManager.modifyCall ( MODEL_KEY, UserWriteModel.class, users -> {

            final UserEntity user = users.getCheckedUser ( userId );

            user.setEmail ( data.getEmail () );
            user.setName ( data.getName () );
            user.setRoles ( new HashSet<> ( data.getRoles () ) );

            users.putUser ( user );

            return new DatabaseUserInformation ( userId, null, data.getRoles (), Helper.toDetails ( user ) );
        } );
    }

    @Override
    public String verifyEmail ( final String userId, final String token )
    {
        return this.storageManager.modifyCall ( MODEL_KEY, UserWriteModel.class, users -> {

            final UserEntity user = users.getUser ( userId ).orElse ( null );

            if ( user == null )
            {
                return "User not found";
            }

            if ( user.isLocked () )
            {
                return "User is locked";
            }

            if ( user.getEmailToken () == null || user.isEmailVerified () )
            {
                // we are already verified
                return null;
            }

            final String salt = user.getEmailTokenSalt ();
            final String hashedToken = hashIt ( salt, token );

            if ( hashedToken.equals ( user.getEmailToken () ) )
            {
                user.setEmailVerified ( true );
                user.setEmailToken ( null );
                user.setEmailTokenSalt ( null );
                user.setEmailTokenDate ( null );

                users.putUser ( user );
                return null;
            }

            return "It may be that you clicked on a verification link which was either expired or superseeded by another e-mail request.";

        } );
    }

    @Override
    public DatabaseUserInformation getUserDetailsByEmail ( final String email )
    {
        return this.storageManager.accessCall ( MODEL_KEY, UserModel.class, users -> users.findUserByEmail ( email ).orElse ( null ) );
    }

    @Override
    public String reRequestEmail ( final String email )
    {
        return this.storageManager.modifyCall ( MODEL_KEY, UserWriteModel.class, users -> {

            final UserEntity user = users.findByEmail ( email ).orElse ( null );

            if ( user == null )
            {
                return "User not found";
            }

            if ( user.isLocked () )
            {
                return "User is locked";
            }

            if ( user.getEmailToken () == null || user.isEmailVerified () )
            {
                // we are already verified
                return "E-Mail is already verified";
            }

            if ( isTooSoon ( user.getEmailTokenDate () ) )
            {
                return MessageFormat.format ( "An e-mail verification was requested at {0,time}. Please wait until {1,time} before requesting the next one!", user.getEmailTokenDate (), nextMailSlot ( user.getEmailTokenDate () ) );
            }

            final String token = applyNewEmailToken ( new Date (), user );

            users.putUser ( user );

            this.mailService.sendVerifyEmail ( user.getEmail (), user.getId (), token );
            return null;
        } );
    }

    @Override
    public String resetPassword ( final String email )
    {
        return this.storageManager.modifyCall ( MODEL_KEY, UserWriteModel.class, users -> {

            final UserEntity user = users.findByEmail ( email ).orElse ( null );

            if ( user == null )
            {
                return "No account for this e-mail address.";
            }

            if ( !user.isEmailVerified () )
            {
                return "The e-mail address for this account is not verified.";
            }

            if ( isTooSoon ( user.getEmailTokenDate () ) )
            {
                return MessageFormat.format ( "A password reset e-mail was requested at {0,time}. Please wait until {1,time} before requesting the next one!", user.getEmailTokenDate (), nextMailSlot ( user.getEmailTokenDate () ) );
            }

            if ( user.isLocked () )
            {
                // we silently fail, since this would give out information about the user's state
                this.mailService.sendEmail ( user.getEmail (), "Password reset request", "lockedUser", null );
                return null;
            }

            final String resetToken = createToken ( 64 );
            final String resetTokenSalt = createToken ( 32 );

            final String resetTokenHash = hashIt ( resetTokenSalt, resetToken );

            user.setEmailTokenSalt ( resetTokenSalt );
            user.setEmailTokenDate ( new Date () );
            user.setEmailToken ( resetTokenHash );

            users.putUser ( user );

            // we don't touch the password for now, could be anybody

            this.mailService.sendResetEmail ( email, resetToken );

            return null;
        } );
    }

    @Override
    public void changePassword ( final String email, final String token, final String password )
    {
        this.storageManager.modifyRun ( MODEL_KEY, UserWriteModel.class, users -> {

            logger.debug ( "Process password change - email: {}, token: {}, password: {}", email, token, password != null ? "***" : null );

            final UserEntity user = users.findByEmail ( email ).orElse ( null );

            if ( user == null )
            {
                throw new RuntimeException ( "User not found" );
            }

            // validate token

            final String salt = user.getEmailTokenSalt ();

            if ( salt == null )
            {
                throw new RuntimeException ( "No token" );
            }

            final String hashedToken = hashIt ( salt, token );

            if ( !hashedToken.equals ( user.getEmailToken () ) )
            {
                throw new RuntimeException ( "Invalid token" );
            }

            // check for "locked" after the token was validated

            if ( user.isLocked () )
            {
                throw new RuntimeException ( "User is locked" );
            }

            checkPassword ( password );

            applyPassword ( user, password );

            user.setEmailToken ( null );
            user.setEmailTokenDate ( null );
            user.setEmailTokenSalt ( null );

            users.putUser ( user );
        } );
    }

    @Override
    public void updatePassword ( final String userId, final String currentPassword, final String newPassword )
    {
        logger.debug ( "Process password update - userId: {},  currentPassword: {},  newPassword: {}", userId, currentPassword != null ? "***" : null, newPassword != null ? "***" : null );

        this.storageManager.modifyRun ( MODEL_KEY, UserWriteModel.class, users -> {

            final UserEntity user = users.getCheckedUser ( userId );

            if ( user.isLocked () )
            {
                throw new RuntimeException ( "User is locked" );
            }

            final String currentSalt = user.getPasswordSalt ();
            final String currentHash = user.getPasswordHash ();

            checkPassword ( newPassword );

            if ( currentPassword != null && currentSalt != null && currentHash != null )
            {
                final String checkHash = hashIt ( currentSalt, currentPassword );
                if ( !currentHash.equals ( checkHash ) )
                {
                    throw new RuntimeException ( "Current password is incorrect" );
                }
            }

            applyPassword ( user, newPassword );

            users.putUser ( user );
        } );
    }

    protected void setUserLocked ( final UserWriteModel users, final String userId, final boolean value )
    {
        final UserEntity user = users.getCheckedUser ( userId );
        user.setLocked ( value );
        users.putUser ( user );
    }

    @Override
    public void lockUser ( final String userId )
    {
        this.storageManager.modifyRun ( MODEL_KEY, UserWriteModel.class, users -> setUserLocked ( users, userId, true ) );
    }

    @Override
    public void unlockUser ( final String userId )
    {
        this.storageManager.modifyRun ( MODEL_KEY, UserWriteModel.class, users -> setUserLocked ( users, userId, false ) );
    }

    @Override
    public void deleteUser ( final String userId )
    {
        this.storageManager.modifyRun ( MODEL_KEY, UserWriteModel.class, users -> users.removeUser ( userId ) );
    }

    private static class LoginResult
    {
        UserInformation userInformation;

        LoginException exception;
    }

    @Override
    public UserInformation checkCredentials ( final String username, final String credentials, final boolean rememberMe ) throws LoginException
    {
        final LoginResult result = loginAndRememberMe ( username, credentials, rememberMe );

        if ( result == null )
        {
            return null;
        }

        if ( result.exception != null )
        {
            throw result.exception;
        }

        return result.userInformation;
    }

    private LoginResult loginAndRememberMe ( final String username, final String credentials, final boolean rememberMe )
    {
        final LoginResult result = this.storageManager.modifyCall ( MODEL_KEY, UserWriteModel.class, users -> {

            final UserEntity user = users.findByEmail ( username ).orElse ( null );

            if ( user == null )
            {
                // let the next one try
                return null;
            }

            boolean valid = false;

            // check for remember me token

            if ( user.getRememberMeTokenHash () != null && user.getRememberMeTokenSalt () != null )
            {
                final String tokenHash = hashIt ( user.getRememberMeTokenSalt (), credentials );
                if ( tokenHash.equals ( user.getRememberMeTokenHash () ) )
                {
                    valid = true;
                }
            }

            // check for password

            if ( user.getPasswordSalt () != null && user.getPasswordHash () != null )
            {
                final String credHash = hashIt ( user.getPasswordSalt (), credentials );
                if ( credHash.equals ( user.getPasswordHash () ) )
                {
                    valid = true;
                }
            }

            if ( !valid )
            {
                // no valid credentials, let other services try
                return null;
            }

            final LoginResult loginResult = new LoginResult ();

            // only fail _after_ the password has been checked, so we should be sure it is the user

            loginResult.exception = validateUserAfterLogin ( user );

            // handle remember me

            String rememberMeToken;

            if ( rememberMe )
            {
                rememberMeToken = createToken ( 128 );
                final String tokenSalt = createToken ( 32 );

                final String tokenHash = hashIt ( tokenSalt, rememberMeToken );

                user.setRememberMeTokenHash ( tokenHash );
                user.setRememberMeTokenSalt ( tokenSalt );

                users.putUser ( user );
            }
            else
            {
                rememberMeToken = null;
            }

            loginResult.userInformation = new DatabaseUserInformation ( user.getId (), rememberMeToken, user.getRoles (), Helper.toDetails ( user ) );

            // we made it

            return loginResult;
        } );
        return result;
    }

    @Override
    public UserInformation refresh ( final UserInformation user )
    {
        return getUserDetails ( user.getId () );
    }

    @Override
    public boolean hasUserBase ()
    {
        return this.storageManager.accessCall ( MODEL_KEY, UserModel.class, users -> !users.isEmpty () );
    }

    private void checkPassword ( final String password )
    {
        try
        {
            this.passwordChecker.checkPassword ( password );
        }
        catch ( final BadPasswordException e )
        {
            throw new RuntimeException ( e );
        }
    }

    private static Date nextMailSlot ( final Date date )
    {
        return new Date ( date.getTime () + MIN_EMAIL_DELAY );
    }

    private static boolean isTooSoon ( final Date date )
    {
        if ( date == null )
        {
            return false;
        }

        return System.currentTimeMillis () - date.getTime () < MIN_EMAIL_DELAY;
    }

    private void applyPassword ( final UserEntity user, final String password )
    {
        if ( password == null || password.isEmpty () )
        {
            return;
        }

        final String salt = createToken ( 32 );
        final String passwordHash = hashIt ( salt, password );

        user.setPasswordSalt ( salt );
        user.setPasswordHash ( passwordHash );
    }

    protected String applyNewEmailToken ( final Date now, final UserEntity user )
    {
        final String token = createToken ( 32 );

        final String tokenSalt = createToken ( 32 );
        final String tokenHash = hashIt ( tokenSalt, token );

        user.setEmailToken ( tokenHash );
        user.setEmailTokenSalt ( tokenSalt );
        user.setEmailTokenDate ( now );
        return token;
    }

    protected LoginException validateUserAfterLogin ( final UserEntity user )
    {
        if ( user.isLocked () )
        {
            return new LoginException ( "User is locked" );
        }

        if ( !user.isEmailVerified () )
        {
            return new LoginException ( "E-mail not verified" );
        }

        return null;
    }

    @Override
    public void run () throws Exception
    {
        this.storageManager.modifyRun ( MODEL_KEY, UserWriteModel.class, users -> {

            final Date timeout = new Date ( System.currentTimeMillis () - getTimeout () );

            final Collection<UserEntity> updates = new LinkedList<> ();
            final Collection<String> removals = new LinkedList<> ();

            for ( final UserEntity user : users.asCollection () )
            {
                if ( user.getEmailTokenDate () == null || user.getEmailTokenDate ().before ( timeout ) )
                {
                    continue;
                }

                // process timeout

                if ( user.isEmailVerified () )
                {
                    user.setEmailToken ( null );
                    user.setEmailTokenDate ( null );
                    user.setEmailTokenSalt ( null );
                    updates.add ( user );
                }
                else
                {
                    // delete
                    removals.add ( user.getId () );
                }
            }

            updates.forEach ( users::putUser );
            removals.forEach ( users::removeUser );
        } );
    }

    private long getTimeout ()
    {
        return TimeUnit.HOURS.toMillis ( 1 );
    }
}
