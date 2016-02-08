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
package org.eclipse.packagedrone.repo.channel.search;

import java.util.Collection;

public class Or extends AbstractCompositePredicate
{
    Or ( final Collection<Predicate> predicates )
    {
        super ( predicates );
    }

    Or ( final Predicate... predicates )
    {
        super ( predicates );
    }
}
