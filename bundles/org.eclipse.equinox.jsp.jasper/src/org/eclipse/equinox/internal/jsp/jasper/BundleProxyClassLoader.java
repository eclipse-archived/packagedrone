/*******************************************************************************
 * Copyright (c) 2005, 2007 Cognos Incorporated, IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Cognos Incorporated - initial API and implementation
 *     IBM Corporation - bug fixes and enhancements
 *******************************************************************************/
package org.eclipse.equinox.internal.jsp.jasper;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import org.osgi.framework.Bundle;


/**
 * A BundleProxyClassLoader wraps a bundle and uses the various Bundle methods to produce a ClassLoader. 
 */
public class BundleProxyClassLoader extends ClassLoader {
	private Bundle bundle;
	private ClassLoader parent;

	public BundleProxyClassLoader(Bundle bundle) {
		this.bundle = bundle;
	}

	public BundleProxyClassLoader(Bundle bundle, ClassLoader parent) {
		super(parent);
		this.parent = parent;
		this.bundle = bundle;
	}

	public Enumeration findResources(String name) throws IOException {
		return bundle.getResources(name);
	}

	public URL findResource(String name) {
		return bundle.getResource(name);
	}

	public Class findClass(String name) throws ClassNotFoundException {
		return bundle.loadClass(name);
	}

	public URL getResource(String name) {
		return (parent == null) ? findResource(name) : super.getResource(name);
	}

	protected Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
		Class clazz = (parent == null) ? findClass(name) : super.loadClass(name, false);
		if (resolve)
			super.resolveClass(clazz);

		return clazz;
	}
}
