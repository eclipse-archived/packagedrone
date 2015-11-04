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
package org.eclipse.packagedrone.storage.apm;

import java.io.Serializable;

public class MockStorageModel implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String value;

    public MockStorageModel ( final String value )
    {
        this.value = value;
    }

    public MockStorageModel ( final MockStorageModel model )
    {
        this.value = model.value;
    }

    public String getValue ()
    {
        return this.value;
    }

    public void setValue ( final String value )
    {
        this.value = value;
    }
}
