/*******************************************************************************
 * Copyright (c) 2016 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.web.common.internal.table;

import java.util.Collection;
import java.util.function.Consumer;

import org.eclipse.packagedrone.web.common.table.TableExtender;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;

public class OsgiTableExtensionManager extends AbstractTableExtensionManager
{
    private final ServiceTracker<TableExtender, TableExtender> tracker;

    public OsgiTableExtensionManager ()
    {
        this.tracker = new ServiceTracker<> ( FrameworkUtil.getBundle ( OsgiTableExtensionManager.class ).getBundleContext (), TableExtender.class, null );
    }

    public void start () throws InvalidSyntaxException
    {
        this.tracker.open ();
    }

    public void stop ()
    {
        this.tracker.close ();
    }

    @Override
    protected void access ( final Consumer<Collection<TableExtender>> extenders )
    {
        extenders.accept ( this.tracker.getTracked ().values () );
    }
}
