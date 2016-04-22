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
package org.eclipse.packagedrone.repo.manage.system.web;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.packagedrone.utils.AttributedValue;
import org.eclipse.packagedrone.utils.Headers;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;
import org.osgi.framework.wiring.BundleRevision;

public class BundleInformation
{
    private final Bundle bundle;

    private final BundleRevision bundleRevision;

    private final URL aboutHtml;

    private final List<LicenseInformation> licenses;

    private final URL licenseTxt;

    private final URL noticeTxt;

    public static class LicenseInformation
    {
        private final String license;

        private final URL url;

        public LicenseInformation ( final String license, final URL url )
        {
            this.license = license;
            this.url = url;
        }

        public String getLicense ()
        {
            return this.license;
        }

        public URL getUrl ()
        {
            return this.url;
        }

        public static List<LicenseInformation> parse ( final String value )
        {
            if ( value == null )
            {
                return new LinkedList<> (); // return mutable list
            }

            final List<LicenseInformation> result = new LinkedList<> ();

            final List<AttributedValue> lics = Headers.parseList ( value );
            for ( final AttributedValue av : lics )
            {
                URL url = null;
                String label = null;
                final String v = av.getValue ();
                try
                {
                    url = new URL ( v );
                    label = "License";
                }
                catch ( final MalformedURLException e )
                {
                    label = v;
                }

                result.add ( new LicenseInformation ( label, url ) );
            }

            return result;
        }
    }

    public BundleInformation ( final Bundle bundle )
    {
        this.bundle = bundle;
        this.bundleRevision = bundle.adapt ( BundleRevision.class );

        this.aboutHtml = bundle.getEntry ( "about.html" );
        this.licenseTxt = bundle.getEntry ( "META-INF/LICENSE.txt" );
        this.noticeTxt = bundle.getEntry ( "META-INF/NOTICE.txt" );

        this.licenses = LicenseInformation.parse ( bundle.getHeaders ().get ( Constants.BUNDLE_LICENSE ) );
    }

    public int getState ()
    {
        return this.bundle.getState ();
    }

    public long getBundleId ()
    {
        return this.bundle.getBundleId ();
    }

    public String getSymbolicName ()
    {
        return this.bundle.getSymbolicName ();
    }

    public Version getVersion ()
    {
        return this.bundle.getVersion ();
    }

    public BundleRevision getBundleRevision ()
    {
        return this.bundleRevision;
    }

    public String getName ()
    {
        return this.bundle.getHeaders ( null ).get ( Constants.BUNDLE_NAME );
    }

    public boolean isFragment ()
    {
        return ( this.bundleRevision.getTypes () & BundleRevision.TYPE_FRAGMENT ) > 0;
    }

    public URL getAboutHtml ()
    {
        return this.aboutHtml;
    }

    public URL getLicenseTxt ()
    {
        return this.licenseTxt;
    }

    public URL getNoticeTxt ()
    {
        return this.noticeTxt;
    }

    public List<LicenseInformation> getLicenses ()
    {
        return this.licenses;
    }
}
