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
package org.eclipse.packagedrone.repo.generator.p2.xml;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.packagedrone.repo.generator.p2.xml.CategoryDefinition.Category;
import org.eclipse.packagedrone.repo.xml.XmlHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CategoryXmlParser
{
    public static CategoryDefinition parse ( final Document doc )
    {
        final Element root = doc.getDocumentElement ();
        if ( !"site".equals ( root.getNodeName () ) )
        {
            return null;
        }

        final List<Category> cats = new LinkedList<> ();

        for ( final Element ele : XmlHelper.iterElement ( root, "category-def" ) )
        {
            final String id = ele.getAttribute ( "name" );
            final String label = ele.getAttribute ( "label" );
            final String description = XmlHelper.getText ( ele, "description" );

            if ( id != null && !id.isEmpty () )
            {
                cats.add ( new Category ( id, label, description ) );
            }
        }

        final Map<String, Set<String>> bundles = new HashMap<> ();
        gatherElements ( root, "bundle", bundles );

        final Map<String, Set<String>> features = new HashMap<> ();
        gatherElements ( root, "feature", features );

        return new CategoryDefinition ( cats, bundles, features );
    }

    protected static void gatherElements ( final Element root, final String elementName, final Map<String, Set<String>> set )
    {
        for ( final Element ele : XmlHelper.iterElement ( root, elementName ) )
        {
            final String id = ele.getAttribute ( "id" );
            if ( id.isEmpty () )
            {
                continue;
            }

            final Set<String> eleCats = new HashSet<> ();
            for ( final Element childEle : XmlHelper.iterElement ( ele, "category" ) )
            {
                final String name = childEle.getAttribute ( "name" );
                if ( !name.isEmpty () )
                {
                    eleCats.add ( name );
                }
            }
            set.put ( id, eleCats );
        }
    }
}
