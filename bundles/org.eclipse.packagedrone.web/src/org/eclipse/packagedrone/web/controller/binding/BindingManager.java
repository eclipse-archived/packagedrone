/*******************************************************************************
 * Copyright (c) 2014, 2016 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.web.controller.binding;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.packagedrone.utils.converter.ConverterManager;
import org.eclipse.packagedrone.utils.reflect.TypeResolver;
import org.eclipse.packagedrone.web.ModelAndView;
import org.eclipse.packagedrone.web.controller.binding.Binder.Initializer;
import org.eclipse.packagedrone.web.controller.validator.ValidationResult;
import org.eclipse.packagedrone.web.controller.validator.Validator;

public class BindingManager
{
    public static class Result extends SimpleBindingResult
    {
    }

    public interface Call
    {
        public Object invoke () throws Exception;
    }

    private final ConverterManager converter;

    public BindingManager ()
    {
        this.converter = ConverterManager.create ();
    }

    /**
     * Create a new BindingManager with default binders
     * <p>
     * This call creates a new BindingManager instance and add the following
     * binders:
     * </p>
     * <ul>
     * <li>{@link MapBinder}</li>
     * <li>{@link BindingManagerBinder}</li>
     * </ul>
     *
     * @param data
     *            the initial data for the MapBinder
     * @return
     *         a new binder manager instance
     */
    public static final BindingManager create ( final Map<String, Object> data )
    {
        final BindingManager result = new BindingManager ();

        result.addBinder ( new MapBinder ( data ) );
        result.addBinder ( new BindingManagerBinder () );

        return result;
    }

    protected Binding bindValue ( final BindTarget target, final ConverterManager converter )
    {
        for ( final Binder binder : this.binders )
        {
            final Binding binding = binder.performBind ( target, converter, this );
            if ( binding != null )
            {
                return binding;
            }
        }
        return null;
    }

    private BindTarget createParameterTarget ( final Parameter parameter, final Object[] args, final int argumentIndex, final TypeResolver typeResolver )
    {
        return new ParameterBindTarget ( parameter, args, argumentIndex, typeResolver );
    }

    protected BindTarget createPropertyTarget ( final Object object, final PropertyDescriptor pd )
    {
        return new PropertyBindTarget ( object, pd );
    }

    public Call bind ( final Method method, final Object targetObject )
    {
        Objects.requireNonNull ( method );
        Objects.requireNonNull ( targetObject );

        final TypeResolver typeResolver = new TypeResolver ( targetObject.getClass () );

        final Parameter[] p = method.getParameters ();

        final Binding[] bindings = new Binding[p.length];
        final Object[] args = new Object[p.length];

        for ( int i = 0; i < p.length; i++ )
        {
            final BindTarget target = createParameterTarget ( p[i], args, i, typeResolver );
            final Binding binding = bindValue ( target, this.converter );

            if ( binding != null )
            {
                bindings[i] = binding;
                target.bind ( binding );

                mergeErrors ( binding.getBindingResult (), this.result );
            }
            else
            {
                throw new IllegalStateException ( String.format ( "Unable to bind parameter '%s' (%s)", p[i].getName (), p[i].getType () ) );
            }
        }

        return new Call () {

            @Override
            public Object invoke () throws Exception
            {
                Object result = method.invoke ( targetObject, args );
                for ( final Binding binding : bindings )
                {
                    result = binding.postProcess ( result );
                }
                result = postProcess ( result );
                return result;
            }
        };
    }

    /**
     * Merge all errors of this binding into this result
     *
     * @param binding
     *            the binding to merge
     * @param result
     *            the result to merge into
     */
    private static void mergeErrors ( final BindingResult bindingResult, final BindingResult result )
    {
        if ( bindingResult == null )
        {
            return;
        }

        result.addErrors ( bindingResult.getLocalErrors () );

        for ( final Map.Entry<String, BindingResult> child : bindingResult.getChildren ().entrySet () )
        {
            mergeErrors ( child.getValue (), result.getChildOrAdd ( child.getKey () ) );
        }
    }

    protected Object postProcess ( final Object result )
    {
        if ( result instanceof ModelAndView )
        {
            ( (ModelAndView)result ).put ( BindingResult.ATTRIBUTE_NAME, this.result );
        }
        return result;
    }

    private final Collection<Binder> binders = new LinkedList<> ();

    private Validator validator;

    private final Result result = new Result ();

    /**
     * Add a new binder
     * <p>
     * If the binder has to be initialized then all methods annotated with
     * {@link Initializer} will be called.
     * </p>
     *
     * @param binder
     *            the binder to add
     * @param initializeBinder
     *            whether the binder will be initialized
     */
    public void addBinder ( final Binder binder, final boolean initializeBinder )
    {
        if ( initializeBinder )
        {
            initializeBinder ( binder );
        }
        this.binders.add ( binder );
    }

    /**
     * Add a new binder
     * <p>
     * The new binder will be initialized.
     * </p>
     *
     * @see #addBinder(Binder, boolean)
     * @param binder
     *            the binder to add
     */
    public void addBinder ( final Binder binder )
    {
        addBinder ( binder, true );
    }

    /**
     * Initialize the binder with our current state
     *
     * @param binder
     *            the binder to initialize
     * @return the list of exceptions or <code>null</code> if there were none
     */
    private void initializeBinder ( final Binder binder )
    {
        for ( final Method m : binder.getClass ().getMethods () )
        {
            if ( !m.isAnnotationPresent ( Binder.Initializer.class ) )
            {
                continue;
            }

            final Call call = bind ( m, binder );

            try
            {
                call.invoke ();
            }
            catch ( final Exception e )
            {
                throw new RuntimeException ( String.format ( "Failed to initialze binder: %s # %s", binder, m ), e );
            }
        }
    }

    public void bindProperties ( final Object o ) throws Exception
    {
        if ( o == null )
        {
            return;
        }

        final Class<?> objectClass = o.getClass ();
        final BeanInfo bi = Introspector.getBeanInfo ( objectClass );

        for ( final PropertyDescriptor pd : bi.getPropertyDescriptors () )
        {
            if ( pd.getWriteMethod () != null )
            {
                final BindTarget target = createPropertyTarget ( o, pd );
                final Binding binding = bindValue ( target, this.converter );
                if ( binding != null )
                {
                    target.bind ( binding );
                    this.result.addChild ( pd.getName (), binding.getBindingResult () );
                }
            }
        }

        validate ( o );
    }

    protected void validate ( final Object o )
    {
        if ( this.validator == null )
        {
            return;
        }

        final ValidationResult vr = this.validator.validate ( o );

        for ( final Map.Entry<String, List<BindingError>> entry : vr.getErrors ().entrySet () )
        {
            this.result.addErrors ( entry.getKey (), entry.getValue () );
        }

        this.result.addMarkers ( vr.getMarkers () );
    }

    public BindingResult getResult ()
    {
        return this.result;
    }

    public void setValidator ( final Validator validator )
    {
        this.validator = validator;
    }
}
