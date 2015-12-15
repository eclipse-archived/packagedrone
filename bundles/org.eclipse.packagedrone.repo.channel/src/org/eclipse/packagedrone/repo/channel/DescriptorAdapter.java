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
package org.eclipse.packagedrone.repo.channel;

import java.util.Collection;
import java.util.Set;

public interface DescriptorAdapter
{
    public ChannelId getDescriptor ();

    public void setDescription ( final String description );

    public void addName ( String name );

    public void removeName ( String name );

    public default Set<String> getNames ()
    {
        return getDescriptor ().getNames ();
    }

    public void setNames ( Collection<String> names );
}
