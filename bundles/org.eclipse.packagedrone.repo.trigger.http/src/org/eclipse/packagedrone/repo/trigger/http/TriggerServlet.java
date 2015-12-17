package org.eclipse.packagedrone.repo.trigger.http;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TriggerServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    private final HttpTrigger trigger;

    public TriggerServlet ( final HttpTrigger trigger )
    {
        this.trigger = trigger;
    }

    @Override
    protected void doGet ( final HttpServletRequest req, final HttpServletResponse resp ) throws ServletException, IOException
    {
        this.trigger.process ();
    }
}
