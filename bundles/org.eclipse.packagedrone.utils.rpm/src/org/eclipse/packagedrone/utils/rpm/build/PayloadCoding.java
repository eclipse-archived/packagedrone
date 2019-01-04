/*******************************************************************************
 * Copyright (c) 2018 Red Hat Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.utils.rpm.build;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.packagedrone.utils.rpm.deps.Dependency;

public interface PayloadCoding
{
    String getCoding ();

    void fillRequirements ( final Consumer<Dependency> requirementsConsumer );

    default List<Dependency> getRequirements ()
    {
        final List<Dependency> result = new LinkedList<> ();
        fillRequirements ( result::add );
        return result;
    }

    InputStream createInputStream ( final InputStream in ) throws IOException;

    OutputStream createOutputStream ( final OutputStream out, final Optional<String> optionalFlags ) throws IOException;

}
