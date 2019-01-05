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

import java.util.Optional;
import java.util.function.Supplier;

public enum PayloadCoding
{
    NONE ( "none", NullPayloadCoding::new ),
    GZIP ( "gzip", GzipPayloadCoding::new ),
    LZMA ( "lzma", LZMAPayloadCoding::new ),
    BZIP2 ( "bzip2", BZip2PayloadCoding::new ),
    ZSTD ( "zstd", ZstdPayloadCoding::new ),
    XZ ( "xz", XZPayloadCoding::new );

    private String value;

    private Supplier<PayloadCodingProvider> newInstanceSupplier;

    private PayloadCoding ( final String value, final Supplier<PayloadCodingProvider> newInstanceSupplier )
    {
        this.value = value;
        this.newInstanceSupplier = newInstanceSupplier;
    }

    public String getValue ()
    {
        return this.value;
    }

    public PayloadCodingProvider createProvider ()
    {
        return this.newInstanceSupplier.get ();
    }

    public static Optional<PayloadCoding> fromValue ( final String payloadCoding )
    {
        if ( payloadCoding == null )
        {
            return Optional.of ( GZIP );
        }

        for ( final PayloadCoding coding : values () )
        {
            if ( coding.value.equals ( payloadCoding ) )
            {
                return Optional.of ( coding );
            }
        }

        return Optional.empty ();
    }
}
