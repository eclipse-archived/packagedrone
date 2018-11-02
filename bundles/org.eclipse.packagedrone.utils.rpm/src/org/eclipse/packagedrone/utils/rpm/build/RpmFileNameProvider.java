/*******************************************************************************
 * Copyright (c) 2018 Yariv Amar and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Yariv Amar - initial API
 *     Red Hat Inc
 *******************************************************************************/
package org.eclipse.packagedrone.utils.rpm.build;

import org.eclipse.packagedrone.utils.rpm.RpmLead;
import org.eclipse.packagedrone.utils.rpm.RpmVersion;

@FunctionalInterface
public interface RpmFileNameProvider
{
    public String getRpmFileName ( String name, RpmVersion version, String architecture );

    /**
     * Legacy filename provider.
     * <p>
     * this provider is the legacy file name format, using "-" before the
     * "arch.rpm" it is here, and set as the default for backwards compatibility
     * </p>
     */
    public static final RpmFileNameProvider LEGACY_FILENAME_PROVIDER = ( name, version, architecture ) -> {
        final StringBuilder sb = new StringBuilder ( RpmLead.toLeadName ( name, version ) );
        sb.append ( '-' ).append ( architecture ).append ( ".rpm" );
        return sb.toString ();
    };

    /**
     * Default filename provider.
     * <p>
     * this rpm file name provider follows the standard RPM file name as
     * {@code <name>-<version>-<release>.<architecture>.rpm}
     * </p>
     */
    public static final RpmFileNameProvider DEFAULT_FILENAME_PROVIDER = ( name, version, architecture ) -> {
        final StringBuilder sb = new StringBuilder ( RpmLead.toLeadName ( name, version ) );
        sb.append ( '.' ).append ( architecture ).append ( ".rpm" );
        return sb.toString ();
    };

}
