package org.eclipse.packagedrone.repo.aspect.mvnosgi.web;

import org.eclipse.packagedrone.repo.MetaKeyBinding;

public class Configuration
{
    @MetaKeyBinding ( namespace = "mvnosgi", key = "groupId" )
    private String groupId;

    public void setGroupId ( final String groupId )
    {
        this.groupId = groupId;
    }

    public String getGroupId ()
    {
        return this.groupId;
    }
}
