/*******************************************************************************
 * Copyright (c) 2015, 2018 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *     Red Hat Inc
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
    SIZE ( 1009 ),
    DISTRIBUTION ( 1010 ),
    VENDOR ( 1011 ),
    LICENSE ( 1014 ),
    PACKAGER ( 1015 ),
    GROUP ( 1016 ),
    URL ( 1020 ),
    OS ( 1021 ),
    ARCH ( 1022 ),
    PREINSTALL_SCRIPT ( 1023 ),
    POSTINSTALL_SCRIPT ( 1024 ),
    PREREMOVE_SCRIPT ( 1025 ),
    POSTREMOVE_SCRIPT ( 1026 ),
    FILE_SIZES ( 1028 ),
    FILE_MODES ( 1030 ),
    FILE_RDEVS ( 1033 ),
    FILE_MTIMES ( 1034 ),
    FILE_DIGESTS ( 1035 ),
    FILE_LINKTO ( 1036 ),
    FILE_FLAGS ( 1037 ),
    FILE_USERNAME ( 1039 ),
    FILE_GROUPNAME ( 1040 ),
    SOURCE_PACKAGE ( 1044 ),
    FILE_VERIFYFLAGS ( 1045 ),
    ARCHIVE_SIZE ( 1046 ),
    PROVIDE_NAME ( 1047 ),
    REQUIRE_FLAGS ( 1048 ),
    REQUIRE_NAME ( 1049 ),
    REQUIRE_VERSION ( 1050 ),
    CONFLICT_FLAGS ( 1053 ),
    CONFLICT_NAME ( 1054 ),
    CONFLICT_VERSION ( 1055 ),
    RPMVERSION ( 1064 ),
    TRIGGER_SCRIPTS ( 1065 ),
    TRIGGER_NAME ( 1066 ),
    TRIGGER_VERSION ( 1067 ),
    TRIGGER_FLAGS ( 1068 ),
    TRIGGER_INDEX ( 1069 ),
    VERIFY_SCRIPT ( 1079 ),
    CHANGELOG_TIMESTAMP ( 1080 ),
    CHANGELOG_AUTHOR ( 1081 ),
    CHANGELOG_TEXT ( 1082 ),
    PREINSTALL_SCRIPT_PROG ( 1085 ),
    POSTINSTALL_SCRIPT_PROG ( 1086 ),
    PREREMOVE_SCRIPT_PROG ( 1087 ),
    POSTREMOVE_SCRIPT_PROG ( 1088 ),
    VERIFY_SCRIPT_PROG ( 1091 ),
    TRIGGERSCRIPT_PROG ( 1092 ),
    OBSOLETE_NAME ( 1090 ),
    FILE_DEVICES ( 1095 ),
    FILE_INODES ( 1096 ),
    FILE_LANGS ( 1097 ),
    PROVIDE_FLAGS ( 1112 ),
    PROVIDE_VERSION ( 1113 ),
    OBSOLETE_FLAGS ( 1114 ),
    OBSOLETE_VERSION ( 1115 ),
    DIR_INDEXES ( 1116 ),
    BASENAMES ( 1117 ),
    DIRNAMES ( 1118 ),
    OPTFLAGS ( 1122 ),
    PAYLOAD_FORMAT ( 1124 ),
    PAYLOAD_CODING ( 1125 ),
    PAYLOAD_FLAGS ( 1126 ),
    PLATFORM ( 1132 ),
    LONGSIZE ( 5009 ),
    RECOMMEND_NAME ( 5046 ),
    RECOMMEND_VERSION ( 5047 ),
    RECOMMEND_FLAGS ( 5048 ),
    SUGGEST_NAME ( 5049 ),
    SUGGEST_VERSION ( 5050 ),
    SUGGEST_FLAGS ( 5051 ),
    SUPPLEMENT_NAME ( 5052 ),
    SUPPLEMENT_VERSION ( 5053 ),
    SUPPLEMENT_FLAGS ( 5054 ),
    ENHANCE_NAME ( 5055 ),
    ENHANCE_VERSION ( 5056 ),
    ENHANCE_FLAGS ( 5057 );

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
