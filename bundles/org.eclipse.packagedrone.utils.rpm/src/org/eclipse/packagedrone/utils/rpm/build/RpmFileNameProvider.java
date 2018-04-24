/*******************************************************************************
 * Copyright (c) 2018 Yariv Amar
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Yariv Amar - initial API
 *******************************************************************************/
package org.eclipse.packagedrone.utils.rpm.build;

@FunctionalInterface
public interface RpmFileNameProvider
{
    public String getRpmFileName ( final RpmBuilder rpmBuilder );

}
