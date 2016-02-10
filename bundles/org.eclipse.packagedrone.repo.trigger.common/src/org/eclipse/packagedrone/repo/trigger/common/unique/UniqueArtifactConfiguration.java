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
package org.eclipse.packagedrone.repo.trigger.common.unique;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.channel.VetoPolicy;
import org.eclipse.packagedrone.repo.gson.MetaKeyTypeAdapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class UniqueArtifactConfiguration
{
    private static final VetoPolicy DEFAULT_VETO_POLICY = VetoPolicy.REJECT;

    @Size ( min = 1, message = "Field must not be empty" )
    private MetaKey[] keys = new MetaKey[0];

    @NotNull ( message = "Field must not be empty" )
    private MetaKey uniqueAttribute;

    private VetoPolicy vetoPolicy = DEFAULT_VETO_POLICY;

    public void setKeys ( final MetaKey[] keys )
    {
        this.keys = keys;
    }

    public MetaKey[] getKeys ()
    {
        return this.keys;
    }

    public void setUniqueAttribute ( final MetaKey uniqueAttribute )
    {
        this.uniqueAttribute = uniqueAttribute;
    }

    public MetaKey getUniqueAttribute ()
    {
        return this.uniqueAttribute;
    }

    public void setVetoPolicy ( final VetoPolicy vetoPolicy )
    {
        this.vetoPolicy = vetoPolicy != null ? vetoPolicy : DEFAULT_VETO_POLICY;
    }

    public VetoPolicy getVetoPolicy ()
    {
        return this.vetoPolicy;
    }

    private static Gson createGson ()
    {
        return new GsonBuilder ().registerTypeAdapter ( MetaKey.class, MetaKeyTypeAdapter.INSTANCE ).create ();
    }

    public static UniqueArtifactConfiguration fromJson ( final String json )
    {
        if ( json == null )
        {
            return null;
        }
        return createGson ().fromJson ( json, UniqueArtifactConfiguration.class );
    }

    public String toJson ()
    {
        return createGson ().toJson ( this );
    }
}
