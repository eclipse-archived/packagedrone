/*******************************************************************************
 * Copyright (c) 2015, 2016 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.utils.rpm.tests;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.eclipse.packagedrone.utils.rpm.RpmInputStream;
import org.eclipse.packagedrone.utils.rpm.RpmTag;
import org.eclipse.packagedrone.utils.rpm.app.Dumper;
import org.junit.Assert;
import org.junit.Test;

public class InputStreamTest
{

    @Test
    public void test1 () throws IOException
    {
        try ( final RpmInputStream in = new RpmInputStream ( new BufferedInputStream ( new FileInputStream ( new File ( "data/org.eclipse.scada-0.2.1-1.noarch.rpm" ) ) ) ) )
        {
            Dumper.dumpAll ( in );

            Assert.assertEquals ( 280, in.getPayloadHeader ().getStart () );
            Assert.assertEquals ( 3501, in.getPayloadHeader ().getLength () );

            Assert.assertEquals ( "cpio", in.getPayloadHeader ().getTag ( RpmTag.PAYLOAD_FORMAT ) );
            Assert.assertEquals ( "lzma", in.getPayloadHeader ().getTag ( RpmTag.PAYLOAD_CODING ) );

            Assert.assertEquals ( "org.eclipse.scada", in.getPayloadHeader ().getTag ( RpmTag.NAME ) );
            Assert.assertEquals ( "0.2.1", in.getPayloadHeader ().getTag ( RpmTag.VERSION ) );
            Assert.assertEquals ( "1", in.getPayloadHeader ().getTag ( RpmTag.RELEASE ) );

            Assert.assertEquals ( "noarch", in.getPayloadHeader ().getTag ( RpmTag.ARCH ) );
            Assert.assertEquals ( "linux", in.getPayloadHeader ().getTag ( RpmTag.OS ) );
            Assert.assertEquals ( "EPL", in.getPayloadHeader ().getTag ( RpmTag.LICENSE ) );

            Assert.assertArrayEquals ( new String[] { //
                    "/etc/", //
                    "/etc/eclipsescada/", //
                    "/etc/profile.d/", //
                    "/usr/bin/", //
                    "/usr/", //
                    "/usr/share/", //
                    "/usr/share/eclipsescada/", //
                    "/usr/share/eclipsescada/sql/", //
                    "/var/log/", //
                    "/var/run/", //
            }, (String[])in.getPayloadHeader ().getTag ( RpmTag.DIRNAMES ) );
        }
    }

    @Test
    public void test2 () throws IOException
    {
        try ( final RpmInputStream in = new RpmInputStream ( new BufferedInputStream ( new FileInputStream ( new File ( "data/org.eclipse.scada-centos6-0.2.1-1.noarch.rpm" ) ) ) ) )
        {
            Dumper.dumpAll ( in );
        }
    }

}
