/*******************************************************************************
 * Copyright (c) 2014, 2016 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.web.common.internal.menu;

import java.util.function.BiFunction;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.packagedrone.web.common.internal.table.OsgiTableExtensionManager;
import org.eclipse.packagedrone.web.common.menu.MenuManager;
import org.eclipse.packagedrone.web.common.menu.MenuManagerImpl;
import org.eclipse.packagedrone.web.common.table.TableExtensionManagerRequest;
import org.eclipse.packagedrone.web.interceptor.Interceptor;
import org.osgi.framework.InvalidSyntaxException;

public class MenuInterceptor implements Interceptor
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
    public boolean preHandle ( final HttpServletRequest request, final HttpServletResponse response ) throws Exception
    {
        safeSet ( request, MenuManager.PROPERTY_NAME, this.menuManager, MenuManager::new );
        safeSet ( request, TableExtensionManagerRequest.PROPERTY_NAME, this.tableExtensionManager, TableExtensionManagerRequest::new );

        return true;
    }

    protected <T, R> void safeSet ( final HttpServletRequest request, final String property, final T value, final BiFunction<T, HttpServletRequest, R> func )
    {
        if ( value != null && request != null )
        {
            request.setAttribute ( property, func.apply ( value, request ) );
        }
    }

}
