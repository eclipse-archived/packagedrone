/*******************************************************************************
 * Copyright (c) 2014, 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.channel.web.breadcrumbs;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.packagedrone.web.LinkTarget;

public class Breadcrumbs
{
    public static class Entry
    {
        private final String label;

        private final String target;

        public Entry ( final String label, final String target )
        {
            this.label = label;
            this.target = target;
        }

        public Entry ( final String label )
        {
            this.label = label;
            this.target = null;
        }

        public String getLabel ()
        {
            return this.label;
        }

        public String getTarget ()
        {
            return this.target;
        }

        public boolean isLink ()
        {
            return this.target != null && !this.target.isEmpty ();
        }
    }

    private final List<Entry> entries;

    public Breadcrumbs ( final List<Entry> entries )
    {
        this.entries = Collections.unmodifiableList ( entries );
    }

    public Breadcrumbs ( final Entry... entries )
    {
        this.entries = Arrays.asList ( entries );
    }

    public List<Entry> getEntries ()
    {
        return this.entries;
    }

    public static Entry create ( final String label, final Class<?> controllerClazz, final String methodName )
    {
        return new Entry ( label, LinkTarget.createFromController ( controllerClazz, methodName ).getUrl () );
    }

    public static Entry create ( final String label, final Class<?> controllerClazz, final String methodName, final Map<String, ?> model )
    {
        return new Entry ( label, LinkTarget.createFromController ( controllerClazz, methodName ).expand ( model ).getUrl () );
    }

    public static Entry create ( final String label, final Class<?> controllerClazz, final String methodName, final String key, final Object value )
    {
        return new Entry ( label, LinkTarget.createFromController ( controllerClazz, methodName ).expand ( Collections.singletonMap ( key, value ) ).getUrl () );
    }
}
