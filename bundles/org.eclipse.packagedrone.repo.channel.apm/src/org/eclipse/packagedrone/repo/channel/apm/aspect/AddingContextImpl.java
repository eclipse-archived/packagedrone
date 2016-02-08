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
package org.eclipse.packagedrone.repo.channel.apm.aspect;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.MetaKeys;
import org.eclipse.packagedrone.repo.channel.AddingContext;
import org.eclipse.packagedrone.repo.channel.ValidationMessage;

public class AddingContextImpl extends PreAddContextImpl implements AddingContext
{
    private final Map<MetaKey, String> providedMetaData;

    private final Map<MetaKey, String> extractedMetaData;

    private final List<ValidationMessage> validationMessages;

    private Map<MetaKey, String> metaData;

    public AddingContextImpl ( final String name, final Path file, final boolean external, final Map<MetaKey, String> providedMetaData, final Map<MetaKey, String> extractedMetaData, final List<ValidationMessage> validationMessages, final AspectableContext context )
    {
        super ( name, file, external, context );
        this.providedMetaData = providedMetaData != null ? Collections.unmodifiableMap ( providedMetaData ) : Collections.emptyMap ();
        this.extractedMetaData = extractedMetaData != null ? Collections.unmodifiableMap ( extractedMetaData ) : Collections.emptyMap ();
        this.validationMessages = validationMessages != null ? Collections.unmodifiableList ( validationMessages ) : Collections.emptyList ();
    }

    @Override
    public Map<MetaKey, String> getProvidedMetaData ()
    {
        return this.providedMetaData;
    }

    @Override
    public Map<MetaKey, String> getExtractedMetaData ()
    {
        return this.extractedMetaData;
    }

    @Override
    public Map<MetaKey, String> getMetaData ()
    {
        if ( this.metaData == null )
        {
            this.metaData = MetaKeys.union ( this.providedMetaData, this.extractedMetaData );
        }
        return this.metaData;
    }

    @Override
    public List<ValidationMessage> getValidationMessages ()
    {
        return this.validationMessages;
    }

}
