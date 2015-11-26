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

    public ArtifactsWriter ( final List<String> fragments, final long numberOfEntries, final String title, final Instant now, final Map<String, String> additionalProperties, final boolean compressed )
    {
        super ( "artifacts", title, "org.eclipse.equinox.p2.artifact.repository.simpleRepository", now, compressed, additionalProperties );

        this.fragments = fragments;
        this.numberOfEntries = numberOfEntries;
    }

    @Override
    protected void writeContent ( final PrintWriter out ) throws IOException
    {
        writeMappings ( out );
        writeArtifacts ( out );
    }

    private void writeMappings ( final PrintWriter out )
    {
        out.append ( IN ).append ( "<mappings size='3'>" ).append ( NL );

        writeRule ( out, "(& (classifier=osgi.bundle))", "${repoUrl}/plugins/${id}/${version}/${id}_${version}.jar" );
        writeRule ( out, "(& (classifier=binary))", "${repoUrl}/binary/${id}/${version}/${id}_${version}" );
        writeRule ( out, "(& (classifier=org.eclipse.update.feature))", "${repoUrl}/features/${id}/${version}/${id}_${version}.jar" );

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
