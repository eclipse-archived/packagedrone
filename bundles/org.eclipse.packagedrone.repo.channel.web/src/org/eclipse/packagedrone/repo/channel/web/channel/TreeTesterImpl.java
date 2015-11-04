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
package org.eclipse.packagedrone.repo.channel.web.channel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.packagedrone.repo.Severity;
import org.eclipse.packagedrone.repo.channel.ArtifactInformation;

public class TreeTesterImpl implements TreeTester
{

    private final Map<String, List<ArtifactInformation>> tree;

    private final Map<String, Severity> cache = new HashMap<> ();

    public TreeTesterImpl ( final Map<String, List<ArtifactInformation>> tree )
    {
        this.tree = tree;
    }

    @Override
    public Severity getState ( final ArtifactInformation artifact )
    {
        if ( this.cache.containsKey ( artifact.getId () ) )
        {
            return this.cache.get ( artifact.getId () );
        }

        final Severity sev = evalSeverity ( artifact );
        this.cache.put ( artifact.getId (), sev );
        return sev;
    }

    private Severity evalSeverity ( final ArtifactInformation artifact )
    {
        final Severity sev = artifact.getOverallValidationState ();
        if ( sev == Severity.ERROR )
        {
            return sev;
        }

        final Severity childSev = getChildState ( artifact );
        if ( childSev == null )
        {
            return sev;
        }

        if ( sev == null )
        {
            return childSev;
        }

        if ( childSev.ordinal () > sev.ordinal () )
        {
            return childSev;
        }
        return sev;
    }

    private Severity getChildState ( final ArtifactInformation artifact )
    {
        final List<ArtifactInformation> childs = this.tree.get ( artifact.getId () );

        if ( childs == null )
        {
            return null;
        }

        Severity maxSev = null;

        for ( final ArtifactInformation child : childs )
        {
            final Severity sev = getState ( child );
            if ( sev == Severity.ERROR )
            {
                return Severity.ERROR;
            }

            maxSev = sev;
        }

        return maxSev;
    }

}
