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

import java.util.function.Consumer;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jdt.annotation.NonNull;

public interface TableExtender
{
    public void getColumns ( @NonNull HttpServletRequest request, @NonNull final TableDescriptor descriptor, @NonNull Consumer<TableColumnProvider> columnReceiver );
}
