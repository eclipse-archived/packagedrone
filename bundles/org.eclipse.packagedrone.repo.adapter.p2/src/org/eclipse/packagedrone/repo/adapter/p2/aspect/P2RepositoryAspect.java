/*******************************************************************************
 * Copyright (c) 2014 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.adapter.p2.aspect;

import org.eclipse.packagedrone.repo.adapter.p2.internal.aspect.ExtractorImpl;
import org.eclipse.packagedrone.repo.adapter.p2.internal.aspect.P2RepoChannelAggregator;
import org.eclipse.packagedrone.repo.aspect.ChannelAspect;
import org.eclipse.packagedrone.repo.aspect.ChannelAspectFactory;
import org.eclipse.packagedrone.repo.aspect.aggregate.ChannelAggregator;
import org.eclipse.packagedrone.repo.aspect.extract.Extractor;
import org.eclipse.packagedrone.utils.xml.XmlToolsFactory;

public class P2RepositoryAspect implements ChannelAspectFactory
{
    public static final String ID = "p2.repo";

    private XmlToolsFactory xmlToolsFactory;

    public void setXmlToolsFactory ( final XmlToolsFactory xmlToolsFactory )
    {
        this.xmlToolsFactory = xmlToolsFactory;
    }

    @Override
    public ChannelAspect createAspect ()
    {
        return new ChannelAspect () {

            @Override
            public String getId ()
            {
                return ID;
            }

            @Override
            public Extractor getExtractor ()
            {
                return new ExtractorImpl ( P2RepositoryAspect.this.xmlToolsFactory );
            }

            @Override
            public ChannelAggregator getChannelAggregator ()
            {
                return new P2RepoChannelAggregator ();
            }
        };
    }

}
