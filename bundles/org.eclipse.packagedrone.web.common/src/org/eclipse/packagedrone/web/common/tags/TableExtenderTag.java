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
package org.eclipse.packagedrone.web.common.tags;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.DynamicAttributes;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

import org.eclipse.packagedrone.web.common.table.TableExtension;
import org.eclipse.packagedrone.web.common.table.TableExtensionManagerRequest;

public class TableExtenderTag extends TagSupport implements DynamicAttributes
{
    private static final long serialVersionUID = 1L;

    private String tags;

    private TableExtension extension;

    private final Map<String, Object> context = new HashMap<> ();

    public void setTags ( final String tags )
    {
        this.tags = tags;
    }

    @Override
    public void setDynamicAttribute ( final String uri, final String name, final Object value ) throws JspException
    {
        this.context.put ( name, value );
    }

    @Override
    public int doStartTag () throws JspException
    {
        final TableExtensionManagerRequest mgr = (TableExtensionManagerRequest)this.pageContext.getRequest ().getAttribute ( TableExtensionManagerRequest.PROPERTY_NAME );
        this.extension = mgr.createExtensions ( getId (), getTagSet (), this.context );

        return Tag.EVAL_BODY_INCLUDE;
    }

    public TableExtension getExtension ()
    {
        return this.extension;
    }

    private Set<String> getTagSet ()
    {
        if ( this.tags == null || this.tags.isEmpty () )
        {
            return Collections.emptySet ();
        }

        return new HashSet<> ( Arrays.asList ( this.tags.split ( "[ ,]+" ) ) );
    }
}
