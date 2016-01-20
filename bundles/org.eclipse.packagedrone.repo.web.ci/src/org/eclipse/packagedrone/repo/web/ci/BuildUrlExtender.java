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
package org.eclipse.packagedrone.repo.web.ci;

import static com.google.common.html.HtmlEscapers.htmlEscaper;

import java.net.URI;
import java.util.function.Consumer;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.eclipse.packagedrone.utils.MapOnce;
import org.eclipse.packagedrone.web.common.table.TableColumn;
import org.eclipse.packagedrone.web.common.table.TableColumnProvider;
import org.eclipse.packagedrone.web.common.table.TableDescriptor;
import org.eclipse.packagedrone.web.common.table.TableExtender;

/**
 * Enhance table with build links of possible
 */
public class BuildUrlExtender implements TableExtender
{
    private static final @NonNull TableColumn COLUMN_URL = new TableColumn ( "ci-link", -10_000, "CI", "Continous Integration" );

    private static final @NonNull MetaKey KEY_JENKINS_NUMBER = new MetaKey ( "jenkins", "buildNumber" );

    private static final @NonNull MetaKey KEY_JENKINS_JOB_NAME = new MetaKey ( "jenkins", "jobName" );

    private static final @NonNull MetaKey KEY_JENKINS_URL = new MetaKey ( "jenkins", "buildUrl" );

    private static final @NonNull String SERVER_NAME_JENKINS = "Jenkins";

    private static final @NonNull MetaKey KEY_HUDSON_NUMBER = new MetaKey ( "hudson", "buildNumber" );

    private static final @NonNull MetaKey KEY_HUDSON_URL = new MetaKey ( "hudson", "buildUrl" );

    private static final @NonNull MetaKey KEY_HUDSON_JOB_NAME = new MetaKey ( "hudson", "jobName" );

    private static final @NonNull String SERVER_NAME_HUDSON = "Hudson";

    @Override
    public void getColumns ( @NonNull final HttpServletRequest request, @NonNull final TableDescriptor descriptor, @NonNull final Consumer<TableColumnProvider> columnReceiver )
    {
        if ( !descriptor.hasTag ( "artifacts" ) )
        {
            return;
        }

        columnReceiver.accept ( TableColumnProvider.stringProvider ( COLUMN_URL, ArtifactInformation.class, BuildUrlExtender::buildLink ) );
    }

    protected static String buildLink ( final ArtifactInformation art )
    {
        if ( art == null )
        {
            return null;
        }

        final MapOnce<ArtifactInformation, String> map = new MapOnce<> ( art );

        map.map ( BuildUrlExtender::fromHudson );
        map.map ( BuildUrlExtender::fromJenkins );

        return map.get ();
    }

    protected static String fromJenkins ( final ArtifactInformation art )
    {
        return fromUrlAndNumber ( art, KEY_JENKINS_URL, KEY_JENKINS_NUMBER, KEY_JENKINS_JOB_NAME, SERVER_NAME_JENKINS );
    }

    protected static String fromHudson ( final ArtifactInformation art )
    {
        return fromUrlAndNumber ( art, KEY_HUDSON_URL, KEY_HUDSON_NUMBER, KEY_HUDSON_JOB_NAME, SERVER_NAME_HUDSON );
    }

    private static String fromUrlAndNumber ( final ArtifactInformation art, @NonNull final MetaKey keyUrl, @NonNull final MetaKey keyNumber, @NonNull final MetaKey keyJobName, @NonNull final String serverName )
    {
        final String url = art.getMetaData ().get ( keyUrl );
        final String number = art.getMetaData ().get ( keyNumber );
        final String jobName = art.getMetaData ().get ( keyJobName ); // optional

        if ( url == null || url.isEmpty () || number == null || number.isEmpty () )
        {
            return null;
        }

        try
        {
            // test parse
            new URI ( url );
            Long.parseLong ( number );
        }
        catch ( final Exception e )
        {
            // ignore
            return null;
        }

        final StringBuilder sb = new StringBuilder ();

        sb.append ( "<a target=\"_blank\" href=\"" ).append ( url ).append ( "\"" );

        if ( jobName != null && !jobName.isEmpty () )
        {
            sb.append ( " title=\"" );
            sb.append ( htmlEscaper ().escape ( String.format ( "Build #%s of job '%s' on %s", jobName, serverName ) ) );
            sb.append ( "\"" );
        }
        else
        {
            sb.append ( String.format ( "title=\"%s build link\"", serverName ) );
        }

        sb.append ( ">" );
        sb.append ( "#" ).append ( number );
        sb.append ( "</a>" );

        return sb.toString ();
    }
}
