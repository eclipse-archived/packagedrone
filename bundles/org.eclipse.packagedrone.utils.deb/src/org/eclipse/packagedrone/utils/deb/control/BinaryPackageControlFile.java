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
package org.eclipse.packagedrone.utils.deb.control;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.packagedrone.utils.deb.FieldFormatter;

/**
 * A control file for binary packages
 */
public class BinaryPackageControlFile extends GenericControlFile
{
    public static final Map<String, FieldFormatter> FORMATTERS;

    static
    {
        final Map<String, FieldFormatter> formatters = new HashMap<> ();
        formatters.put ( Fields.DESCRIPTION, FieldFormatter.MULTI );

        FORMATTERS = Collections.unmodifiableMap ( formatters );
    }

    public interface Fields
    {
        public static final String PACKAGE = "Package"; //$NON-NLS-1$

        public static final String VERSION = "Version"; //$NON-NLS-1$

        public static final String ARCHITECTURE = "Architecture"; //$NON-NLS-1$

        public static final String SECTION = "Section"; //$NON-NLS-1$

        public static final String PRIORITY = "Priority"; //$NON-NLS-1$

        public static final String ESSENTIAL = "Essential"; //$NON-NLS-1$

        public static final String DESCRIPTION = "Description"; //$NON-NLS-1$

        public static final String MAINTAINER = "Maintainer"; //$NON-NLS-1$

        public static final String INSTALLED_SIZE = "Installed-Size"; //$NON-NLS-1$

        public static final String CONFLICTS = "Conflicts"; //$NON-NLS-1$

        public static final String DEPENDS = "Depends"; //$NON-NLS-1$

        public static final String PRE_DEPENDS = "Pre-Depends"; //$NON-NLS-1$
    }

    public void setPackage ( final String value )
    {
        this.values.put ( Fields.PACKAGE, value );
    }

    public String getPackage ()
    {
        return this.values.get ( Fields.PACKAGE );
    }

    public void setVersion ( final String value )
    {
        this.values.put ( Fields.VERSION, value );
    }

    public String getVersion ()
    {
        return this.values.get ( Fields.VERSION );
    }

    public String getArchitecture ()
    {
        return this.values.get ( Fields.ARCHITECTURE );
    }

    public void setArchitecture ( final String value )
    {
        this.values.put ( Fields.ARCHITECTURE, value );
    }

    public String getMaintainer ()
    {
        return this.values.get ( Fields.MAINTAINER );
    }

    public void setMaintainer ( final String value )
    {
        this.values.put ( Fields.MAINTAINER, value );
    }

    public String getDescription ()
    {
        return this.values.get ( Fields.DESCRIPTION );
    }

    public void setDescription ( final String value )
    {
        this.values.put ( Fields.DESCRIPTION, value );
    }

    public String getPriority ()
    {
        return this.values.get ( Fields.PRIORITY );
    }

    public void setPriority ( final String value )
    {
        this.values.put ( Fields.PRIORITY, value );
    }

    public String getSection ()
    {
        return this.values.get ( Fields.SECTION );
    }

    public void setSection ( final String value )
    {
        this.values.put ( Fields.SECTION, value );
    }

    public void validate ()
    {
        validate ( this );
    }

    public static void validate ( final BinaryPackageControlFile controlFile )
    {
        hasField ( controlFile, Fields.PACKAGE );
        hasField ( controlFile, Fields.ARCHITECTURE );
        hasField ( controlFile, Fields.VERSION );
        hasField ( controlFile, Fields.MAINTAINER );
        hasField ( controlFile, Fields.DESCRIPTION );
    }

    private static void hasField ( final BinaryPackageControlFile controlFile, final String field )
    {
        final String result = controlFile.values.get ( field );
        if ( result == null )
        {
            throw new IllegalStateException ( String.format ( "Control file must have field '%s'", field ) );
        }
    }

    public String makeFileName ()
    {
        final String name = String.format ( "%s_%s_%s.deb", getPackage (), getVersion (), getArchitecture () );
        try
        {
            return URLEncoder.encode ( name, "UTF-8" );
        }
        catch ( final UnsupportedEncodingException e )
        {
            throw new RuntimeException ( e );
        }
    }
}
