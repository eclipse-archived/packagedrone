/*******************************************************************************
 * Copyright (c) 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Rathgeb - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.aspect.mvnosgi.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;

public class Pom
{
    private final String groupId;

    private final String artifactId;

    private final String version;

    public Pom ( final String groupId, final String artifactId, final String version )
    {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    @Override
    public String toString ()
    {
        try
        {
            final StringWriter sw = new StringWriter ();
            writePom ( sw );
            return sw.toString ();
        }
        catch ( final IOException e )
        {
            return super.toString ();
        }

    }

    public void writePom ( final OutputStream stream ) throws IOException
    {
        writePom ( new OutputStreamWriter ( stream, StandardCharsets.UTF_8 ) );
    }

    public void writePom ( final Writer writer ) throws IOException
    {
        final Model model = new Model ();
        model.setGroupId ( this.groupId );
        model.setArtifactId ( this.artifactId );
        model.setVersion ( this.version );

        new MavenXpp3Writer ().write ( writer, model );
    }
}
