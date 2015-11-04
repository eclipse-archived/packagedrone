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
package org.eclipse.packagedrone.repo.aspect.group;

/**
 * A service which can be registered for groups
 * <p>
 * A group is currently only used for grouping channel aspects in the UI.
 * However this "service" allows to provide the group information from a single
 * point. The channel aspects references the group id and for each group there
 * has to be an instance of Group registered with OSGi, providing the necessary
 * information.
 * </p>
 */
public interface Group
{
    public GroupInformation getInformation ();
}
