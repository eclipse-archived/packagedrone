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

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.channel.deploy.DeployGroup;
import org.eclipse.packagedrone.repo.channel.deploy.DeployKey;
import org.eclipse.packagedrone.repo.channel.stats.ChannelStatistics;

/**
 * The service for working with channels
 * <h2>Channel access</h2>
 * <p>
 * Accessing a channel is primarily done by using the
 * {@link #accessRun(By, Class, ChannelOperationVoid)} and
 * {@link #accessCall(By, Class, ChannelOperation)} methods. Each of those
 * methods access an interface class and operation. The interface class chooses
 * in which way the channel will be accessed (e.g. read-only, writing,
 * configuration). Not all channels must support all operations. The operation
 * receives an instance of the requested interface class and may perform
 * operations on this instance as long as the operation method does not return.
 * After the operation is completed the instance will be disposed can further
 * calls to this instance will cause exceptions.
 * </p>
 * <p>
 * Supported interface classes are:
 * </p>
 * <dl>
 * <dt>{@link ReadableChannel}</dt>
 * <dd>A read only instance to the channel. Will lock the channel in shared read
 * mode. No write access is possible.</dd>
 * <dt>{@link ModifiableChannel}</dt>
 * <dd>A modifiable instance to the channel. Will lock the channel in exclusive
 * mode. No other read or write access is possible. Modifications will not be
 * persisted when the operation returns with an exception.
 * <dt>{@link DescriptorAdapter}</dt>
 * <dd>An instance for accessing the id and name mapping of the channel. Won't
 * lock the channel itself.</dd>
 * <dt>{@link DeployKeysChannelAdapter}</dt>
 * <dd>An instance for managing the deploy keys of a channel. Won't lock the
 * channel itself.</dt>
 * </dl>
 */
public interface ChannelService
{
    @FunctionalInterface
    public interface ChannelOperation<R, T>
    {
        public R process ( T channel ) throws Exception;
    }

    @FunctionalInterface
    public interface ChannelOperationVoid<T>
    {
        public void process ( T channel ) throws Exception;
    }

    @FunctionalInterface
    public interface ArtifactReceiver
    {
        public void consume ( ArtifactInformation artifact, InputStream stream ) throws IOException;
    }

    /**
     * Locator for channels
     */
    public static final class By
    {
        public static enum Type
        {
            ID,
            NAME,
            COMPOSITE;
        }

        private final Type type;

        private final Object qualifier;

        private By ( final Type type, final Object qualifier )
        {
            Objects.requireNonNull ( type );
            Objects.requireNonNull ( qualifier );

            this.type = type;
            this.qualifier = qualifier;
        }

        public Type getType ()
        {
            return type;
        }

        public Object getQualifier ()
        {
            return qualifier;
        }

        @Override
        public String toString ()
        {
            return String.format ( "[By %s = %s]", type, qualifier );
        }

        /**
         * Locate a channel by its ID
         *
         * @param channelId
         *            the channel id
         * @return the locator instance
         */
        public static By id ( final String channelId )
        {
            return new By ( Type.ID, channelId );
        }

        /**
         * Locate a channel by its name
         *
         * @param name
         *            the name of the channel
         * @return the locator instance
         */
        public static By name ( final String name )
        {
            return new By ( Type.NAME, name );
        }

        /**
         * Locate a channel either by its name, falling back to its id
         * <p>
         * First it will be tried to locate the channel by name and then by id.
         * </p>
         *
         * @param nameOrId
         *            the name or id of the channel
         * @return the locator instance
         */
        public static By nameOrId ( final String nameOrId )
        {
            return new By ( Type.COMPOSITE, new By[] { id ( nameOrId ), name ( nameOrId ) } );
        }
    }

    /**
     * List all channels
     *
     * @return the list of channel information
     */
    public Collection<ChannelInformation> list ();

    /**
     * Get the state of a single channel
     *
     * @param by
     *            the channel locator
     * @return the optional channel information, never returns {@code null}
     */
    public Optional<ChannelInformation> getState ( By by );

    public ChannelId create ( @NonNull String providerId, @NonNull ChannelDetails details, @NonNull Map<MetaKey, String> configuration );

    /**
     * Delete a channel
     *
     * @param by
     *            the channel locator
     * @return
     *         {@code true} if the channel was present and got deleted,
     *         {@code false otherwise}
     */
    public boolean delete ( By by );

    /**
     * Access a channel
     * <p>
     * It is guaranteed that either the operation will be called or the
     * ChannelNotFoundException will be thrown
     * </p>
     *
     * @param by
     *            locator of the channel to access
     * @param clazz
     *            the interface of the channel which should be accessed
     * @param operation
     *            the operation which should be performed on the channel
     * @return
     *         returns the value of the operation
     * @throws ChannelNotFoundException
     *             if the channel was not found
     */
    public <R, T> R accessCall ( By by, Class<T> clazz, ChannelOperation<R, T> operation );

    /**
     * Access a channel
     * <p>
     * It is guaranteed that either the operation will be called or the
     * ChannelNotFoundException will be thrown
     * </p>
     *
     * @param by
     *            locator of the channel to access
     * @param clazz
     *            the interface of the channel which should be accessed
     * @param operation
     *            the operation which should be performed on the channel
     * @throws ChannelNotFoundException
     *             if the channel was not found
     */
    public default <T> void accessRun ( final By by, final Class<T> clazz, final ChannelOperationVoid<T> operation )
    {
        accessCall ( by, clazz, channel -> {
            operation.process ( channel );
            return null;
        } );
    }

    public default @NonNull Optional<Collection<DeployKey>> getChannelDeployKeys ( final By by )
    {
        return getChannelDeployGroups ( by ).map ( groups -> groups.stream ().flatMap ( group -> group.getKeys ().stream () ).collect ( toList () ) );
    }

    public default @NonNull Optional<Set<String>> getChannelDeployKeyStrings ( final By by )
    {
        return getChannelDeployGroups ( by ).map ( groups -> groups.stream ().flatMap ( group -> group.getKeys ().stream () ).map ( DeployKey::getKey ).collect ( toSet () ) );
    }

    public @NonNull Optional<Collection<DeployGroup>> getChannelDeployGroups ( By by );

    public default boolean streamArtifact ( final String channelId, final String artifactId, final ArtifactReceiver receiver )
    {
        try
        {
            return accessCall ( By.id ( channelId ), ReadableChannel.class, channel -> {

                final Optional<ChannelArtifactInformation> artifact = channel.getArtifact ( artifactId );
                if ( !artifact.isPresent () )
                {
                    return false;
                }

                return channel.getContext ().stream ( artifactId, stream -> {
                    receiver.consume ( artifact.get (), stream );
                } );
            } );
        }
        catch ( final ChannelNotFoundException e )
        {
            return false;
        }
    }

    public ChannelStatistics getStatistics ();

    /**
     * Delete all channels
     */
    public void wipeClean ();

    public static final Pattern NAME_PATTERN = Pattern.compile ( "[a-zA-Z0-9\\-_\\.]+" );
}
