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
package org.eclipse.packagedrone.repo.manage.system;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A service for backing up and restoring system configuration.
 * <p>
 * This service does provide backup and restore capabilities for system
 * configuration only, not for actual data.
 * </p>
 */
public interface ConfigurationBackupService
{
    /**
     * Spool out the configuration in a ZIP file
     *
     * @param stream
     *            the stream to which the ZIP file is being written
     */
    public void createConfigurationBackup ( OutputStream stream ) throws IOException;

    public void restoreConfiguration ( InputStream stream ) throws Exception;

    public void provisionConfiguration ( InputStream stream ) throws Exception;
}
