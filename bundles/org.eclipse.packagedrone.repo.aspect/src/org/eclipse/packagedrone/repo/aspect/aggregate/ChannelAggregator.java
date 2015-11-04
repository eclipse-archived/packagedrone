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
package org.eclipse.packagedrone.repo.aspect.aggregate;

import java.util.Map;

/**
 * A channel aggregator <br/>
 * <p>
 * A channel aggregator is run after the channel was somehow modified. Either
 * through an artifact operation, or by modifying the channels provided meta
 * data.
 * </p>
 * <p>
 * The channel aggregator can provide meta data which is then stored as
 * <q>channel extracted meta data</q>.
 * </p>
 * <p>
 * <em>Note:</em> Artifacts must not depend on channel meta data!
 * </p>
 */
public interface ChannelAggregator
{
    public Map<String, String> aggregateMetaData ( AggregationContext context ) throws Exception;
}
