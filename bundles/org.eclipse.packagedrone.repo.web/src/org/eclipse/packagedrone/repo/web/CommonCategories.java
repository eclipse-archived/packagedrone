/*******************************************************************************
 * Copyright (c) 2016 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.web;

public enum CommonCategories
{
    EDIT ( "Edit", 150 );

    private String label;

    private int priority;

    private CommonCategories ( final String label, final int priority )
    {
        this.label = label;
        this.priority = priority;
    }

    public String getLabel ()
    {
        return this.label;
    }

    public int getPriority ()
    {
        return this.priority;
    }
}
