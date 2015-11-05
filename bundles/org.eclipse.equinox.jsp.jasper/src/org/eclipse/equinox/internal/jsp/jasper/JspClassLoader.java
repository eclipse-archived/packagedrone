/*******************************************************************************
 * Copyright (c) 2005, 2010 Cognos Incorporated, IBM Corporation and others.
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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.StringTokenizer;

import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;

/**
 * Jasper requires that this class loader be an instance of URLClassLoader.
 * At runtime it uses the URLClassLoader's getURLs method to find jar files that are in turn searched for TLDs. In a webapp
 * these jar files would normally be located in WEB-INF/lib. In the OSGi context, this behaviour is provided by returning the
 * URLs of the jar files contained on the Bundle-ClassPath. Other than jar file tld resources this classloader is not used for
 * loading classes which should be done by the other contained class loaders.
 * 
 * The rest of the ClassLoader is as follows:
 * 1) Thread-ContextClassLoader (top - parent) -- see ContextFinder
 * 2) Jasper Bundle
 * 3) The Bundle referenced at JSPServlet creation
 */
public class JspClassLoader extends URLClassLoader {

	private static final Bundle JASPERBUNDLE = Activator.getJasperBundle();
	private static final ClassLoader PARENT = JspClassLoader.class.getClassLoader().getParent();
	private static final String JAVA_PACKAGE = "java."; //$NON-NLS-1$
	private static final ClassLoader EMPTY_CLASSLOADER = new ClassLoader() {
		public URL getResource(String name) {
			return null;
		}

		public Enumeration findResources(String name) throws IOException {
			return new Enumeration() {
				public boolean hasMoreElements() {
					return false;
				}

				public Object nextElement() {
					return null;
				}
			};
		}

		public Class loadClass(String name) throws ClassNotFoundException {
			throw new ClassNotFoundException(name);
		}
	};

	public JspClassLoader(Bundle bundle) {
		super(new URL[0], new BundleProxyClassLoader(bundle, new BundleProxyClassLoader(JASPERBUNDLE, new JSPContextFinder(EMPTY_CLASSLOADER))));
		addBundleClassPathJars(bundle);
		Bundle[] fragments = Activator.getFragments(bundle);
		if (fragments != null) {
			for (int i = 0; i < fragments.length; i++) {
				addBundleClassPathJars(fragments[i]);
			}
		}
	}

	private void addBundleClassPathJars(Bundle bundle) {
		Dictionary headers = bundle.getHeaders();
		String classPath = (String) headers.get(Constants.BUNDLE_CLASSPATH);
		if (classPath != null) {
			StringTokenizer tokenizer = new StringTokenizer(classPath, ","); //$NON-NLS-1$
			while (tokenizer.hasMoreTokens()) {
				String candidate = tokenizer.nextToken().trim();
				if (candidate.endsWith(".jar")) { //$NON-NLS-1$
					URL entry = bundle.getEntry(candidate);
					if (entry != null) {
						URL jarEntryURL;
						try {
							jarEntryURL = new URL("jar:" + entry.toString() + "!/"); //$NON-NLS-1$ //$NON-NLS-2$
							super.addURL(jarEntryURL);
						} catch (MalformedURLException e) {
							// TODO should log this.
						}
					}
				}
			}
		}
	}

	protected Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
		if (PARENT != null && name.startsWith(JAVA_PACKAGE))
			return PARENT.loadClass(name);
		return super.loadClass(name, resolve);
	}

	// Classes should "not" be loaded by this classloader from the URLs - it is just used for TLD resource discovery.
	protected Class findClass(String name) throws ClassNotFoundException {
		throw new ClassNotFoundException(name);
	}
}
