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
package org.eclipse.packagedrone.repo.channel.search;

public class SearchOptions
{
    public static final SearchOptions DEFAULT_OPTIONS = new SearchOptions ();

    private long limit = -1;

    private long skip = 0;

    SearchOptions ()
    {
    }

    SearchOptions ( final SearchOptions other )
    {
    }

    public long getLimit ()
    {
        return this.limit;
    }

    public long getSkip ()
    {
        return this.skip;
    }

    public static class Builder
    {
        private final SearchOptions options = new SearchOptions ();

        public Builder skip ( final long skip )
        {
            this.options.skip = skip;
            return this;
        }

        public Builder limit ( final long limit )
        {
            this.options.limit = limit;
            return this;
        }

        public SearchOptions build ()
        {
            return new SearchOptions ( this.options );
        }
    }
}
