/*******************************************************************************
 * Copyright (c) 2017 Gemtec GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Peter Jeschke/Gemtec GmbH - initial implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.aspect.common.manifest;

import org.eclipse.packagedrone.repo.aspect.ChannelAspect;
import org.eclipse.packagedrone.repo.aspect.ChannelAspectFactory;
import org.eclipse.packagedrone.repo.aspect.extract.Extractor;

/**
 * Provides manifest information as metadata.
 *
 * @author Peter Jeschke
 */
public class ManifestAspectFactory implements ChannelAspectFactory
{
    private static final String ID = "manifest";

    @Override
    public ChannelAspect createAspect ()
    {
        return new ChannelAspect () {

            @Override
            public Extractor getExtractor ()
            {
                return new ManifestMetadataExtractor ();
            }

            @Override
            public String getId ()
            {
                return ID;
            }
        };
    }

}
