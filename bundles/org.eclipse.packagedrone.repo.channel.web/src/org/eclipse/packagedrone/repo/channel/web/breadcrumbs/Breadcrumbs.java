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

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.packagedrone.web.LinkTarget;

import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;

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

    public static class Builder
    {
        private final List<Entry> entries = new LinkedList<> ();

        public Builder ()
        {
        }

        public Builder add ( final Entry entry )
        {
            this.entries.add ( entry );
            return this;
        }

        public Builder add ( final String label )
        {
            this.entries.add ( new Entry ( label ) );
            return this;
        }

        public Builder add ( final String label, final String target )
        {
            this.entries.add ( new Entry ( label, target ) );
            return this;
        }

        public Builder add ( final String label, final String targetPattern, final String... pathSegments )
        {
            Objects.requireNonNull ( targetPattern );
            Objects.requireNonNull ( pathSegments );

            final Escaper esc = UrlEscapers.urlPathSegmentEscaper ();

            final Object[] encoded = new String[pathSegments.length];
            for ( int i = 0; i < pathSegments.length; i++ )
            {
                encoded[i] = esc.escape ( pathSegments[i] );
            }

            this.entries.add ( new Entry ( label, MessageFormat.format ( targetPattern, encoded ) ) );
            return this;
        }

        public Breadcrumbs build ()
        {
            return new Breadcrumbs ( this.entries );
        }

        public void buildTo ( final Map<String, Object> model )
        {
            model.put ( "breadcrumbs", build () );
        }
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
