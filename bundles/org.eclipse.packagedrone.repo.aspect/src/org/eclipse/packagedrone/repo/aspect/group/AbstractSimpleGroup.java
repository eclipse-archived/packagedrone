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
package org.eclipse.packagedrone.repo.aspect.group;

/**
 * A an abstract class helping to create group information services
 * <p>
 * In order to use this class:
 * <ul>
 * <li>Derive from it</li>
 * <li>Register your class with OSGi (e.g. using DS)</li>
 * <li>The first call to {@link #getInformation()} will create the information
 * object</li>
 * </ul>
 * </p>
 */
public abstract class AbstractSimpleGroup implements Group
{
    private final String id;

    private GroupInformation info;

    public AbstractSimpleGroup ( final String id )
    {
        this.id = id;
    }

    @Override
    public GroupInformation getInformation ()
    {
        if ( this.info == null )
        {
            this.info = new GroupInformation () {

                @Override
                public String getId ()
                {
                    return AbstractSimpleGroup.this.id;
                }

                @Override
                public String getName ()
                {
                    return AbstractSimpleGroup.this.getName ();
                }
            };
        }
        return this.info;
    }

    protected abstract String getName ();
}
