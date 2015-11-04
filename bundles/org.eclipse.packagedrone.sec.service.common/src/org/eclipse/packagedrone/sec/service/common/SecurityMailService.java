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
package org.eclipse.packagedrone.sec.service.common;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.packagedrone.mail.MailService;
import org.eclipse.packagedrone.repo.manage.system.SitePrefixService;
import org.eclipse.scada.utils.str.StringReplacer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.html.HtmlEscapers;
import com.google.common.io.CharStreams;

/**
 * A helper service for other security services to send out common mails
 */
public class SecurityMailService
{

    private final static Logger logger = LoggerFactory.getLogger ( SecurityMailService.class );

    private MailService mailService;

    private SitePrefixService sitePrefixService;

    public void setSitePrefixService ( final SitePrefixService sitePrefixService )
    {
        this.sitePrefixService = sitePrefixService;
    }

    public void setMailService ( final MailService mailService )
    {
        this.mailService = mailService;
    }

    public void unsetMailService ( final MailService mailService )
    {
        this.mailService = null;
    }

    public void sendResetEmail ( final String email, final String resetToken )
    {
        String link;
        try
        {
            link = String.format ( "%s/signup/newPassword?email=%s&token=%s", getSitePrefix (), URLEncoder.encode ( email, "UTF-8" ), resetToken );
        }
        catch ( final UnsupportedEncodingException e )
        {
            throw new RuntimeException ( e );
        }

        final Map<String, String> model = new HashMap<> ();
        model.put ( "token", resetToken );
        model.put ( "link", link );
        model.put ( "linkEncoded", HtmlEscapers.htmlEscaper ().escape ( link ) );
        sendEmail ( email, "Password reset request", "passwordReset", model );
    }

    public void sendEmail ( final String email, final String subject, final String resource, final Map<String, ?> model )
    {
        final MailService mailService = this.mailService;

        if ( mailService == null )
        {
            throw new IllegalStateException ( "Failed to send e-mail. Mail service not present!" );
        }

        final URL url = SecurityMailService.class.getResource ( String.format ( "mails/%s.txt", resource ) );
        if ( url == null )
        {
            logger.info ( "Failed to load mail content" );
            throw new IllegalStateException ( String.format ( "Unable to find message content: %s", resource ) );
        }

        final URL urlHtml = SecurityMailService.class.getResource ( String.format ( "mails/%s.html", resource ) );

        final String data = loadAndFill ( resource, model, url );
        final String dataHtml = loadAndFill ( resource, model, urlHtml );

        try
        {
            mailService.sendMessage ( email, subject, data, dataHtml );
        }
        catch ( final Exception e )
        {
            logger.warn ( "Failed to send e-mail to: " + email, e );
        }
    }

    private String loadAndFill ( final String resource, final Map<String, ?> model, final URL url )
    {
        if ( url == null )
        {
            return null;
        }

        String data;
        try ( InputStream is = url.openStream (); Reader r = new InputStreamReader ( is, StandardCharsets.UTF_8 ) )
        {
            data = CharStreams.toString ( r );
        }
        catch ( final Exception e )
        {
            logger.warn ( "Failed to process mail content", e );
            throw new RuntimeException ( "Failed to load mail content: " + resource, e );
        }

        if ( model != null )
        {
            data = StringReplacer.replace ( data, StringReplacer.newExtendedSource ( model ), StringReplacer.DEFAULT_PATTERN, true );
        }
        return data;
    }

    public void sendVerifyEmail ( final String email, final String userId, final String token )
    {
        final String link = String.format ( "%s/signup/verifyEmail?userId=%s&token=%s", getSitePrefix (), userId, token );

        final Map<String, String> model = new HashMap<> ();
        model.put ( "token", token );
        model.put ( "link", link );
        model.put ( "linkEncoded", HtmlEscapers.htmlEscaper ().escape ( link ) );
        sendEmail ( email, "Verify your account", "verify", model );
    }

    protected String getSitePrefix ()
    {
        return this.sitePrefixService.getSitePrefix ();
    }
}
