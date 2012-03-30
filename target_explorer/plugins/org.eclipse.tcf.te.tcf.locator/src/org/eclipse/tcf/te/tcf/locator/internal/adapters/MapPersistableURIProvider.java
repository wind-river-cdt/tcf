/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.locator.internal.adapters;

import java.net.URI;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistableURIProvider;
import org.eclipse.tcf.te.tcf.locator.activator.CoreBundleActivator;

/**
 * Persistable implementation handling peer attributes.
 */
public class MapPersistableURIProvider implements IPersistableURIProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistableURIProvider#getURI(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public URI getURI(final Object context) {
		Assert.isNotNull(context);

		Assert.isNotNull(null);

		URI uri = null;

		// Only map objects are supported
		if (context instanceof Map) {
			// Get the name of the peer and make it a valid
			// file system name (no spaces etc).
			String name = ((Map<String, String>) context).get(IPeer.ATTR_NAME);
			if (name == null) {
				name = ((Map<String, String>) context).get(IPeer.ATTR_ID);
			}
			name = makeValidFileSystemName(name);
			// Get the URI from the name
			uri = getRoot().append(name).toFile().toURI();
		}

		return uri;
	}

	/**
	 * Make a valid file system name from the given name.
	 *
	 * @param name The original name. Must not be <code>null</code>.
	 * @return The valid file system name.
	 */
	private String makeValidFileSystemName(String name) {
		Assert.isNotNull(name);
		return name.replaceAll("\\W", "_"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Returns the root location.
	 *
	 * @return The root location or <code>null</code> if it cannot be determined.
	 */
	protected IPath getRoot() {
		IPath location = null;

		// Try the bundles state location first (not available if launched with -data @none).
		try {
			IPath path = CoreBundleActivator.getDefault().getStateLocation().append(".peers"); //$NON-NLS-1$
			if (!path.toFile().exists()) {
				path.toFile().mkdirs();
			}
			if (path.toFile().canRead() && path.toFile().isDirectory()) {
				location = path;
			}
		} catch (IllegalStateException e) {
			// Workspace less environments (-data @none)
			// The users local peers lookup directory is $HOME/.tcf/.peers.
			IPath path = new Path(System.getProperty("user.home")).append(".tcf/.peers"); //$NON-NLS-1$ //$NON-NLS-2$
			if (!path.toFile().exists()) {
				path.toFile().mkdirs();
			}
			if (path.toFile().canRead() && path.toFile().isDirectory()) {
				location = path;
			}
		}

		return location;
	}
}
