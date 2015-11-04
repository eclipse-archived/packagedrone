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
package org.eclipse.packagedrone.utils.rpm;

import java.util.HashMap;
import java.util.Map;

public enum RpmTag implements RpmBaseTag
{
    NAME ( 1000 ),
    VERSION ( 1001 ),
    RELEASE ( 1002 ),
    EPOCH ( 1003 ),
    SUMMARY ( 1004 ),
    DESCRIPTION ( 1005 ),
    BUILDTIME ( 1006 ),
    BUILDHOST ( 1007 ),
    INSTALLED_SIZE ( 1009 ),
    DISTRIBUTION ( 1010 ),
    VENDOR ( 1011 ),
    LICENSE ( 1014 ),
    PACKAGER ( 1015 ),
    GROUP ( 1016 ),
    URL ( 1020 ),
    OS ( 1021 ),
    ARCH ( 1022 ),
    SOURCE_PACKAGE ( 1044 ),
    ARCHIVE_SIZE ( 1046 ),
    PROVIDE_NAME ( 1047 ),
    REQUIRE_FLAGS ( 1048 ),
    REQUIRE_NAME ( 1049 ),
    REQUIRE_VERSION ( 1050 ),
    CONFLICT_FLAGS ( 1053 ),
    CONFLICT_NAME ( 1054 ),
    CONFLICT_VERSION ( 1055 ),
    CHANGELOG_TIMESTAMP ( 1080 ),
    CHANGELOG_AUTHOR ( 1081 ),
    CHANGELOG_TEXT ( 1082 ),
    OBSOLETE_NAME ( 1090 ),
    PROVIDE_FLAGS ( 1112 ),
    PROVIDE_VERSION ( 1113 ),
    OBSOLETE_FLAGS ( 1114 ),
    OBSOLETE_VERSION ( 1115 ),
    BASENAMES ( 1117 ),
    DIRNAMES ( 1118 ),
    PAYLOAD_FORMAT ( 1124 ),
    PAYLOAD_CODING ( 1125 );

    private Integer value;

    private RpmTag ( final Integer value )
    {
        this.value = value;
    }

    @Override
    public Integer getValue ()
    {
        return this.value;
    }

    private final static Map<Integer, RpmTag> all = new HashMap<> ( RpmTag.values ().length );

    static
    {
        for ( final RpmTag tag : values () )
        {
            all.put ( tag.getValue (), tag );
        }
    }

    public static RpmTag find ( final Integer value )
    {
        return all.get ( value );
    }
}
