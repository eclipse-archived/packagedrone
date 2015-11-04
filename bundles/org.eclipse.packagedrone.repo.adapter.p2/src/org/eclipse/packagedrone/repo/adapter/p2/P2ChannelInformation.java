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
package org.eclipse.packagedrone.repo.adapter.p2;

import org.eclipse.packagedrone.repo.MetaKeyBinding;
import org.eclipse.packagedrone.repo.adapter.p2.aspect.P2RepositoryAspect;

public class P2ChannelInformation
{
    @MetaKeyBinding ( namespace = P2RepositoryAspect.ID, key = "title" )
    private String title;

    @MetaKeyBinding ( namespace = P2RepositoryAspect.ID, key = "mirrorsUrl" )
    private String mirrorsUrl;

    @MetaKeyBinding ( namespace = P2RepositoryAspect.ID, key = "statisticsUrl" )
    private String statisticsUrl;

    public void setTitle ( final String title )
    {
        this.title = title;
    }

    public String getTitle ()
    {
        return this.title;
    }

    public void setMirrorsUrl ( final String mirrorsUrl )
    {
        this.mirrorsUrl = mirrorsUrl;
    }

    public String getMirrorsUrl ()
    {
        return this.mirrorsUrl;
    }

    public void setStatisticsUrl ( final String statisticsUrl )
    {
        this.statisticsUrl = statisticsUrl;
    }

    public String getStatisticsUrl ()
    {
        return this.statisticsUrl;
    }
}
