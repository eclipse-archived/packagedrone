/*******************************************************************************
 * Copyright (c) 2014 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention ( RetentionPolicy.RUNTIME )
@Inherited
@Target ( ElementType.TYPE )
/**
 * This marks a class as controller for the web framework
 * <p>
 * Simply adding this annotation is not enough. The class must also be
 * registered as an OSGi service. If the class does not implement any interfaces,
 * then the class itself can be used for registering it with OSGi.
 * </p>
 */
public @interface Controller
{
}
