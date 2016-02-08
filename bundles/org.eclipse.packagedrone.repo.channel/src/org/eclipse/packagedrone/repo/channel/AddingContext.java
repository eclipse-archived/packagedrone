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
package org.eclipse.packagedrone.repo.channel;

import java.util.List;
import java.util.Map;

import org.eclipse.packagedrone.repo.MetaKey;

public interface AddingContext extends PreAddContext
{
    public Map<MetaKey, String> getProvidedMetaData ();

    public Map<MetaKey, String> getExtractedMetaData ();

    public Map<MetaKey, String> getMetaData ();

    public List<ValidationMessage> getValidationMessages ();
}
