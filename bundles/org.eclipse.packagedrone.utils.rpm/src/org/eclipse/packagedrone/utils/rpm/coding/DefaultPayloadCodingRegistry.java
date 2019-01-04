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
package org.eclipse.packagedrone.utils.rpm.coding;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class DefaultPayloadCodingRegistry
{
    private static final String GZIP = "gzip";

    private static final String BZIP2 = "bzip2";

    private static final String LZMA = "lzma";

    private static final String XZ = "xz";

    private static final String ZSTD = "zstd";

    private static final PayloadCodingProvider NULL_PAYLOAD_CODING = new NullPayloadCoding ();

    private static final Map<String, PayloadCodingProvider> REGISTRY = new TreeMap<> ();

    static
    {
        REGISTRY.put ( GZIP, new GzipPayloadCoding () );
        REGISTRY.put ( BZIP2, new BZip2PayloadCoding () );
        REGISTRY.put ( LZMA, new LZMAPayloadCoding () );
        REGISTRY.put ( XZ, new XZPayloadCoding () );
        REGISTRY.put ( ZSTD, new ZstdPayloadCoding () );
    }

    public static PayloadCodingProvider get ( final String coding ) throws IOException
    {
        if ( coding == null )
        {
            return NULL_PAYLOAD_CODING;
        }

        final PayloadCodingProvider payloadCoding = REGISTRY.get ( coding );

        if ( payloadCoding == null )
        {
            throw new IOException ( String.format ( "Unknown payload coding '%s'", coding ) );
        }

        return payloadCoding;
    }
}
