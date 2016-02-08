/*******************************************************************************
 * Copyright (c) 2015, 2016 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.adapter.p2.internal.aspect;

import static com.google.common.xml.XmlEscapers.xmlAttributeEscaper;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class ArtifactsWriter extends AbstractWriter
{
    private final List<String> fragments;

    private final long numberOfEntries;

    private final List<ArtifactRule> rules;

    public ArtifactsWriter ( final List<String> fragments, final long numberOfEntries, final String title, final Instant now, final Map<String, String> additionalProperties, final boolean compressed, final List<ArtifactRule> rules )
    {
        super ( "artifacts", title, "org.eclipse.equinox.p2.artifact.repository.simpleRepository", now, compressed, additionalProperties );

        this.fragments = fragments;
        this.numberOfEntries = numberOfEntries;
        this.rules = rules;
    }

    @Override
    protected void writeContent ( final PrintWriter out ) throws IOException
    {
        writeMappings ( out );
        writeArtifacts ( out );
    }

    private void writeMappings ( final PrintWriter out )
    {
        out.append ( IN ).format ( "<mappings size='%d'>", this.rules.size () ).append ( NL );

        for ( final ArtifactRule rule : this.rules )
        {
            writeRule ( out, rule.getFilter ().toString (), rule.getPattern () );
        }

        out.append ( IN ).append ( "</mappings>" ).append ( NL );
    }

    private void writeRule ( final PrintWriter out, final String filter, final String output )
    {
        out.append ( IN2 ).format ( "<rule filter='%s' output='%s' />", xmlAttributeEscaper ().escape ( filter ), xmlAttributeEscaper ().escape ( output ) ).append ( NL );
    }

    private void writeArtifacts ( final PrintWriter out )
    {
        out.append ( IN ).format ( "<artifacts size='%s'>", this.numberOfEntries ).append ( NL );

        this.fragments.stream ().forEach ( out::append );

        out.append ( IN ).append ( "</artifacts>" ).append ( NL );
    }

}
