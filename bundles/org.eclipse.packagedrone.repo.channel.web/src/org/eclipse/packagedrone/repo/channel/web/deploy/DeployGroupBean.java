package org.eclipse.packagedrone.repo.channel.web.deploy;

import org.eclipse.packagedrone.repo.channel.deploy.DeployGroup;

public class DeployGroupBean
{
    private String name;

    public void setName ( final String name )
    {
        this.name = name;
    }

    public String getName ()
    {
        return this.name;
    }

    public static DeployGroupBean fromGroup ( final DeployGroup group )
    {
        final DeployGroupBean result = new DeployGroupBean ();
        result.setName ( group.getName () );
        return result;
    }

}
