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
package org.eclipse.packagedrone.repo.adapter.p2.internal.aspect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;

public class ArtifactRule
{
    private static final List<ArtifactRule> REPOSITORY_RULES;

    private static final List<ArtifactRule> ZIP_RULES;

    static
    {
        {
            final ArrayList<ArtifactRule> list = new ArrayList<> ( 3 );

            try
            {
                list.add ( new ArtifactRule ( FrameworkUtil.createFilter ( "(& (classifier=osgi.bundle))" ), "${repoUrl}/plugins/${id}/${version}/${id}_${version}.jar" ) );
                list.add ( new ArtifactRule ( FrameworkUtil.createFilter ( "(& (classifier=binary))" ), "${repoUrl}/binary/${id}/${version}/${id}_${version}" ) );
                list.add ( new ArtifactRule ( FrameworkUtil.createFilter ( "(& (classifier=org.eclipse.update.feature))" ), "${repoUrl}/features/${id}/${version}/${id}_${version}.jar" ) );
            }
            catch ( final InvalidSyntaxException e )
            {
                throw new RuntimeException ( e );
            }

            REPOSITORY_RULES = Collections.unmodifiableList ( list );
        }

        {
            final ArrayList<ArtifactRule> list = new ArrayList<> ( 3 );

            try
            {
                list.add ( new ArtifactRule ( FrameworkUtil.createFilter ( "(& (classifier=osgi.bundle))" ), "${repoUrl}/plugins/${id}_${version}.jar" ) );
                list.add ( new ArtifactRule ( FrameworkUtil.createFilter ( "(& (classifier=binary))" ), "${repoUrl}/binary/${id}_${version}" ) );
                list.add ( new ArtifactRule ( FrameworkUtil.createFilter ( "(& (classifier=org.eclipse.update.feature))" ), "${repoUrl}/features/${id}_${version}.jar" ) );
            }
            catch ( final InvalidSyntaxException e )
            {
                throw new RuntimeException ( e );
            }

            ZIP_RULES = Collections.unmodifiableList ( list );
        }

    }

    private final Filter filter;

    private final String pattern;

    public ArtifactRule ( final Filter filter, final String pattern )
    {
        this.filter = filter;
        this.pattern = pattern;
    }

    public Filter getFilter ()
    {
        return this.filter;
    }

    public String getPattern ()
    {
        return this.pattern;
    }

    public static List<ArtifactRule> getDefaultZipRules ()
    {
        return ZIP_RULES;
    }

    public static List<ArtifactRule> getDefaultRepositoryRules ()
    {
        return REPOSITORY_RULES;
    }
}
