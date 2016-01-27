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
package org.eclipse.packagedrone.repo.trigger;

import java.io.PrintWriter;

public interface Processor
{
    /**
     * Perform the actual processing
     *
     * @param context
     *            The context to work on
     */
    public void process ( Object context );

    /**
     * Stream the actual state as HTML
     * <p>
     * This method should stream a short snippet of HTML out to writer about how
     * the processor is configured. The intended use case is for the triggers
     * view to give a short summary without needing to make further calls.
     * </p>
     * <p>
     * The output should really be a short snippet. I will be rendered inside a
     * <code>&lt;div&gt;</code> element and may use bootstrap components.
     * </p>
     * <p>
     * <strong>Note:</strong> The implementor has to ensure that all values
     * rendered are either valid HTML or is being escaped.
     * </p>
     *
     * @param writer
     *            the writer to stream to
     */
    public default void streamHtmlState ( final PrintWriter writer )
    {
    }
}
