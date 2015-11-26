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
package org.eclipse.packagedrone.repo.channel;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChannelState implements Validated
{
    private String description;

    private long numberOfArtifacts;

    private long numberOfBytes;

    private boolean locked;

    private Instant creationTimestamp;

    private Instant modificationTimestamp;

    private List<ValidationMessage> messages = Collections.emptyList ();

    private ChannelState ()
    {
    }

    private ChannelState ( final ChannelState other )
    {
        this.messages = other.messages;
        this.description = other.description;
        this.numberOfArtifacts = other.numberOfArtifacts;
        this.numberOfBytes = other.numberOfBytes;
        this.locked = other.locked;
        this.creationTimestamp = other.creationTimestamp;
        this.modificationTimestamp = other.modificationTimestamp;
    }

    public String getDescription ()
    {
        return this.description;
    }

    public long getNumberOfArtifacts ()
    {
        return this.numberOfArtifacts;
    }

    public long getNumberOfBytes ()
    {
        return this.numberOfBytes;
    }

    public boolean isLocked ()
    {
        return this.locked;
    }

    public Instant getCreationTimestamp ()
    {
        return this.creationTimestamp;
    }

    public Instant getModificationTimestamp ()
    {
        return this.modificationTimestamp;
    }

    @Override
    public Collection<ValidationMessage> getValidationMessages ()
    {
        return this.messages;
    }

    public static class Builder
    {
        private ChannelState value;

        private boolean needFork;

        public Builder ()
        {
            this.value = new ChannelState ();
            this.value.creationTimestamp = this.value.modificationTimestamp = Instant.now ();
        }

        public Builder ( final ChannelState other )
        {
            this.value = other != null ? new ChannelState ( other ) : new ChannelState ();
            if ( this.value.creationTimestamp == null )
            {
                this.value.creationTimestamp = Instant.now ();
            }
            if ( this.value.modificationTimestamp == null )
            {
                this.value.modificationTimestamp = Instant.now ();
            }
        }

        public Builder ( final ChannelState other, final ChannelDetails details )
        {
            this ( other );

            if ( details != null )
            {
                this.value.description = details.getDescription ();
            }
        }

        public void setCreationTimestamp ( final Instant creationTimestamp )
        {
            checkFork ();
            this.value.creationTimestamp = creationTimestamp;
        }

        public void setModificationTimestamp ( final Instant modificationTimestamp )
        {
            checkFork ();
            this.value.modificationTimestamp = modificationTimestamp;
        }

        public void setDescription ( final String description )
        {
            checkFork ();
            this.value.description = description;
        }

        public void setNumberOfArtifacts ( final long numberOfArtifacts )
        {
            checkFork ();
            this.value.numberOfArtifacts = numberOfArtifacts;
        }

        public void setNumberOfBytes ( final long numberOfBytes )
        {
            checkFork ();
            this.value.numberOfBytes = numberOfBytes;
        }

        public void incrementNumberOfBytes ( final long numberOfBytes )
        {
            checkFork ();
            this.value.numberOfBytes += numberOfBytes;
        }

        public void setLocked ( final boolean locked )
        {
            checkFork ();
            this.value.locked = locked;
        }

        public void setValidationMessages ( final List<ValidationMessage> messages )
        {
            checkFork ();
            this.value.messages = Collections.unmodifiableList ( new CopyOnWriteArrayList<> ( messages ) );
        }

        private void checkFork ()
        {
            if ( this.needFork )
            {
                this.needFork = false;
                this.value = new ChannelState ( this.value );
            }
        }

        public ChannelState build ()
        {
            this.needFork = true;
            return this.value;
        }

    }

}
