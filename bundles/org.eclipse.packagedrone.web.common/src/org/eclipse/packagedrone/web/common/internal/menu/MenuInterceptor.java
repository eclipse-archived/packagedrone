/*******************************************************************************
 * Copyright (c) 2014, 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.web.common.internal.menu;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.packagedrone.web.ModelAndView;
import org.eclipse.packagedrone.web.RequestHandler;
import org.eclipse.packagedrone.web.common.internal.table.OsgiTableExtensionManager;
import org.eclipse.packagedrone.web.common.menu.MenuManager;
import org.eclipse.packagedrone.web.common.menu.MenuManagerImpl;
import org.eclipse.packagedrone.web.common.table.TableExtensionManager;
import org.eclipse.packagedrone.web.common.table.TableExtensionManagerRequest;
import org.eclipse.packagedrone.web.interceptor.ModelAndViewInterceptorAdapter;
import org.osgi.framework.InvalidSyntaxException;

public class MenuInterceptor extends ModelAndViewInterceptorAdapter
{
    private MenuManagerImpl menuManager;

    private OsgiTableExtensionManager tableExtensionManager;

    public void activate () throws InvalidSyntaxException
    {
        this.menuManager = new MenuManagerImpl ();

        this.tableExtensionManager = new OsgiTableExtensionManager ();
        this.tableExtensionManager.start ();
    }

    public void deactivate ()
    {
        if ( this.menuManager != null )
        {
            this.menuManager.close ();
            this.menuManager = null;
        }

        if ( this.tableExtensionManager != null )
        {
            this.tableExtensionManager.stop ();
            this.tableExtensionManager = null;
        }
    }

    @Override
    protected void postHandle ( final HttpServletRequest request, final HttpServletResponse response, final RequestHandler requestHandler, final ModelAndView modelAndView )
    {
        if ( modelAndView != null && !modelAndView.isRedirect () )
        {
            modelAndView.put ( "menuManager", new MenuManager ( this.menuManager, request ) );

            final TableExtensionManager table = this.tableExtensionManager;
            if ( table != null && request != null )
            {
                modelAndView.put ( TableExtensionManagerRequest.PROPERTY_NAME, new TableExtensionManagerRequest ( table, request ) );
            }
        }
    }

}
