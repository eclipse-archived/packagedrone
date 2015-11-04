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
package org.eclipse.packagedrone.web.controller.binding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.packagedrone.utils.converter.ConverterManager;

public interface Binder
{
    /**
     * Marks a method of the binder as initializer
     * <p>
     * Before calling
     * {@link Binder#performBind(BindTarget, ConverterManager, BindingManager)}
     * the binding manager will call methods marked with {@link Initializer}
     * with the current binding manager state.
     * </p>
     * <p>
     * <em>Note:</em> The initializer methods will get called when adding the
     * binder to the manager. It is not guaranteed that the method
     * {@link Binder#performBind(BindTarget, ConverterManager, BindingManager)}
     * will be called afterwards.
     * </p>
     */
    @Retention ( RetentionPolicy.RUNTIME )
    @Target ( ElementType.METHOD )
    public @interface Initializer
    {
    }

    public Binding performBind ( BindTarget target, ConverterManager converter, BindingManager bindingManager );
}
