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
package org.eclipse.packagedrone.repo.aspect.cleanup;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.utils.converter.JSON;

@JSON
public class Aggregator
{
    private List<MetaKey> fields = new LinkedList<> ();

    public List<MetaKey> getFields ()
    {
        return this.fields;
    }

    public void setFields ( final List<MetaKey> fields )
    {
        this.fields = fields;
    }

    public List<String> makeKey ( final Map<MetaKey, String> metaData )
    {
        final List<String> result = new ArrayList<> ( this.fields.size () );

        for ( final MetaKey field : this.fields )
        {
            result.add ( metaData.get ( field ) );
        }

        return result;
    }

}
