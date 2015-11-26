/*******************************************************************************
 * Copyright (c) 2014, 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.aspect.common.p2;

import javax.xml.stream.XMLOutputFactory;

import org.eclipse.packagedrone.repo.aspect.ChannelAspect;
import org.eclipse.packagedrone.repo.aspect.ChannelAspectFactory;
import org.eclipse.packagedrone.repo.aspect.common.p2.internal.P2Virtualizer;
import org.eclipse.packagedrone.repo.aspect.virtual.Virtualizer;
import org.eclipse.packagedrone.utils.xml.XmlToolsFactory;

public class P2MetaDataAspectFactory implements ChannelAspectFactory
{
    public static final String ID = "p2.metadata";

    private ThreadLocal<XMLOutputFactory> factoryLocal;

    public void setXmlToolsFactory ( final XmlToolsFactory xmlToolsFactory )
    {
        this.factoryLocal = ThreadLocal.withInitial ( xmlToolsFactory::newXMLOutputFactory );
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
            public Virtualizer getArtifactVirtualizer ()
            {
                return new P2Virtualizer ( P2MetaDataAspectFactory.this.factoryLocal::get );
            }
        };
    }

}
