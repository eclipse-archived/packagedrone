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
package org.eclipse.packagedrone.mail.java;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.eclipse.packagedrone.mail.MailService;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.CharStreams;

public class DefaultMailService implements MailService
{
    private final static Logger logger = LoggerFactory.getLogger ( DefaultMailService.class );

    public static final String PROPERTY_PREFIX = "properties.";

    public static final String SERVICE_PID = "org.eclipse.packagedrone.mail.default";

    private Session session;

    private Dictionary<String, Object> config;

    public DefaultMailService ()
    {
    }

    public void updated ( final ComponentContext context )
    {
        stop ();
        start ( context );
    }

    public void start ( final ComponentContext context )
    {
        this.config = context.getProperties ();

        final String username = getString ( "username" );
        final String password = getString ( "password" );

        final Properties properties = new Properties ();

        final Enumeration<String> keys = this.config.keys ();
        while ( keys.hasMoreElements () )
        {
            final String key = keys.nextElement ();
            logger.debug ( "Checking key: {}", key );
            if ( key.startsWith ( PROPERTY_PREFIX ) )
            {
                final Object val = this.config.get ( key );
                if ( val == null )
                {
                    continue;
                }
                final String mkey = key.substring ( PROPERTY_PREFIX.length () );
                logger.info ( "Property - {} = {}", mkey, val );
                properties.put ( mkey, val );
            }
        }

        Authenticator auth = null;
        if ( username != null && password != null )
        {
            auth = new Authenticator () {
                @Override
                protected PasswordAuthentication getPasswordAuthentication ()
                {
                    return new PasswordAuthentication ( username, password );
                }
            };
            properties.put ( "mail.smtp.auth", "true" );
        }

        this.session = Session.getInstance ( properties, auth );
    }

    private String getString ( final String key )
    {
        final Object val = this.config.get ( key );
        if ( val != null )
        {
            return val.toString ();
        }
        return null;
    }

    public void stop ()
    {
        this.session = null;
    }

    @Override
    public void sendMessage ( final String to, final String subject, final Readable readable ) throws Exception
    {
        // create message

        final Message message = createMessage ( to, subject );

        // set text

        message.setText ( CharStreams.toString ( readable ) );

        // send message

        sendMessage ( message );
    }

    @Override
    public void sendMessage ( final String to, final String subject, final String text, final String html ) throws Exception
    {
        // create message

        final Message message = createMessage ( to, subject );

        if ( html != null && !html.isEmpty () )
        {
            // create multipart

            final Multipart parts = new MimeMultipart ( "alternative" );

            // set text

            final MimeBodyPart textPart = new MimeBodyPart ();
            textPart.setText ( text, "UTF-8" );
            parts.addBodyPart ( textPart );

            // set HTML, optionally

            final MimeBodyPart htmlPart = new MimeBodyPart ();
            htmlPart.setContent ( html, "text/html; charset=utf-8" );
            parts.addBodyPart ( htmlPart );

            // set parts

            message.setContent ( parts );
        }
        else
        {
            // plain text
            message.setText ( text );
        }

        // send message

        sendMessage ( message );
    }

    private void sendMessage ( final Message message ) throws MessagingException, NoSuchProviderException
    {
        final ClassLoader oldClassLoader = Thread.currentThread ().getContextClassLoader ();
        Thread.currentThread ().setContextClassLoader ( getClass ().getClassLoader () );

        try
        {

            // commit

            message.saveChanges ();

            // connect

            final Transport transport = this.session.getTransport ();
            transport.connect ();

            // send

            try
            {
                transport.sendMessage ( message, message.getAllRecipients () );
            }
            finally
            {
                // close

                transport.close ();
            }
        }
        finally
        {
            Thread.currentThread ().setContextClassLoader ( oldClassLoader );
        }
    }

    private Message createMessage ( final String to, final String subject ) throws MessagingException, AddressException
    {
        final MimeMessage message = new MimeMessage ( this.session );

        final String from = getString ( "from" );
        if ( from != null )
        {
            message.setFrom ( new InternetAddress ( from ) );
        }
        else
        {
            message.setFrom ();
        }

        // recipient

        final InternetAddress recipient = new InternetAddress ();
        recipient.setAddress ( to );
        message.setRecipient ( javax.mail.Message.RecipientType.TO, recipient );

        // mail

        final String prefix = getString ( "prefix" );
        if ( prefix != null )
        {
            message.setSubject ( prefix + " " + subject );
        }
        else
        {
            message.setSubject ( subject );
        }

        message.setHeader ( "Return-Path", "<>" );

        return message;
    }
}
