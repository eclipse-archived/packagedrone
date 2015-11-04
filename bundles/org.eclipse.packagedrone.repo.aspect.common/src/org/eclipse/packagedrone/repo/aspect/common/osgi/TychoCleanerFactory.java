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
package org.eclipse.packagedrone.repo.aspect.common.osgi;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.packagedrone.repo.aspect.ChannelAspect;
import org.eclipse.packagedrone.repo.aspect.ChannelAspectFactory;
import org.eclipse.packagedrone.repo.aspect.listener.ChannelListener;
import org.eclipse.packagedrone.repo.aspect.listener.PreAddContext;

public class TychoCleanerFactory implements ChannelAspectFactory
{
    private static final String ID = "tycho-cleaner";

    private static List<Pattern> ignoredPatterns = new LinkedList<> ();

    static
    {
        ignoredPatterns.add ( Pattern.compile ( ".*-p2content.xml$" ) );
        ignoredPatterns.add ( Pattern.compile ( ".*-p2artifacts.xml$" ) );
        ignoredPatterns.add ( Pattern.compile ( ".*-p2metadata.xml$" ) );
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
            public ChannelListener getChannelListener ()
            {
                return new ChannelListener () {

                    @Override
                    public void artifactPreAdd ( final PreAddContext context )
                    {
                        if ( !context.isExternal () )
                        {
                            // we don't prevent internal artifacts from being added
                            return;
                        }

                        final String name = context.getName ();
                        for ( final Pattern pattern : ignoredPatterns )
                        {
                            if ( pattern.matcher ( name ).matches () )
                            {
                                context.vetoAdd ();
                                return;
                            }
                        }
                    }
                };
            }
        };
    }
}
