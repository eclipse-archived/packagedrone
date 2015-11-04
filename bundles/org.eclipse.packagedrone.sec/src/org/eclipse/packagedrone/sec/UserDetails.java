package org.eclipse.packagedrone.sec;

import java.util.HashSet;
import java.util.Set;

/**
 * A user details object used for updating user records
 */
public class UserDetails
{
    private final String name;

    private final String email;

    private final HashSet<String> roles;

    public UserDetails ( final String name, final String email, final Set<String> roles )
    {
        this.name = name;
        this.email = email;
        this.roles = new HashSet<> ( roles );
    }

    public String getName ()
    {
        return this.name;
    }

    public String getEmail ()
    {
        return this.email;
    }

    public HashSet<String> getRoles ()
    {
        return this.roles;
    }
}
