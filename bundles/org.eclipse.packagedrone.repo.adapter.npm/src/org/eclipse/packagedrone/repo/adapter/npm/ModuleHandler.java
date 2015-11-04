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
package org.eclipse.packagedrone.repo.adapter.npm;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.adapter.npm.aspect.NpmChannelAspectFactory;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.eclipse.packagedrone.repo.channel.ReadableChannel;
import org.eclipse.packagedrone.repo.manage.system.SystemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ModuleHandler
{
    private static final MetaKey KEY_NPM_PACKAGE_JSON = new MetaKey ( NpmChannelAspectFactory.ID, "package.json" );

    private static final MetaKey KEY_SHA1 = new MetaKey ( "hasher", "sha1" );

    private final static Logger logger = LoggerFactory.getLogger ( ModuleHandler.class );

    private final ReadableChannel channel;

    private final String moduleName;

    private final boolean pretty;

    private final SystemService service;

    public ModuleHandler ( final SystemService service, final ReadableChannel channel, final String moduleName, final boolean pretty )
    {
        this.service = service;
        this.channel = channel;
        this.moduleName = moduleName;
        this.pretty = pretty;
    }

    public void process ( final HttpServletResponse response ) throws IOException
    {
        response.setContentType ( "application/json" );
        process ( response.getOutputStream () );
    }

    private static class PackageEntry
    {
        private final JsonElement element;

        private final PackageInfo info;

        private final ArtifactInformation artifact;

        public PackageEntry ( final PackageInfo info, final JsonElement element, final ArtifactInformation artifact )
        {
            this.info = info;
            this.element = element;
            this.artifact = artifact;
        }

        public ArtifactInformation getArtifact ()
        {
            return this.artifact;
        }

        public JsonElement getElement ()
        {
            return this.element;
        }

        public PackageInfo getInfo ()
        {
            return this.info;
        }
    }

    public void process ( final OutputStream stream ) throws IOException
    {
        final String sitePrefix = this.service.getDefaultSitePrefix ();

        final GsonBuilder builder = new GsonBuilder ();
        if ( this.pretty )
        {
            builder.setPrettyPrinting ();
        }

        builder.setDateFormat ( "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" );

        final Gson gson = builder.create ();

        final JsonParser parser = new JsonParser ();

        final TreeMap<String, PackageEntry> versions = new TreeMap<> ();

        for ( final ArtifactInformation art : this.channel.getArtifacts () )
        {
            final String pkg = art.getMetaData ().get ( KEY_NPM_PACKAGE_JSON );
            if ( pkg == null )
            {
                continue;
            }

            try
            {
                final JsonElement pkgEle = parser.parse ( pkg );
                final PackageInfo pi = gson.fromJson ( pkgEle, PackageInfo.class );

                if ( !this.moduleName.equals ( pi.getName () ) )
                {
                    continue;
                }

                versions.put ( pi.getVersion (), new PackageEntry ( pi, pkgEle, art ) );
            }
            catch ( final Exception e )
            {
                logger.info ( "Failed to parse package.json of " + art.getId (), e );
                continue;
            }
        }

        // now build the main file

        final JsonObject main = new JsonObject ();

        // pull in meta data from most recent version

        main.addProperty ( "name", this.moduleName );

        if ( !versions.isEmpty () )
        {
            final Entry<String, PackageEntry> mostRecent = versions.lastEntry ();
            final PackageInfo pi = mostRecent.getValue ().getInfo ();
            main.addProperty ( "license", pi.getLicense () );

            final JsonObject distTags = new JsonObject ();
            main.add ( "dist-tags", distTags );

            distTags.addProperty ( "latest", pi.getVersion () );
        }

        final JsonObject times = new JsonObject ();
        main.add ( "time", times );

        final JsonObject versionsEle = new JsonObject ();
        main.add ( "versions", versionsEle );

        for ( final Map.Entry<String, PackageEntry> entry : versions.entrySet () )
        {
            final PackageInfo pi = entry.getValue ().getInfo ();
            final ArtifactInformation art = entry.getValue ().getArtifact ();

            times.add ( pi.getVersion (), gson.toJsonTree ( art.getCreationTimestamp () ) );

            final JsonObject ele = (JsonObject)entry.getValue ().getElement ();

            final JsonObject dist = new JsonObject ();
            dist.addProperty ( "shasum", art.getMetaData ().get ( KEY_SHA1 ) );
            dist.addProperty ( "tarball", String.format ( "%s/artifact/%s/dump", sitePrefix, art.getId () ) );

            ele.add ( "dist", dist );

            versionsEle.add ( pi.getVersion (), ele );
        }

        // render

        try ( OutputStreamWriter out = new OutputStreamWriter ( stream, StandardCharsets.UTF_8 ) )
        {
            gson.toJson ( main, out );
        }
    }
}
