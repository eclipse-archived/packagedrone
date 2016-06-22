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

public class SecurityServiceStorageHandler extends AbstractSimplerGsonStorageModelProvider<UserProfileStorage, ModifiableUserProfileStorage, AccessTokenModel>
{
    public SecurityServiceStorageHandler ()
    {
        super ( UserProfileStorage.class, ModifiableUserProfileStorage.class, AccessTokenModel.class, "accessTokens.json", ModifiableUserProfileStorage::new, ModifiableUserProfileStorage::new );
    }

    @Override
    protected AccessTokenModel toGsonModel ( final ModifiableUserProfileStorage writeModel )
    {
        final AccessTokenModel model = new AccessTokenModel ();
        model.setTokens ( writeModel.list () );
        return model;
    }

    @Override
    protected ModifiableUserProfileStorage fromGsonModel ( final AccessTokenModel gsonModel )
    {
        return new ModifiableUserProfileStorage ( gsonModel.getTokens () );
    }
}
