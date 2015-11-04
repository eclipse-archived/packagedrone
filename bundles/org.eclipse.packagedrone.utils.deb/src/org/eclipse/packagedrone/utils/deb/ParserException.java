package org.eclipse.packagedrone.utils.deb;

public class ParserException extends Exception
{
    private static final long serialVersionUID = 1L;

    public ParserException ()
    {
        super ();
    }

    public ParserException ( final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace )
    {
        super ( message, cause, enableSuppression, writableStackTrace );
    }

    public ParserException ( final String message, final Throwable cause )
    {
        super ( message, cause );
    }

    public ParserException ( final String message )
    {
        super ( message );
    }

    public ParserException ( final Throwable cause )
    {
        super ( cause );
    }

}
