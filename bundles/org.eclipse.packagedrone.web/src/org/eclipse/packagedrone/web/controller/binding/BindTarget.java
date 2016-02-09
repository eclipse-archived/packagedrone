/*******************************************************************************
 * Copyright (c) 2014, 2016 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.web.controller.binding;

import java.lang.annotation.Annotation;
import java.util.Collection;

public interface BindTarget
{
    public Class<?> getType ();

    public <T extends Annotation> T getAnnotation ( Class<T> clazz );

    public <T extends Annotation> Collection<T> getAnnotationsByType ( Class<T> annotationClass );

    public void bind ( Binding binding );

    public String getQualifier ();

    public boolean isAnnotationPresent ( Class<? extends Annotation> clazz );
}
