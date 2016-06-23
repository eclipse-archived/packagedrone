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
package org.eclipse.packagedrone.repo.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel ( description = "Generic error information" )
public class ErrorInformation
{
    private String code;

    private String message;

    private String description;

    @ApiModelProperty ( "An error code" )
    public String getCode ()
    {
        return this.code;
    }

    public void setCode ( final String code )
    {
        this.code = code;
    }

    @ApiModelProperty ( "An error message" )
    public String getMessage ()
    {
        return this.message;
    }

    public void setMessage ( final String message )
    {
        this.message = message;
    }

    @ApiModelProperty ( required = false, value = "A longer description" )
    public String getDescription ()
    {
        return this.description;
    }

    public void setDescription ( final String description )
    {
        this.description = description;
    }
}
