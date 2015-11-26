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
package org.eclipse.packagedrone.repo.adapter.p2.internal.aspect;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class MetaDataWriter extends AbstractWriter
{
    private final List<String> fragments;

    private final long numberOfEntries;

    public MetaDataWriter ( final List<String> fragments, final long numberOfEntries, final String title, final Instant now, final Map<String, String> additionalProperties, final boolean compressed )
    {
        super ( "content", title, "org.eclipse.equinox.internal.p2.metadata.repository.LocalMetadataRepository", now, compressed, additionalProperties );

        this.fragments = fragments;
        this.numberOfEntries = numberOfEntries;
    }

    @Override
    protected void writeContent ( final PrintWriter out ) throws IOException
    {
        out.append ( IN ).format ( "<units size='%s'>", this.numberOfEntries ).append ( NL );

        this.fragments.stream ().forEach ( out::append );

        out.append ( IN ).append ( "</units>" ).append ( NL );
    }

}
