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
package org.eclipse.packagedrone.repo.api.internal.upload;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.api.transfer.TransferArchiveReader;
import org.eclipse.packagedrone.repo.api.upload.RejectedArtifact;
import org.eclipse.packagedrone.repo.api.upload.UploadError;
import org.eclipse.packagedrone.repo.api.upload.UploadResult;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.eclipse.packagedrone.repo.channel.ChannelNotFoundException;
import org.eclipse.packagedrone.repo.channel.ChannelService;
import org.eclipse.packagedrone.repo.channel.ChannelService.By;
import org.eclipse.packagedrone.repo.channel.ChannelService.ChannelOperation;
import org.eclipse.packagedrone.repo.channel.ModifiableChannel;
import org.eclipse.packagedrone.repo.channel.servlet.AbstractChannelServiceServlet;
import org.eclipse.packagedrone.utils.PathInformation;
import org.eclipse.scada.utils.ExceptionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class UploadServletV3 extends AbstractChannelServiceServlet
{
    private final static Logger logger = LoggerFactory.getLogger ( UploadServletV3.class );

    private static final long serialVersionUID = 1L;

    public static final String BASE_PATH = "/api/v3/upload";

    protected static class RequestException extends RuntimeException
    {
        private static final long serialVersionUID = 1L;

        private final int statusCode;

        public RequestException ( final int statusCode, final String message )
        {
            super ( message );
            this.statusCode = statusCode;
        }

        public RequestException ( final String message )
        {
            this ( HttpServletResponse.SC_BAD_REQUEST, message );
        }

        public int getStatusCode ()
        {
            return this.statusCode;
        }
    }

    private Gson createGson ()
    {
        return new GsonBuilder ().create ();
    }

    @Override
    protected void doGet ( final HttpServletRequest request, final HttpServletResponse response ) throws ServletException, IOException
    {
        processError ( response, new RequestException ( HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Use POST or PUT requests to upload your content. See the documentation for more information." ) );
    }

    @Override
    protected void doPut ( final HttpServletRequest request, final HttpServletResponse response ) throws ServletException, IOException
    {
        doStore ( request, response );
    }

    @Override
    protected void doPost ( final HttpServletRequest request, final HttpServletResponse response ) throws ServletException, IOException
    {
        doStore ( request, response );
    }

    private void doStore ( final HttpServletRequest request, final HttpServletResponse response ) throws ServletException, IOException
    {
        try
        {
            final Object result = processStore ( request, response );
            response.setContentType ( MediaType.APPLICATION_JSON );
            createGson ().toJson ( result, response.getWriter () );
        }
        catch ( final RequestException e )
        {
            processError ( response, e );
        }
    }

    private void processError ( final HttpServletResponse response, final RequestException e ) throws IOException
    {
        response.setContentType ( MediaType.APPLICATION_JSON );
        createGson ().toJson ( new UploadError ( e.getMessage () ), response.getWriter () );
    }

    private Object processStore ( final HttpServletRequest request, final HttpServletResponse response )
    {
        final PathInformation path = new PathInformation ( request.getPathInfo () );

        final String type = path.nextOrThrow ( () -> new RequestException ( "Invalid request path for upload. Missing upload type." ) ).toLowerCase ();
        final String target = path.nextOrThrow ( () -> new RequestException ( "Invalid request path for upload. Missing target." ) ).toLowerCase ();

        String channelId;
        String parentId;

        switch ( target )
        {
            case "channel":
                channelId = path.nextOrThrow ( () -> new RequestException ( "Missing channel ID in request path." ) );
                parentId = null;
                break;
            case "artifact":
                channelId = path.nextOrThrow ( () -> new RequestException ( "Missing channel ID in request path." ) );
                parentId = path.nextOrThrow ( () -> new RequestException ( "Missing parent ID in request path." ) );
                break;
            default:
                throw new RequestException ( String.format ( "Unknown upload target type: '%s'", target ) );
        }

        try
        {
            switch ( type )
            {
                case "plain":
                    try
                    {
                        final String name = path.getRemainder ();
                        return processPlain ( channelId, parentId, name, request );
                    }
                    catch ( final NoSuchElementException e )
                    {
                        throw new RequestException ( "Request path is missing artifact name." );
                    }
                case "archive":
                    if ( path.hasNext () )
                    {
                        throw new RequestException ( "Illegal additional path components found." );
                    }
                    return processArchive ( channelId, parentId, request );
                default:
                    throw new RequestException ( String.format ( "Invalid upload type: '%s'", type ) );
            }
        }
        catch ( final IOException e )
        {
            logger.warn ( "Failed to perform upload", e );
            throw new RequestException ( HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "I/O error while uploading" );
        }
    }

    private Object processChannel ( final String channelId, final HttpServletRequest request, final ChannelOperation<Object, ModifiableChannel> operation )
    {
        final By by = By.nameOrId ( channelId );

        if ( !isAuthenticated ( by, request ) )
        {
            logger.warn ( "Request is not authenticated" );
            throw channelNotFound ( channelId );
        }

        final ChannelService service = getService ( request );

        try
        {
            return service.accessCall ( by, ModifiableChannel.class, operation );
        }
        catch ( final ChannelNotFoundException e )
        {
            throw channelNotFound ( channelId );
        }
        catch ( final Exception e )
        {
            throw new RequestException ( HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ExceptionHelper.getMessage ( e ) );
        }
    }

    private RequestException channelNotFound ( final String id )
    {
        return new RequestException ( HttpServletResponse.SC_NOT_FOUND, String.format ( "Channel '%s' could not be found", id ) );
    }

    private Object processPlain ( final String channelId, final String parentId, final String artifactName, final HttpServletRequest request ) throws IOException
    {
        // get the input stream first and claim it

        final InputStream stream = request.getInputStream ();

        // now process the meta data from the query string

        final Map<MetaKey, String> metadata;

        try
        {
            metadata = UploadServletV2.makeMetaData ( request );
        }
        catch ( final IllegalArgumentException e )
        {
            throw new RequestException ( e.getMessage () );
        }

        return processChannel ( channelId, request, channel -> {
            final ArtifactInformation result = channel.getContext ().createArtifact ( parentId, stream, artifactName, metadata );

            final UploadResult uploadResult = new UploadResult ();

            uploadResult.setChannelId ( channel.getId ().getId () );

            addResult ( uploadResult, artifactName, result );

            return uploadResult;
        } );
    }

    private Object processArchive ( final String channelId, final String rootParentId, final HttpServletRequest request )
    {
        return processChannel ( channelId, request, channel -> {

            final UploadResult uploadResult = new UploadResult ();
            uploadResult.setChannelId ( channel.getId ().getId () );

            @SuppressWarnings ( "resource" ) // no need to close
            final TransferArchiveReader reader = new TransferArchiveReader ( request.getInputStream () );

            final Set<String> ignored = new HashSet<> ();

            reader.process ( ( parentId, artifactName, metadata, stream ) -> {

                String id;

                if ( ignored.contains ( parentId ) )
                {
                    // parent got ignored, ignore children as well

                    id = UUID.randomUUID ().toString ();
                    ignored.add ( id );

                    final RejectedArtifact art = new RejectedArtifact ();
                    art.setName ( artifactName );
                    art.setReason ( "Parent artifact was rejected" );
                    uploadResult.getRejectedArtifacts ().add ( art );
                }
                else
                {
                    // try add

                    final String effectiveParentId = parentId == null ? rootParentId : parentId;

                    final ArtifactInformation result = channel.getContext ().createArtifact ( effectiveParentId, stream, artifactName, metadata );
                    if ( result == null )
                    {
                        // failed to add
                        id = UUID.randomUUID ().toString ();
                        ignored.add ( id );
                    }
                    else
                    {
                        // successfully added
                        id = result.getId ();
                    }
                    addResult ( uploadResult, artifactName, result );
                }
                return id;
            } );

            return uploadResult;
        } );
    }

    private void addResult ( final UploadResult result, final String name, final ArtifactInformation artifact )
    {
        if ( artifact != null )
        {
            final org.eclipse.packagedrone.repo.api.upload.ArtifactInformation art = new org.eclipse.packagedrone.repo.api.upload.ArtifactInformation ();
            art.setId ( artifact.getId () );
            art.setParentId ( artifact.getParentId () );
            art.setName ( artifact.getName () );
            art.setSize ( artifact.getSize () );
            art.setErrors ( artifact.getValidationErrorCount () );
            art.setWarnings ( artifact.getValidationWarningCount () );
            result.getCreatedArtifacts ().add ( art );
        }
        else
        {
            final RejectedArtifact art = new RejectedArtifact ();
            art.setName ( name );
            art.setReason ( "Artifact was rejected by validation" );
            result.getRejectedArtifacts ().add ( art );
        }
    }
}
