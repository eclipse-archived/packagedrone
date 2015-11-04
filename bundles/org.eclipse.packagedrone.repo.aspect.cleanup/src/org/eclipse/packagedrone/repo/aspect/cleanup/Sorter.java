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

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.eclipse.packagedrone.utils.converter.JSON;

import com.google.gson.GsonBuilder;

@JSON
public class Sorter
{
    private List<Field> fields = new LinkedList<> ();

    public void setFields ( final List<Field> fields )
    {
        this.fields = fields;
    }

    public List<Field> getFields ()
    {
        return this.fields;
    }

    public Comparator<ArtifactInformation> makeComparator ()
    {
        return new Comparator<ArtifactInformation> () {

            @Override
            public int compare ( final ArtifactInformation o1, final ArtifactInformation o2 )
            {
                for ( final Field field : Sorter.this.fields )
                {
                    final String v1 = o1.getMetaData ().get ( field.getKey () );
                    final String v2 = o2.getMetaData ().get ( field.getKey () );

                    final int rc = field.compare ( v1, v2 );
                    if ( rc != 0 )
                    {
                        return rc;
                    }
                }

                return 0;
            }
        };
    }

    @Override
    public String toString ()
    {
        return new GsonBuilder ().create ().toJson ( this );
    }
}
