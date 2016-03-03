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
import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.net.URI;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.servlet.http.HttpServletRequest;

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
    private static final TableColumn COLUMN_URL = new TableColumn ( "ci-link", -10_000, "CI", "Continous Integration" );

    private static final MetaKey KEY_JENKINS_NUMBER = new MetaKey ( "jenkins", "buildNumber" );

    private static final MetaKey KEY_JENKINS_JOB_NAME = new MetaKey ( "jenkins", "jobName" );

    private static final MetaKey KEY_JENKINS_URL = new MetaKey ( "jenkins", "buildUrl" );

    private static final String SERVER_NAME_JENKINS = "Jenkins";

    private static final MetaKey KEY_HUDSON_NUMBER = new MetaKey ( "hudson", "buildNumber" );

    private static final MetaKey KEY_HUDSON_URL = new MetaKey ( "hudson", "buildUrl" );

    private static final MetaKey KEY_HUDSON_JOB_NAME = new MetaKey ( "hudson", "jobName" );

    private static final String SERVER_NAME_HUDSON = "Hudson";

    private static final MetaKey KEY_TRAVIS_REPO = new MetaKey ( "travis", "repoSlug" );

    private static final MetaKey KEY_TRAVIS_JOB_ID = new MetaKey ( "travis", "jobId" );

    private static final MetaKey KEY_TRAVIS_JOB_NUMBER = new MetaKey ( "travis", "jobNumber" );

    private static final String SERVER_NAME_TRAVIS = "Travis CI";

    @Override
    public void getColumns ( final HttpServletRequest request, final TableDescriptor descriptor, final Consumer<TableColumnProvider> columnReceiver )
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
        map.map ( BuildUrlExtender::fromTravis );

        return map.get ().orElse ( null );
    }

    protected static Optional<String> fromJenkins ( final ArtifactInformation art )
    {
        return fromUrlAndNumber ( art, KEY_JENKINS_URL, KEY_JENKINS_NUMBER, KEY_JENKINS_JOB_NAME, SERVER_NAME_JENKINS );
    }

    protected static Optional<String> fromHudson ( final ArtifactInformation art )
    {
        return fromUrlAndNumber ( art, KEY_HUDSON_URL, KEY_HUDSON_NUMBER, KEY_HUDSON_JOB_NAME, SERVER_NAME_HUDSON );
    }

    private static Optional<String> makeUrl ( final String serverName, final Supplier<String> urlProvider, final Supplier<String> labelProvider, final Supplier<String> titleProvider )
    {
        final String url;

        try
        {
            url = urlProvider.get ();
            if ( url == null || url.isEmpty () )
            {
                return empty ();
            }
            new URI ( url );
        }
        catch ( final Exception e )
        {
            return empty ();
        }

        final String label;
        try
        {
            label = labelProvider.get ();
        }
        catch ( final Exception e )
        {
            return empty ();
        }

        if ( label == null || label.isEmpty () )
        {
            return empty ();
        }

        String title = null;

        try
        {
            title = titleProvider.get ();
        }
        catch ( final Exception e )
        {
            // ignore
        }

        final StringBuilder sb = new StringBuilder ();

        sb.append ( "<a target=\"_blank\" href=\"" ).append ( url ).append ( "\"" );

        if ( title != null && !title.isEmpty () )
        {
            sb.append ( " title=\"" );
            sb.append ( htmlEscaper ().escape ( title ) );
            sb.append ( "\"" );
        }
        else
        {
            sb.append ( String.format ( "title=\"%s build link\"", serverName ) );
        }

        sb.append ( ">" );
        sb.append ( htmlEscaper ().escape ( label ) );
        sb.append ( "</a>" );

        return of ( sb.toString () );
    }

    private static Optional<String> fromUrlAndNumber ( final ArtifactInformation art, final MetaKey keyUrl, final MetaKey keyNumber, final MetaKey keyJobName, final String serverName )
    {
        return makeUrl ( serverName, () -> art.getMetaData ().get ( keyUrl ), () -> {
            return String.format ( "#%d", Long.parseLong ( art.getMetaData ().get ( keyNumber ) ) );
        }, () -> {
            final Optional<String> jobName = Optional.ofNullable ( art.getMetaData ().get ( keyJobName ) );
            return jobName.map ( name -> String.format ( "Build #%s of job '%s' on %s", name, serverName ) ).orElse ( null );
        } );
    }

    private static Optional<String> fromTravis ( final ArtifactInformation art )
    {
        return makeUrl ( SERVER_NAME_TRAVIS, () -> {
            final String repo = art.getMetaData ().get ( KEY_TRAVIS_REPO );
            if ( repo == null || repo.isEmpty () )
            {
                return null;
            }
            return String.format ( "https://travis-ci.org/%s/jobs/%s", repo, art.getMetaData ().get ( KEY_TRAVIS_JOB_ID ) );
        }, () -> {
            final Optional<String> jobNumber = Optional.ofNullable ( art.getMetaData ().get ( KEY_TRAVIS_JOB_NUMBER ) );
            return jobNumber.map ( name -> "#" + name ).orElse ( null );
        }, () -> {
            final Optional<String> jobName = Optional.ofNullable ( art.getMetaData ().get ( KEY_TRAVIS_JOB_NUMBER ) );
            return jobName.map ( name -> String.format ( "%s job #%s of repository %s", SERVER_NAME_TRAVIS, name, art.getMetaData ().get ( KEY_TRAVIS_REPO ) ) ).orElse ( null );
        } );
    }
}
