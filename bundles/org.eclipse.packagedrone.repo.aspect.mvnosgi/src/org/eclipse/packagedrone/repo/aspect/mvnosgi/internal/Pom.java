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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;

public class Pom
{
    private static final boolean USE_POM_GENERATION_FALLBACK = true;

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
        return generatePom ();
    }

    private String generatePom ()
    {
        try
        {
            final Model model = new Model ();
            model.setGroupId ( this.groupId );
            model.setArtifactId ( this.artifactId );
            model.setVersion ( this.version );

            final StringWriter w = new StringWriter ();
            new MavenXpp3Writer ().write ( w, model );

            return w.toString ();
        }
        catch ( final IOException ex )
        {
            // IMHO we should never catch an IOException if we are using a StringWriter...

            if ( USE_POM_GENERATION_FALLBACK )
            {
                return generatePomFallback ();
            }
            else
            {
                return null;
            }
        }
    }

    /**
     * Generate a simple pom.
     * This function should generate a valid simple pom file.
     * The pom could be simple but the function should never fail.
     * We could use this function if the normal pom generation process failed.
     *
     * @return a simple pom
     */
    private String generatePomFallback ()
    {
        final StringBuilder sb = new StringBuilder ( 1024 );

        sb.append ( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" );
        sb.append ( "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">" );
        sb.append ( String.format ( "<modelVersion>4.0.0</modelVersion><groupId>%s</groupId><artifactId>%s</artifactId><version>%s</version>", this.groupId, this.artifactId, this.version ) );
        sb.append ( "</project>" );

        return sb.toString ();
    }

    public InputStream getInputStream ()
    {
        return new ByteArrayInputStream ( generatePom ().getBytes ( StandardCharsets.UTF_8 ) );
    }

}
