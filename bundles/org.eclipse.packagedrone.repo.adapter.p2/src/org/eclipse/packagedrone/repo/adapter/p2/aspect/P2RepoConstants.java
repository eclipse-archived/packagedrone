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
package org.eclipse.packagedrone.repo.adapter.p2.aspect;

import org.eclipse.packagedrone.repo.MetaKey;

public interface P2RepoConstants
{
    public static final MetaKey KEY_FRAGMENT_COUNT = new MetaKey ( P2RepositoryAspect.ID, "fragment-count" );

    public static final MetaKey KEY_FRAGMENT_DATA = new MetaKey ( P2RepositoryAspect.ID, "fragment-data" );

    public static final MetaKey KEY_FRAGMENT_KEYS = new MetaKey ( P2RepositoryAspect.ID, "fragment-keys" );

    public static final MetaKey KEY_FRAGMENT_MD5 = new MetaKey ( P2RepositoryAspect.ID, "fragment-md5" );

    public static final MetaKey KEY_FRAGMENT_TYPE = new MetaKey ( P2RepositoryAspect.ID, "fragment-type" );

    public static final MetaKey KEY_REPO_TITLE = new MetaKey ( "p2.repo", "title" );
}
