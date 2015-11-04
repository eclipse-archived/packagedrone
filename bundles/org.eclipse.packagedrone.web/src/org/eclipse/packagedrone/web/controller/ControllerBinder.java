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
package org.eclipse.packagedrone.web.controller;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.packagedrone.web.controller.binding.Binder;

@Retention ( RetentionPolicy.RUNTIME )
@Target ( ElementType.METHOD )
@Repeatable ( ControllerBinders.class )
public @interface ControllerBinder
{
    /**
     * The class implementing the binder
     * <p>
     * The binder class must have a zero argument constructor
     * </p>
     *
     * @return the class of the binder
     */
    Class<? extends Binder>value ();

    ControllerBinderParameter[]parameters () default {};
}
