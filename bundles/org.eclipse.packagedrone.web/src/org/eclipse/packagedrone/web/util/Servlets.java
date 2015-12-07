/*******************************************************************************
 * Copyright (c) 2015 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.web.util;

import java.io.File;

import javax.servlet.MultipartConfigElement;

public class Servlets
{
    public static MultipartConfigElement createMultiPartConfiguration ( final String prefix )
    {
        final String location = System.getProperty ( prefix + ".location", System.getProperty ( "java.io.tmpdir" ) + File.separator + prefix );
        final long maxFileSize = Long.getLong ( prefix + ".maxFileSize", 1 * 1024 * 1024 * 1024 /* 1GB */ );
        final long maxRequestSize = Long.getLong ( prefix + ".maxRequestSize", 1 * 1024 * 1024 * 1024/* 1GB */ );
        final int fileSizeThreshold = Integer.getInteger ( prefix + ".fileSizeThreshold", 10 * 1024 * 1024 /* 10 MB */ );

        return new MultipartConfigElement ( location, maxFileSize, maxRequestSize, fileSizeThreshold );
    }
}
