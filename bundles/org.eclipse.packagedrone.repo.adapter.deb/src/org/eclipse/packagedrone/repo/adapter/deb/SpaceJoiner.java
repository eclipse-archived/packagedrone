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
package org.eclipse.packagedrone.repo.adapter.deb;

import java.util.Arrays;
import java.util.Collection;
import java.util.TreeSet;

import org.eclipse.packagedrone.repo.BindingConverter;
import org.eclipse.scada.utils.str.StringHelper;

public class SpaceJoiner implements BindingConverter
{

    @Override
    public String encode ( final Object value )
    {
        return StringHelper.join ( (Collection<?>)value, " " );
    }

    @Override
    public Object decode ( final String string )
    {
        return new TreeSet<> ( Arrays.asList ( string.split ( "\\s+" ) ) );
    }

}
