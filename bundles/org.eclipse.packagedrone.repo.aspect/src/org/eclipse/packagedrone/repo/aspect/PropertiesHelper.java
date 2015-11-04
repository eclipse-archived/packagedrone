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
package org.eclipse.packagedrone.repo.aspect;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.CharStreams;

public final class PropertiesHelper
{

    private final static Logger logger = LoggerFactory.getLogger ( PropertiesHelper.class );

    private PropertiesHelper ()
    {
    }

    public static String loadUrl ( final Bundle bundle, final String descUrl )
    {
        if ( descUrl == null )
        {
            return null;
        }

        final URL url = bundle.getEntry ( descUrl );
        if ( url == null )
        {
            return null;
        }

        try ( Reader reader = new InputStreamReader ( url.openStream (), StandardCharsets.UTF_8 ) )
        {
            return CharStreams.toString ( reader );
        }
        catch ( final Exception e )
        {
            logger.info ( "Failed to load url", e );
        }
        return null;
    }
}
