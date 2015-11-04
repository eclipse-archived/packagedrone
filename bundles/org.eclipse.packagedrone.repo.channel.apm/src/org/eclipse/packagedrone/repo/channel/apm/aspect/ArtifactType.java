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
package org.eclipse.packagedrone.repo.channel.apm.aspect;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public enum ArtifactType
{
    STORED ( Arrays.asList ( "stored" ), true ),
    VIRTUAL ( Arrays.asList ( "virtual" ), false ),
    GENERATOR ( Arrays.asList ( "stored", "generator" ), true ),
    GENERATED ( Arrays.asList ( "generated" ), true );

    private Set<String> facetTypes;

    private boolean external;

    private ArtifactType ( final List<String> facetType, final boolean external )
    {
        this.facetTypes = Collections.unmodifiableSet ( new HashSet<> ( facetType ) );
        this.external = external;
    }

    public Set<String> getFacetTypes ()
    {
        return this.facetTypes;
    }

    public boolean isExternal ()
    {
        return this.external;
    }

}
