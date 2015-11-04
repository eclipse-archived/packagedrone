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

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;

import org.eclipse.packagedrone.repo.MetaKey;

/**
 * Context for import operations <br/>
 * Calling the schedule methods from different threads works as long as the
 * import process is not finished.
 */
public interface ImportSubContext
{
    /**
     * Schedule the import of an artifact <br/>
     * The stream will be closed by the import process, whether the import was
     * successful or not.
     * <p>
     * Although technically possible, this method should not be called with
     * streams based on network communication or other sources which could
     * block.
     * </p>
     *
     * @param stream
     *            the stream to import data from
     * @param name
     *            the name of the artifact
     * @param providedMetaData
     *            the provided meta data
     * @return
     *         A new context which will add artifacts below this scheduled
     *         artifact
     */
    public ImportSubContext scheduleImport ( InputStream stream, String name, Map<MetaKey, String> providedMetaData );

    /**
     * Schedule the import of an artifact</br>
     *
     * @param file
     *            the file to import from
     * @param deleteAfterImport
     *            If set to <code>true</code> then the import process will
     *            delete the file after the import process. Deleting the file
     *            follows the same rules
     *            as closing the stream in
     *            {@link #scheduleImport(InputStream, String, Map)}.
     * @param name
     *            the name of the artifact
     * @param providedMetaData
     *            the provided meta data
     * @return
     *         A new context which will add artifacts below this scheduled
     *         artifact
     */
    public ImportSubContext scheduleImport ( Path file, boolean deleteAfterImport, String name, Map<MetaKey, String> providedMetaData );

    /**
     * Schedule the import of an artifact from a file <br/>
     * This method actually calls:
     * <code>scheduleImport ( file, true, name, null )</code>.
     *
     * @param file
     *            the file to import from
     * @param name
     *            the name of the artifact
     * @return
     *         A new context which will add artifacts below this scheduled
     *         artifact
     */
    public default ImportSubContext scheduleImport ( final Path file, final String name )
    {
        return scheduleImport ( file, true, name, null );
    }
}
