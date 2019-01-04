/*******************************************************************************
 * Copyright (c) 2018 Red Hat Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.utils.rpm.build;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class PayloadCodingRegistry
{
    public static final String GZIP = "gzip";

    public static final String BZIP2 = "bzip2";

    public static final String LZMA = "lzma";

    public static final String XZ = "xz";

    public static final String ZSTD = "zstd";

    private static final PayloadCoding NULL_PAYLOAD_CODING = new NullPayloadCoding ();

    private static final Map<String, PayloadCoding> REGISTRY = new TreeMap<> ();

    static
    {
        REGISTRY.put ( GZIP, new GzipPayloadCoding () );

        REGISTRY.put ( BZIP2, new BZip2PayloadCoding () );

        REGISTRY.put ( LZMA, new LZMAPayloadCoding () );

        REGISTRY.put ( XZ, new XZPayloadCoding () );

        REGISTRY.put ( ZSTD, new ZstdPayloadCoding () );
    }

    public static PayloadCoding get ( final String coding ) throws IOException
    {
        if ( coding == null )
        {
            return NULL_PAYLOAD_CODING;
        }

        PayloadCoding payloadCoding = REGISTRY.get ( coding );

        if ( payloadCoding == null )
        {
            throw new IOException ( String.format( "Unknown payload coding %s", coding ) );
        }

        return payloadCoding;
    }
}
