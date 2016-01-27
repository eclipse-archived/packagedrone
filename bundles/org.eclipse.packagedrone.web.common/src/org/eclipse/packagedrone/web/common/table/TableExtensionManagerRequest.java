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
package org.eclipse.packagedrone.web.common.table;

import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

public class TableExtensionManagerRequest
{
    public static final String PROPERTY_NAME = "tableExtensionManager";

    private final TableExtensionManager manager;

    private final HttpServletRequest request;

    public TableExtensionManagerRequest ( final TableExtensionManager manager, final HttpServletRequest request )
    {
        this.manager = manager;
        this.request = request;
    }

    public TableExtension createExtensions ( final String id, final Set<String> tags, final Map<String, Object> context )
    {
        return this.manager.createExtensions ( this.request, new TableDescriptor ( id, tags, context ) );
    }

}
