/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.core.adapters;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.te.core.activator.CoreBundleActivator;
import org.eclipse.tcf.te.core.nls.Messages;
import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;
import org.eclipse.tcf.te.runtime.persistence.PersistenceDelegateManager;
import org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistable;
import org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistenceDelegate;

/**
 * Model node persistable adapter implementation.
 */
public class ModelNodePersistableAdapter implements IPersistable {

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistable#getStorageID()
	 */
	@Override
	public String getStorageID() {
		return "org.eclipse.tcf.te.runtime.persistence.properties"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistable#getURI(java.lang.Object)
	 */
	@Override
	public URI getURI(Object data) {
		Assert.isNotNull(data);

		URI uri = null;

		// Only model nodes are supported
		if (data instanceof IModelNode) {
			IModelNode node = (IModelNode) data;
			if (node.getName() != null && !"".equals(node.getName().trim())) { //$NON-NLS-1$
				// Get the node name and make it a valid file system name (no spaces etc).
				IPath path = getRoot().append(makeValidFileSystemName(((IModelNode) data).getName().trim()));
				if (!"ini".equals(path.getFileExtension())) path = path.addFileExtension("ini"); //$NON-NLS-1$ //$NON-NLS-2$
				uri = path.toFile().toURI();
			}
			// If the name is not set, check for "Path"
			else if (node.getStringProperty("Path") != null && !"".equals(node.getStringProperty("Path").trim())) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				IPath path = new Path(node.getStringProperty("Path")); //$NON-NLS-1$
				uri = path.toFile().toURI();
			}
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
	public IPath getRoot() {
		IPath location = null;

		// Try the bundles state location first (not available if launched with -data @none).
		try {
			IPath path = Platform.getStateLocation(CoreBundleActivator.getContext().getBundle()).append(".store"); //$NON-NLS-1$
			if (!path.toFile().exists()) path.toFile().mkdirs();
			if (path.toFile().canRead() && path.toFile().isDirectory()) {
				location = path;
			}
		} catch (IllegalStateException e) {
			// Workspace less environments (-data @none)
			// The users local target definition persistence directory is $HOME/.tcf/.store.
			IPath path = new Path(System.getProperty("user.home")).append(".tcf/.store"); //$NON-NLS-1$ //$NON-NLS-2$
			if (!path.toFile().exists()) path.toFile().mkdirs();
			if (path.toFile().canRead() && path.toFile().isDirectory()) {
				location = path;
			}
		}

		return location;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistable#exportFrom(java.lang.Object)
	 */
	@Override
	public Map<String, Object> exportFrom(Object data) throws IOException {
		Assert.isNotNull(data);

		// Create a new map instance that will hold the exported properties
		Map<String, Object> result = new HashMap<String, Object>();

		// Only model nodes are supported
		if (data instanceof IModelNode && !((IModelNode)data).isEmpty()) {
			// Get a snapshot of all properties
			Map<String, Object> properties = ((IModelNode)data).getProperties();
			// And export the properties to the result map
			exportFromMap(properties, result);
		}

		// If the result map is empty, return null
		return !result.isEmpty() ? result : null;
	}

	/**
	 * Exports the properties of a map from the given source into the given
	 * destination.
	 *
	 * @param src The map to export the properties from. Must not be <code>null</code>.
	 * @param dst The map to write the exported properties to. Must not be <code>null</code>.
	 *
	 * @throws IOException - if the operation fails.
	 */
	@SuppressWarnings("unchecked")
    protected void exportFromMap(Map<String, Object> src, Map<String, Object> dst) throws IOException {
		Assert.isNotNull(src);
		Assert.isNotNull(dst);

		// Loop all properties and check for transient or complex properties
		for (String key : src.keySet()) {
			if (key.contains(".transient")) continue; //$NON-NLS-1$

			// Get the property value
			Object value = src.get(key);

			// If the value is null, no need to go any further
			if (value == null) continue;

			// For String, Integer, Boolean, etc ... export them as string
			boolean isSimpleType = value instanceof String || value instanceof Boolean || value instanceof Integer || value instanceof Long
										|| value instanceof Float || value instanceof Double;
			if (isSimpleType) {
				dst.put(key, value.toString());
				continue;
			}

			// BigInteger, BigDecimal ... probably needs special handling, for now, export them as string
			boolean isBigType = value instanceof BigInteger || value instanceof BigDecimal;
			if (isBigType) {
				dst.put(key, value.toString());
				continue;
			}

			// For Lists and Arrays, do a deepToString
			boolean isListType = value instanceof List<?> || value instanceof Object[];
			if (isListType) {
				dst.put(key, Arrays.deepToString(value instanceof List<?> ? ((List<?>)value).toArray() : (Object[])value));
				continue;
			}

			// For Maps, create a new destination map and call ourself
			boolean isMapType = value instanceof Map<?,?>;
			if (isMapType) {
				Map<String, Object> result = new HashMap<String, Object>();
				exportFromMap((Map<String, Object>)value, result);
				if (!result.isEmpty()) dst.put(key, result);
				continue;
			}

			// For anything remaining, check if the value object type can be adapted to
			// an IPersistable itself
			IPersistable persistable = value instanceof IAdaptable ? (IPersistable)((IAdaptable)value).getAdapter(IPersistable.class) : null;
			if (persistable == null) persistable = (IPersistable)Platform.getAdapterManager().getAdapter(value, IPersistable.class);
			if (persistable != null) {
				// Create a reference object
				Map<String, String> reference = new HashMap<String, String>();
				reference.put("storageID", persistable.getStorageID()); //$NON-NLS-1$
				reference.put("uri", persistable.getURI(value).toString()); //$NON-NLS-1$

				IPersistenceDelegate delegate = PersistenceDelegateManager.getInstance().getDelegate(persistable.getStorageID(), false);
				if (delegate != null) {
					delegate.write(persistable.getURI(value), persistable.exportFrom(value));
					dst.put(key, reference);
					continue;
				}
			}

			// Falling through down here is a problem. We should never end up here,
			// because it means we have no idea on how to persist an object
			throw new IOException(NLS.bind(Messages.ModelNodePersistableAdapter_export_unknownType, value.getClass().getCanonicalName(), key));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistable#importTo(java.lang.Object, java.util.Map)
	 */
	@Override
	public void importTo(Object data, Map<String, Object> external) throws IOException {
		Assert.isNotNull(data);
		Assert.isNotNull(external);

		// Only model nodes are supported
		if (data instanceof IModelNode) {
			IModelNode node = (IModelNode) data;
			for (String key : external.keySet()) {
				node.setProperty(key, external.get(key));
			}
		}
	}

}
