/*******************************************************************************
 * Copyright (c) 2015 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.repo.channel.apm;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.eclipse.packagedrone.repo.channel.ArtifactInformation;
import org.eclipse.packagedrone.repo.channel.CacheEntryInformation;
import org.eclipse.packagedrone.repo.channel.ValidationMessage;
import org.junit.Assert;

public final class ModelAssert
{
    private ModelAssert ()
    {
    }

    public static void assertModels ( final ModifyContextImpl expected, final ModifyContextImpl actual )
    {
        assertValue ( expected, actual, c -> c.getState ().isLocked () );
        assertValue ( expected, actual, c -> c.getState ().getCreationTimestamp () );
        assertValue ( expected, actual, c -> c.getState ().getModificationTimestamp () );
        assertValue ( expected, actual, c -> c.getState ().getNumberOfArtifacts () );
        assertValue ( expected, actual, c -> c.getState ().getNumberOfBytes () );

        assertCollection ( expected, actual, c -> c.getAspectStates ().keySet (), Assert::assertEquals );
        assertMap ( expected, actual, c -> c.getExtractedMetaData (), Assert::assertEquals );
        assertMap ( expected, actual, c -> c.getProvidedMetaData (), Assert::assertEquals );

        assertValidationMessages ( expected.getValidationMessages (), actual.getValidationMessages () );

        assertMap ( expected, actual, ModifyContextImpl::getCacheEntries, ( a, b ) -> {
            assertValue ( a, b, CacheEntryInformation::getKey );
            assertValue ( a, b, CacheEntryInformation::getMimeType );
            assertValue ( a, b, CacheEntryInformation::getName );
            assertValue ( a, b, CacheEntryInformation::getSize );
            assertValue ( a, b, CacheEntryInformation::getTimestamp );
        } );

        assertArtifacts ( expected.getArtifacts (), actual.getArtifacts () );
    }

    private static void assertValidationMessages ( final Collection<ValidationMessage> v1, final Collection<ValidationMessage> v2 )
    {
        assertCollection ( v1, v2, v -> v, ( a, b ) -> {
            assertValue ( a, b, ValidationMessage::getSeverity );
            assertValue ( a, b, ValidationMessage::getMessage );
            assertValue ( a, b, ValidationMessage::getAspectId );
            assertCollection ( a, b, ValidationMessage::getArtifactIds, Assert::assertEquals );
        } );
    }

    private static void assertArtifacts ( final Map<String, ArtifactInformation> a1, final Map<String, ArtifactInformation> a2 )
    {
        assertMap ( a1, a2, a -> a, ( a, b ) -> {
            assertValue ( a, b, ArtifactInformation::getId );
            assertValue ( a, b, ArtifactInformation::getCreationInstant );
            assertValue ( a, b, ArtifactInformation::getName );
            assertValue ( a, b, ArtifactInformation::getSize );

            assertValue ( a, b, ArtifactInformation::getParentId );
            assertCollection ( a, b, ArtifactInformation::getChildIds, Assert::assertEquals );

            assertCollection ( a, b, ArtifactInformation::getFacets, Assert::assertEquals );

            assertMap ( a, b, c -> c.getExtractedMetaData (), Assert::assertEquals );
            assertMap ( a, b, c -> c.getProvidedMetaData (), Assert::assertEquals );

            assertValidationMessages ( a.getValidationMessages (), b.getValidationMessages () );

            assertValue ( a, b, ArtifactInformation::getVirtualizerAspectId );
        } );
    }

    private static <C, T> void assertMap ( final C ctx1, final C ctx2, final Function<C, Map<?, T>> func, final BiConsumer<T, T> comparator )
    {
        final Map<?, T> col1 = func.apply ( ctx1 );
        final Map<?, T> col2 = func.apply ( ctx2 );

        Assert.assertNotNull ( col1 );
        Assert.assertNotNull ( col2 );

        Assert.assertArrayEquals ( col1.keySet ().toArray (), col2.keySet ().toArray () );
        for ( final Map.Entry<?, T> entry : col1.entrySet () )
        {
            final T value2 = col2.get ( entry.getKey () );
            comparator.accept ( entry.getValue (), value2 );
        }
    }

    @SuppressWarnings ( "unchecked" )
    private static <C, T> void assertCollection ( final C ctx1, final C ctx2, final Function<C, Collection<T>> func, final BiConsumer<T, T> comparator )
    {
        final Collection<?> col1 = func.apply ( ctx1 );
        final Collection<?> col2 = func.apply ( ctx2 );

        Assert.assertNotNull ( col1 );
        Assert.assertNotNull ( col2 );

        // test size first

        Assert.assertEquals ( col1.size (), col2.size () );

        // convert to arrays ... we want position access, fast

        final Object[] a1 = col1.toArray ();
        final Object[] a2 = col2.toArray ();

        // both arrays will have the same size

        for ( int i = 0; i < a1.length; i++ )
        {
            comparator.accept ( (T)a1[i], (T)a2[i] );
        }

        Assert.assertArrayEquals ( col1.toArray (), col2.toArray () );
    }

    private static <T, R> void assertValue ( final T ctx1, final T ctx2, final Function<T, R> func )
    {
        Assert.assertEquals ( func.apply ( ctx1 ), func.apply ( ctx2 ) );
    }

}
