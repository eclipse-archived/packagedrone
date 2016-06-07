/*******************************************************************************
 * Copyright (c) 2014, 2016 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.utils.deb.build;

/**
 * Additional information for content entries
 */
public class EntryInformation
{
    public static final EntryInformation DEFAULT_DIRECTORY = new EntryInformation ( 0755 );

    public static final EntryInformation DEFAULT_FILE = new EntryInformation ( 0644 );

    public static final EntryInformation DEFAULT_FILE_EXEC = new EntryInformation ( 0755 );

    public static final EntryInformation DEFAULT_FILE_CONF = new EntryInformation ( "root", "root", 0644, true );

    private final String user;

    private final String group;

    private final int mode;

    private final boolean configurationFile;

    public EntryInformation ( final String user, final String group, final int mode, final boolean configurationFile )
    {
        this.user = user;
        this.group = group;
        this.mode = mode;
        this.configurationFile = configurationFile;
    }

    public EntryInformation ( final int mode )
    {
        this ( "root", "root", mode, false );
    }

    public boolean isConfigurationFile ()
    {
        return this.configurationFile;
    }

    public String getGroup ()
    {
        return this.group;
    }

    public int getMode ()
    {
        return this.mode;
    }

    public String getUser ()
    {
        return this.user;
    }
}
