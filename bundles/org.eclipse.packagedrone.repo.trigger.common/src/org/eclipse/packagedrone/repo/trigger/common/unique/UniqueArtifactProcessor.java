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
package org.eclipse.packagedrone.repo.trigger.common.unique;

import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static org.eclipse.packagedrone.repo.channel.search.Predicates.and;
import static org.eclipse.packagedrone.repo.channel.search.Predicates.equal;
import static org.eclipse.packagedrone.repo.channel.search.Predicates.not;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.packagedrone.repo.MetaKey;
import org.eclipse.packagedrone.repo.channel.AddingContext;
import org.eclipse.packagedrone.repo.channel.Veto;
import org.eclipse.packagedrone.repo.channel.VetoPolicy;
import org.eclipse.packagedrone.repo.channel.search.Predicate;
import org.eclipse.packagedrone.repo.channel.search.SearchOptions;
import org.eclipse.packagedrone.repo.channel.search.SearchOptions.Builder;
import org.eclipse.packagedrone.repo.trigger.Processor;

import com.google.common.escape.Escaper;
import com.google.common.html.HtmlEscapers;

public class UniqueArtifactProcessor implements Processor
{
    private final static Escaper ESC = HtmlEscapers.htmlEscaper ();

    private final UniqueArtifactConfiguration cfg;

    public UniqueArtifactProcessor ( final UniqueArtifactConfiguration cfg )
    {
        this.cfg = cfg;
    }

    @Override
    public void streamHtmlState ( final PrintWriter writer )
    {
        writer.format ( "Ensure that all artifacts with the key%s %s have the same value in <code>%s</code> by: %s.", this.cfg.getKeys ().length > 1 ? "s" : "", format ( this.cfg.getKeys () ), format ( this.cfg.getUniqueAttribute () ), format ( this.cfg.getVetoPolicy () ) );
        if ( this.cfg.isSkipMissingAttributes () )
        {
            writer.format ( " Artifacts with missing <q>artifac keys</q> will not be checked." );
        }
    }

    private Object format ( final VetoPolicy vetoPolicy )
    {
        switch ( vetoPolicy )
        {
            case FAIL:
                return "failing the operation";
            case REJECT: //$FALL-THROUGH$
            default:
                return "skipping the artifact";
        }
    }

    private String format ( final MetaKey[] keys )
    {
        return Arrays.stream ( keys ).map ( key -> String.format ( "<code>%s</code>", format ( key ) ) ).collect ( Collectors.joining ( ", " ) );
    }

    private static String format ( final MetaKey metaKey )
    {
        if ( metaKey == null )
        {
            return "";
        }

        return ESC.escape ( metaKey.toString () );
    }

    @Override
    public void process ( final Object context )
    {
        final AddingContext ctx = (AddingContext)context;

        final Map<MetaKey, String> target = new HashMap<> ( this.cfg.getKeys ().length );
        for ( final MetaKey key : this.cfg.getKeys () )
        {
            final String keyValue = ctx.getMetaData ().get ( key );
            if ( keyValue == null && this.cfg.isSkipMissingAttributes () )
            {
                // pass
                return;
            }
            target.put ( key, keyValue );
        }

        final String newUniqueValue = ctx.getMetaData ().get ( this.cfg.getUniqueAttribute () );

        if ( newUniqueValue == null )
        {
            // pass
            return;
        }

        if ( ctx.getArtifactLocator ().process ( makePredicate ( target, newUniqueValue ), makeOptions (), stream -> stream.findAny () ).isPresent () )
        {
            ctx.vetoAdd ( makeVeto ( newUniqueValue ) );
        }
    }

    private Veto makeVeto ( final String newUniqueValue )
    {
        String reason = this.cfg.getReason ();
        if ( reason == null )
        {
            final String fields = stream ( this.cfg.getKeys () ).map ( Object::toString ).collect ( joining ( ", " ) );
            reason = String.format ( "There is a least one artifact with the same values of %s but a different value of '%s' than '%s'", fields, this.cfg.getUniqueAttribute (), newUniqueValue );
        }
        return new Veto ( this.cfg.getVetoPolicy (), reason );
    }

    private SearchOptions makeOptions ()
    {
        final Builder builder = new SearchOptions.Builder ();

        builder.limit ( 1 );

        return builder.build ();
    }

    private Predicate makePredicate ( final Map<MetaKey, String> target, final String uniqueValue )
    {
        final List<Predicate> ands = new LinkedList<> ();

        for ( final Map.Entry<MetaKey, String> entry : target.entrySet () )
        {
            ands.add ( equal ( entry.getKey (), ofNullable ( entry.getValue () ) ) );
        }

        ands.add ( not ( equal ( this.cfg.getUniqueAttribute (), uniqueValue ) ) );

        return and ( ands );
    }

}
