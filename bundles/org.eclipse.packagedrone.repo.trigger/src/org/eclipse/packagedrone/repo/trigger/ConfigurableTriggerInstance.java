/*******************************************************************************
 * Copyright (c) 2015 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.trigger;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;

public interface ConfigurableTriggerInstance extends TriggerInstance
{
    /**
     * Get an unmodifiable configuration map
     *
     * @return the unmodifiable configuration
     */
    public @NonNull Map<String, String> getConfiguration ();

    public void configure ( @NonNull Map<String, String> configuration );

    public void delete ();
}
