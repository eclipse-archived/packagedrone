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
package org.eclipse.packagedrone.repo.adapter.deb.aspect.internal;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.packagedrone.repo.aspect.Constants;
import org.eclipse.packagedrone.repo.aspect.extract.Extractor;
import org.eclipse.packagedrone.utils.deb.Packages;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class DebianExtractor implements Extractor
{
    private final GsonBuilder builder;

    public DebianExtractor ()
    {
        this.builder = new GsonBuilder ();
    }

    @Override
    public void extractMetaData ( final Extractor.Context context, final Map<String, String> metadata ) throws Exception
    {
        final SortedMap<String, String> controlFile;
        try
        {
            final Map<String, String> result = Packages.parseControlFile ( context.getPath ().toFile () );
            controlFile = result != null ? new TreeMap<> ( result ) : null;
        }
        catch ( final Exception e )
        {
            return;
        }

        if ( controlFile == null )
        {
            // no control file found
            return;
        }

        metadata.put ( "package", controlFile.get ( "Package" ) );
        metadata.put ( "version", controlFile.get ( "Version" ) );
        metadata.put ( "maintainer", controlFile.get ( "Maintainer" ) );
        metadata.put ( "description", controlFile.get ( "Description" ) );
        metadata.put ( "shortDescription", makeShort ( controlFile.get ( "Description" ) ) );
        metadata.put ( "architecture", controlFile.get ( "Architecture" ) );
        metadata.put ( "section", controlFile.get ( "Section" ) );

        metadata.put ( Constants.KEY_ARTIFACT_LABEL, "Debian Package" );

        final Gson gson = this.builder.create ();

        metadata.put ( "control.json", gson.toJson ( new ControlInformation ( controlFile ) ) );
    }

    private String makeShort ( final String string )
    {
        if ( string == null )
        {
            return null;
        }

        final String toks[] = string.split ( "\n", 2 );
        if ( toks.length > 0 )
        {
            return toks[0];
        }
        return null;
    }
}
