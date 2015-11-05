/*******************************************************************************
 * Copyright (c) 2005, 2009 Cognos Incorporated, IBM Corporation and others.
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

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class Activator implements BundleActivator, ServiceTrackerCustomizer {

	private ServiceTracker packageAdminTracker;
	private static PackageAdmin packageAdmin;
	private volatile static Bundle thisBundle;
	private BundleContext context;

	public void start(BundleContext context) throws Exception {
		//disable the JSR99 compiler that does not work in OSGi;
		//This will convince jasper to use the JDTCompiler that invokes ecj (see JSP-21 on the glassfish bug-tracker)
		System.setProperty("org.apache.jasper.compiler.disablejsr199", Boolean.TRUE.toString());
		this.context = context;
		thisBundle = context.getBundle();
		packageAdminTracker = new ServiceTracker(context, PackageAdmin.class.getName(), this);
		packageAdminTracker.open();
	}

	public void stop(BundleContext context) throws Exception {
		packageAdminTracker.close();
		packageAdminTracker = null;
		thisBundle = null;
		this.context = null;
	}

	public Object addingService(ServiceReference reference) {
		synchronized (Activator.class) {
			packageAdmin = (PackageAdmin) context.getService(reference);
		}
		return packageAdmin;
	}

	public void modifiedService(ServiceReference reference, Object service) {
	}

	public void removedService(ServiceReference reference, Object service) {
		synchronized (Activator.class) {
			context.ungetService(reference);
			packageAdmin = null;
		}
	}

	public static synchronized Bundle getBundle(Class clazz) {
		if (packageAdmin == null)
			throw new IllegalStateException("Not started"); //$NON-NLS-1$

		return packageAdmin.getBundle(clazz);
	}

	public static Bundle[] getFragments(Bundle bundle) {
		if (packageAdmin == null)
			throw new IllegalStateException("Not started"); //$NON-NLS-1$

		return packageAdmin.getFragments(bundle);
	}

	public static Bundle getJasperBundle() {
		Bundle bundle = getBundle(org.apache.jasper.servlet.JspServlet.class);
		if (bundle != null)
			return bundle;

		if (thisBundle == null)
			throw new IllegalStateException("Not started"); //$NON-NLS-1$

		ExportedPackage[] exportedPackages = packageAdmin.getExportedPackages("org.apache.jasper.servlet"); //$NON-NLS-1$
		for (int i = 0; i < exportedPackages.length; i++) {
			Bundle[] importingBundles = exportedPackages[i].getImportingBundles();
			for (int j = 0; j < importingBundles.length; j++) {
				if (thisBundle.equals(importingBundles[j]))
					return exportedPackages[i].getExportingBundle();
			}
		}
		return null;
	}
}
