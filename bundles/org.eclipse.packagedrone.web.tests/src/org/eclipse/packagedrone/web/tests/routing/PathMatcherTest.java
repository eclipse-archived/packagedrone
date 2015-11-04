/*******************************************************************************
 * Copyright (c) 2014 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.web.tests.routing;

import java.util.Collections;
import java.util.Map;

import org.eclipse.packagedrone.web.controller.routing.RequestMappingInformation;
import org.eclipse.packagedrone.web.controller.routing.RequestMappingInformation.Match;
import org.junit.Assert;
import org.junit.Test;

public class PathMatcherTest
{
    @Test
    public void testSimpleMatch ()
    {
        final RequestMappingInformation rmi = new RequestMappingInformation ( "/", "GET" );
        assertResult ( rmi.matches ( "/", "GET" ), Collections.<String, String> emptyMap () );
    }

    @Test
    public void testBugMatch ()
    {
        final RequestMappingInformation rmi = new RequestMappingInformation ( "/setup/databaseUpgrade", "GET" );
        Assert.assertNull ( rmi.matches ( "/setup", "GET" ) );
    }

    @Test
    public void testNoMatch ()
    {
        final RequestMappingInformation rmi = new RequestMappingInformation ( "/", "GET" );
        Assert.assertNull ( rmi.matches ( "/path", "GET" ) );
    }

    @Test
    public void testWithAttributes ()
    {
        final RequestMappingInformation rmi = new RequestMappingInformation ( "/path/{id}/get", "GET" );
        final Map<String, String> attr = Collections.singletonMap ( "id", "123" );
        assertResult ( rmi.matches ( "/path/123/get", "GET" ), attr );
    }

    @Test
    public void testWithAttributesNoMatch ()
    {
        final RequestMappingInformation rmi = new RequestMappingInformation ( "/path/{id}/get", "GET" );
        Assert.assertNull ( rmi.matches ( "/path/123/post", "GET" ) );
    }

    private void assertResult ( final Match matches, final Map<String, String> attributes )
    {
        Assert.assertNotNull ( matches );
        Assert.assertEquals ( attributes.size (), matches.getAttributes ().size () );

        for ( final Map.Entry<String, String> entry : attributes.entrySet () )
        {
            Assert.assertEquals ( entry.getValue (), matches.getAttributes ().get ( entry.getKey () ) );
        }
    }
}
