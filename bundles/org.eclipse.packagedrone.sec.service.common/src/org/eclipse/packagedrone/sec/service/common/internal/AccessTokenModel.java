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
package org.eclipse.packagedrone.sec.service.common.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.packagedrone.sec.service.AccessToken;

public class AccessTokenModel
{
    private List<AccessToken> tokens = new ArrayList<> ();

    public void setTokens ( final List<AccessToken> tokens )
    {
        this.tokens = tokens;
    }

    public List<AccessToken> getTokens ()
    {
        return this.tokens;
    }
}
