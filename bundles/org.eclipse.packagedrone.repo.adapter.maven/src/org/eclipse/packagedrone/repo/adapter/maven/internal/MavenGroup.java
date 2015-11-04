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
package org.eclipse.packagedrone.repo.adapter.maven.internal;

import org.eclipse.packagedrone.repo.aspect.group.AbstractSimpleGroup;

public class MavenGroup extends AbstractSimpleGroup
{

    public MavenGroup ()
    {
        super ( "maven" );
    }

    @Override
    protected String getName ()
    {
        return "Maven";
    }

}
