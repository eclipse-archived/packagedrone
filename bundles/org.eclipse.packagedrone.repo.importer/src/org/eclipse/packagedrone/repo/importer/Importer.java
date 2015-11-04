/*******************************************************************************
 * Copyright (c) 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.importer;

public interface Importer
{
    public static final String IMPORTER_ID = "package.drone.importer.id";

    /**
     * Import one or more artifacts into package drone
     *
     * @param context
     *            The context used for importing
     * @param configuration
     *            The importer specific configuration. This could be JSON, XML
     *            or just plain text.
     */
    public void runImport ( ImportContext context, String configuration ) throws Exception;

    public ImporterDescription getDescription ();
}
