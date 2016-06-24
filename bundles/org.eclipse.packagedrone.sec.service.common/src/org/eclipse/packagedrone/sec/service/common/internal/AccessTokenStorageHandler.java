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

import org.eclipse.packagedrone.storage.apm.AbstractSimplerGsonStorageModelProvider;

public class AccessTokenStorageHandler extends AbstractSimplerGsonStorageModelProvider<AccessTokenStorage, ModifiableAccessTokenStorage, AccessTokenModel>
{
    public AccessTokenStorageHandler ()
    {
        super ( AccessTokenStorage.class, ModifiableAccessTokenStorage.class, AccessTokenModel.class, "accessTokens.json", ModifiableAccessTokenStorage::new, ModifiableAccessTokenStorage::new );
    }

    @Override
    protected AccessTokenModel toGsonModel ( final ModifiableAccessTokenStorage writeModel )
    {
        final AccessTokenModel model = new AccessTokenModel ();
        model.setTokens ( writeModel.list () );
        return model;
    }

    @Override
    protected ModifiableAccessTokenStorage fromGsonModel ( final AccessTokenModel gsonModel )
    {
        return new ModifiableAccessTokenStorage ( gsonModel.getTokens () );
    }
}
