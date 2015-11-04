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
package org.eclipse.packagedrone.sec.web.captcha.internal;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.manage.core.CoreService;
import org.eclipse.packagedrone.sec.web.captcha.CaptchaResult;
import org.eclipse.packagedrone.sec.web.captcha.CaptchaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

@SuppressWarnings ( "restriction" )
public class RecaptchaService implements CaptchaService
{
    private final static Logger logger = LoggerFactory.getLogger ( RecaptchaService.class );

    private static final URI SITE_VERIFY_URI = URI.create ( System.getProperty ( "drone.recaptche.siteVerifyUri", "https://www.google.com/recaptcha/api/siteverify" ) );

    public static class Response
    {
        private boolean success;

        @SerializedName ( "error-codes" )
        private Set<String> errorCodes = new HashSet<> ();

        public void setSuccess ( final boolean success )
        {
            this.success = success;
        }

        public boolean isSuccess ()
        {
            return this.success;
        }

        public void setErrorCodes ( final Set<String> errorCodes )
        {
            this.errorCodes = errorCodes;
        }

        public Set<String> getErrorCodes ()
        {
            return this.errorCodes;
        }

    }

    private CoreService coreService;

    private CloseableHttpClient client;

    public void setCoreService ( final CoreService coreService )
    {
        this.coreService = coreService;
    }

    public void start ()
    {
        this.client = HttpClients.createDefault ();
    }

    public void stop () throws IOException
    {
        this.client.close ();
    }

    private String getRecaptcheSecretKey ()
    {
        final CoreService service = this.coreService;
        if ( service == null )
        {
            return null;
        }

        return service.getCoreProperty ( new MetaKey ( "recaptcha", "recaptcha-secret-key" ) );
    }

    @Override
    public CaptchaResult checkCaptcha ( final HttpServletRequest request )
    {
        final String key = getRecaptcheSecretKey ();
        if ( key == null )
        {
            return CaptchaResult.OK;
        }

        final String value = request.getParameter ( "g-recaptcha-response" );
        if ( value == null || value.isEmpty () )
        {
            return CaptchaResult.errorResult ( "No captcha response" );
        }

        final HttpPost post = new HttpPost ();
        post.setURI ( SITE_VERIFY_URI );

        try
        {

            final List<NameValuePair> params = new ArrayList<NameValuePair> ( 2 );
            params.add ( new BasicNameValuePair ( "secret", key ) );
            params.add ( new BasicNameValuePair ( "response", value ) );
            post.setEntity ( new UrlEncodedFormEntity ( params, "UTF-8" ) );

            try ( final CloseableHttpResponse result = this.client.execute ( post ) )
            {
                final HttpEntity ent = result.getEntity ();
                if ( ent == null )
                {
                    return CaptchaResult.errorResult ( "No response from captcha service" );
                }

                try ( Reader r = new InputStreamReader ( ent.getContent (), StandardCharsets.UTF_8 ) )
                {
                    final String str = CharStreams.toString ( r );

                    final Gson g = makeGson ();
                    final Response response = g.fromJson ( str, Response.class );

                    if ( response.isSuccess () )
                    {
                        return CaptchaResult.OK;
                    }
                    else
                    {
                        return CaptchaResult.errorResult ( response.getErrorCodes () );
                    }
                }
            }
        }
        catch ( final Exception e )
        {
            logger.warn ( "Failed to check captcha", e );
            return CaptchaResult.exceptionResult ( e );
        }
    }

    private Gson makeGson ()
    {
        final GsonBuilder builder = new GsonBuilder ();
        return builder.create ();
    }

}
