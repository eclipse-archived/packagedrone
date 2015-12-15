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
package org.eclipse.packagedrone.repo.adapter.deb.servlet;

import static com.google.common.html.HtmlEscapers.htmlEscaper;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.packagedrone.VersionInformation;
import org.eclipse.scada.utils.str.ExtendedPropertiesReplacer;
import org.eclipse.scada.utils.str.StringReplacer;

import com.google.common.io.CharStreams;

public class Helper
{
    public static void render ( final HttpServletResponse response, final URL url, final String title, final Map<String, Object> model ) throws IOException
    {
        final PrintWriter w = response.getWriter ();
        response.setContentType ( "text/html" );
        final String content = StringReplacer.replace ( loadResource ( url ), new ExtendedPropertiesReplacer ( model ), StringReplacer.DEFAULT_PATTERN, true );

        final Map<String, Object> m2 = new HashMap<> ( 2 );
        m2.put ( "content", content );
        m2.put ( "title", htmlEscaper ().escape ( title ) );
        m2.put ( "version", VersionInformation.VERSION );

        final String fo = StringReplacer.replace ( loadResource ( Helper.class.getResource ( "content/base.html" ) ), new ExtendedPropertiesReplacer ( m2 ), StringReplacer.DEFAULT_PATTERN, true );

        w.write ( fo );
    }

    private static String loadResource ( final URL url ) throws IOException
    {
        try ( InputStream is = url.openStream (); Reader r = new InputStreamReader ( is, StandardCharsets.UTF_8 ) )
        {
            return CharStreams.toString ( r );
        }
    }

}
