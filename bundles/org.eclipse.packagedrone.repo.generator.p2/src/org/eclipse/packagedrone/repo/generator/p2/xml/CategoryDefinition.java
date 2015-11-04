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
package org.eclipse.packagedrone.repo.generator.p2.xml;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class CategoryDefinition
{
    public static class Category
    {
        private final String label;

        private final String description;

        private final String id;

        public Category ( final String id, final String label, final String description )
        {
            this.id = id;
            this.label = label;
            this.description = description;
        }

        public String getDescription ()
        {
            return this.description;
        }

        public String getLabel ()
        {
            return this.label;
        }

        public String getId ()
        {
            return this.id;
        }
    }

    private final List<Category> categories;

    private final Map<String, Set<String>> bundles;

    private final Map<String, Set<String>> features;

    public CategoryDefinition ( final List<Category> categories, final Map<String, Set<String>> bundles, final Map<String, Set<String>> features )
    {
        this.categories = categories;
        this.features = features;
        this.bundles = bundles;
    }

    public List<Category> getCategories ()
    {
        return this.categories;
    }

    public Map<String, Set<String>> getBundles ()
    {
        return this.bundles;
    }

    public Map<String, Set<String>> getFeatures ()
    {
        return this.features;
    }
}
