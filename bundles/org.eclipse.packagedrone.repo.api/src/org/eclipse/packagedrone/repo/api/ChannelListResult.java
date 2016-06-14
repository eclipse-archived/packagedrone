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

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class ChannelListResult
{
    private List<ChannelInformation> channels;

    @ApiModelProperty
    public List<ChannelInformation> getChannels ()
    {
        return this.channels;
    }

    public void setChannels ( final List<ChannelInformation> channels )
    {
        this.channels = channels;
    }
}
