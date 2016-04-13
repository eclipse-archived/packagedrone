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
package org.eclipse.packagedrone.utils.rpm.header;

import static java.util.Arrays.stream;
import static java.util.Comparator.comparingInt;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

import org.eclipse.packagedrone.utils.rpm.Rpms;

/**
 * Process headers (signature and package)
 *
 * @author Jens Reimann
 */
public final class Headers
{
    private Headers ()
    {
    }

    public static ByteBuffer render ( final HeaderEntry[] entries, final boolean sorted, final Integer immutableTag ) throws IOException
    {
        Objects.requireNonNull ( entries );

        // sorted header

        if ( sorted )
        {
            Arrays.sort ( entries, comparingInt ( HeaderEntry::getTag ) );
        }

        // number of entries

        int numEntries = entries.length;
        if ( immutableTag != null )
        {
            numEntries++;
        }

        // allocate header

        int len = 16; // common part (magic, version, counters)
        final int entriesLen = numEntries * 4 * 4; // one record (4 ints) for each entry
        len += entriesLen; // record for each entry

        len += stream ( entries ).mapToInt ( Headers::rawEntrySize ).sum (); // raw entry size
        if ( immutableTag != null )
        {
            len += 16;
        }
        len += numEntries * 8; // over allocate for paddings

        final ByteBuffer buffer = ByteBuffer.allocate ( len );

        // header magic

        buffer.put ( Rpms.HEADER_MAGIC );

        // header version

        buffer.put ( (byte)1 );

        // 4 empty bytes

        buffer.put ( Rpms.EMPTY_128, 0, 4 );

        // number of entries

        buffer.putInt ( numEntries );

        // header payload size

        final int payloadSizePosition = buffer.position ();
        buffer.putInt ( 0 ); // empty for now

        // advance, leave gap for entry records

        buffer.position ( buffer.position () + entriesLen );

        // process entries, add header payload

        final int startPayloadPosition = buffer.position ();

        /*
         * If we need to write an immutable header marker, then
         * it will go first in the record list, but last in the data section.
         *
         * So we start at index 1 and fill up later
         */

        int i = immutableTag == null ? 0 : 1;

        for ( final HeaderEntry entry : entries )
        {
            // align

            align ( buffer, entry );

            // append payload data

            final int index = buffer.position () - startPayloadPosition;
            buffer.put ( entry.getData () );

            // fill entry record

            fillEntryRecord ( buffer, i, entry, index );

            i++;
        }

        if ( immutableTag != null )
        {
            final ByteBuffer tagData = ByteBuffer.wrap ( new byte[16] );

            /*
             * create the data section for the immutable tag
             * this is indeed another tag record structure, stored inside the
             * payload data of the immutable tag entry
             */

            final int numImmutable = entries.length + 1; // we want all entries to be immutable
            fillEntryRecordAt ( tagData, 0, immutableTag, Type.BLOB.type (), -numImmutable * 16, 16 );

            // get the index in the payload section ... we are last now

            final int index = buffer.position () - startPayloadPosition;

            // but the data of the immutable tag into the payload section

            buffer.put ( tagData.array () );

            // write the entry record of the immutable tag

            fillEntryRecord ( buffer, 0, immutableTag, Type.BLOB.type (), index, 16 );
        }

        // update payloadSizePosition

        final int payloadSize = buffer.position () - startPayloadPosition;
        buffer.putInt ( payloadSizePosition, payloadSize );

        // return result - note that the last entry is not padded

        buffer.flip ();
        return buffer;
    }

    private static void fillEntryRecord ( final ByteBuffer buffer, final int entryIndex, final HeaderEntry entry, final int index )
    {
        fillEntryRecord ( buffer, entryIndex, entry.getTag (), entry.getType ().type (), index, entry.getCount () );
    }

    private static void fillEntryRecord ( final ByteBuffer buffer, final int entryIndex, final int tag, final int type, final int index, final int count )
    {
        final int entryPosition = 16 + entryIndex * 16;

        fillEntryRecordAt ( buffer, entryPosition, tag, type, index, count );
    }

    private static void fillEntryRecordAt ( final ByteBuffer buffer, final int position, final int tag, final int type, final int index, final int count )
    {
        buffer.putInt ( position, tag );
        buffer.putInt ( position + 4, type );
        buffer.putInt ( position + 8, index );
        buffer.putInt ( position + 12, count );
    }

    public static byte[] makeEntryRecord ( final int tag, final int type, final int index, final int count )
    {
        final ByteBuffer buffer = ByteBuffer.wrap ( new byte[16] );
        fillEntryRecordAt ( buffer, 0, tag, type, index, count );
        return buffer.array ();
    }

    private static void align ( final ByteBuffer buffer, final HeaderEntry entry )
    {
        final int position = buffer.position ();
        final int alignment = entry.getType ().align ();

        final int v = position % alignment;
        if ( v <= 0 )
        {
            return;
        }

        final int len = alignment - v;

        buffer.put ( Rpms.EMPTY_128, 0, len );
    }

    private static int rawEntrySize ( final HeaderEntry entry )
    {
        return entry.getData () != null ? entry.getData ().length : 0;
    }
}
