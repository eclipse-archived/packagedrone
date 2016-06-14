/*******************************************************************************
 * Copyright (c) 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *     M-Ezzat - code cleanup - squid:S2131
 *******************************************************************************/
package org.eclipse.packagedrone.repo.adapter.p2.internal.aspect;

import static org.eclipse.packagedrone.repo.xml.XmlHelper.addElement;
import static org.eclipse.packagedrone.repo.xml.XmlHelper.fixSize;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.packagedrone.repo.xml.XmlHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

public abstract class AbstractRepositoryProcessor extends AbstractDocumentProcessor
{
    private final XmlHelper xml = new XmlHelper ();

    protected final Map<String, String> properties = new HashMap<> ();

    private final boolean compressed;

    private final String title;

    private final String basename;

    public AbstractRepositoryProcessor ( final String title, final String basename, final boolean compressed, final DocumentCache cache, final Map<String, String> additionalProperties )
    {
        super ( cache );

        this.title = title;
        this.compressed = compressed;
        this.basename = basename;

        this.properties.putAll ( additionalProperties );

        this.properties.put ( "p2.timestamp", Long.toString(System.currentTimeMillis ()) );

        if ( compressed )
        {
            this.properties.put ( "p2.compressed", "true" );
        }
    }

    @Override
    public String getId ()
    {
        return this.basename + ( this.compressed ? ".jar" : ".xml" );
    }

    @Override
    public String getMimeType ()
    {
        return this.compressed ? "application/zip" : "application/xml";
    }

    protected void addProperties ( final Element root )
    {
        final Element props = addElement ( root, "properties" );

        for ( final Map.Entry<String, String> entry : this.properties.entrySet () )
        {
            final Element p = addElement ( props, "property" );
            p.setAttribute ( "name", entry.getKey () );
            p.setAttribute ( "value", entry.getValue () );
        }

        fixSize ( props );
    }

    protected Document initRepository ( final String processingType, final String type )
    {
        final Document doc = this.xml.create ();

        {
            final ProcessingInstruction pi = doc.createProcessingInstruction ( processingType, "version=\"1.1.0\"" );
            doc.appendChild ( pi );
        }

        final Element root = doc.createElement ( "repository" );
        doc.appendChild ( root );
        root.setAttribute ( "name", this.title );
        root.setAttribute ( "type", type );
        root.setAttribute ( "version", "1" );

        return doc;
    }

    public void write ( final Document doc, final OutputStream stream ) throws IOException
    {
        if ( this.compressed )
        {
            final ZipOutputStream zos = new ZipOutputStream ( stream );
            zos.putNextEntry ( new ZipEntry ( this.basename + ".xml" ) );
            writeDoc ( doc, zos );
            zos.closeEntry ();
            zos.finish ();
        }
        else
        {
            writeDoc ( doc, stream );
        }
    }

    protected void writeDoc ( final Document doc, final OutputStream stream )
    {
        try
        {
            this.xml.write ( doc, stream );
        }
        catch ( final Exception e )
        {
            throw new RuntimeException ( e );
        }
    }

}
