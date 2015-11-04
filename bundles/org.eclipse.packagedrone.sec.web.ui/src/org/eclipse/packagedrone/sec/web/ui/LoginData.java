package org.eclipse.packagedrone.sec.web.ui;

public class LoginData
{
    private String email;

    private String password;

    private Boolean rememberMe = false;

    public String getEmail ()
    {
        return this.email;
    }

    public void setEmail ( final String email )
    {
        this.email = email;
    }

    public String getPassword ()
    {
        return this.password;
    }

    public void setPassword ( final String password )
    {
        this.password = password;
    }

    public boolean isRememberMeSafe ()
    {
        return this.rememberMe == null ? false : this.rememberMe;
    }

    public Boolean getRememberMe ()
    {
        return this.rememberMe;
    }

    public void setRememberMe ( final Boolean rememberMe )
    {
        this.rememberMe = rememberMe;
    }

}
