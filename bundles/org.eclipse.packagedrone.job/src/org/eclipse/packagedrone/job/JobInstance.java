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
package org.eclipse.packagedrone.job;

@FunctionalInterface
public interface JobInstance
{
    public static interface Context
    {
        public void beginWork ( String label, long amount );

        public void setCurrentTaskName ( String name );

        public void worked ( long amount );

        public void complete ();

        public void setResult ( String data );
    }

    public void run ( Context context ) throws Exception;
}
