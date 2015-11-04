/*******************************************************************************
 * Copyright (c) 2014 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.utils.logging.jul;

 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 import org.slf4j.bridge.SLF4JBridgeHandler;

 public class Activator implements BundleActivator
 {

     @Override
     public void start ( final BundleContext arg0 ) throws Exception
     {
         SLF4JBridgeHandler.removeHandlersForRootLogger ();
         SLF4JBridgeHandler.install ();
     }

     @Override
     public void stop ( final BundleContext arg0 ) throws Exception
     {
     }

 }
