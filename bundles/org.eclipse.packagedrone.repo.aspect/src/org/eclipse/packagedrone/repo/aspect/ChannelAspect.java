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
package org.eclipse.packagedrone.repo.aspect;

import org.eclipse.packagedrone.repo.aspect.aggregate.ChannelAggregator;
import org.eclipse.packagedrone.repo.aspect.extract.Extractor;
import org.eclipse.packagedrone.repo.aspect.listener.ChannelListener;
import org.eclipse.packagedrone.repo.aspect.virtual.Virtualizer;

public interface ChannelAspect
{
    /**
     * Get the factory id
     *
     * @return the factory id
     */
    public String getId ();

    /**
     * @return an extractor or <code>null</code>
     */
    public default Extractor getExtractor ()
    {
        return null;
    }

    /**
     * @deprecated Use the trigger system instead
     * @return a channel listener or <code>null</code>
     */
    @Deprecated
    public default ChannelListener getChannelListener ()
    {
        return null;
    }

    /**
     * @return a virtualizer or <code>null</code>
     */
    public default Virtualizer getArtifactVirtualizer ()
    {
        return null;
    }

    /**
     * @return an aggregator which works on the whole channel, or
     *         <code>null</code>
     */
    public default ChannelAggregator getChannelAggregator ()
    {
        return null;
    }

}
