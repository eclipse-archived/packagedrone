package org.eclipse.packagedrone.repo.generator;

import org.eclipse.packagedrone.web.LinkTarget;

public class GeneratorInformation implements Comparable<GeneratorInformation>
{
    private final String id;

    private final String label;

    private final String description;

    private final LinkTarget addTarget;

    public GeneratorInformation ( final String id, final String label, final String description, final LinkTarget addTarget )
    {
        this.id = id;
        this.label = label;
        this.description = description;
        this.addTarget = addTarget;
    }

    public LinkTarget getAddTarget ()
    {
        return this.addTarget;
    }

    public String getId ()
    {
        return this.id;
    }

    public String getLabel ()
    {
        return this.label;
    }

    public String getDescription ()
    {
        return this.description;
    }

    @Override
    public int hashCode ()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( this.id == null ? 0 : this.id.hashCode () );
        return result;
    }

    @Override
    public boolean equals ( final Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( obj == null )
        {
            return false;
        }
        if ( this.getClass() != obj.getClass() )
        {
            return false;
        }
        final GeneratorInformation other = (GeneratorInformation)obj;
        if ( this.id == null )
        {
            if ( other.id != null )
            {
                return false;
            }
        }
        else if ( !this.id.equals ( other.id ) )
        {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo ( final GeneratorInformation o )
    {
        return this.id.compareTo ( o.id );
    }

}
