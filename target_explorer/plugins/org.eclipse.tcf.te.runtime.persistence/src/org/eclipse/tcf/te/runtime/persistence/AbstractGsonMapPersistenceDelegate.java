/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.tcf.te.runtime.persistence;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.tcf.te.runtime.extensions.ExecutableExtension;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistenceDelegate;
import org.eclipse.tcf.te.runtime.properties.PropertiesContainer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * AbstractGsonMapPersistenceDelegate
 */
public abstract class AbstractGsonMapPersistenceDelegate extends ExecutableExtension implements IPersistenceDelegate {

	private final String defaultFileExtension;

	/**
	 * Constructor.
	 */
	public AbstractGsonMapPersistenceDelegate() {
		this("ini"); //$NON-NLS-1$
	}

	/**
	 * Constructor.
	 */
	public AbstractGsonMapPersistenceDelegate(String defaultFileExtension) {
		super();
		Assert.isNotNull(defaultFileExtension);
		this.defaultFileExtension = defaultFileExtension;
	}

	/**
	 * Return the default file extension if container is an URI.
	 */
	protected String getDefaultFileExtension() {
		return defaultFileExtension;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistenceDelegate#write(java.lang.Object, java.lang.Object, java.lang.String)
	 */
	@Override
	public Object write(Object context, Object container, String key) throws IOException {
		Assert.isNotNull(context);
		Assert.isNotNull(container);

		if (container instanceof URI) {
			URI uri = (URI)container;

			// Only "file:" URIs are supported
			if (!"file".equalsIgnoreCase(uri.getScheme())) { //$NON-NLS-1$
				throw new IOException("Unsupported URI schema '" + uri.getScheme() + "'"); //$NON-NLS-1$ //$NON-NLS-2$
			}

			// Create the file object from the given URI
			File file = new File(uri.normalize());

			// The file must be absolute
			if (!file.isAbsolute()) {
				throw new IOException("URI must denote an absolute file path."); //$NON-NLS-1$
			}

			// If the file defaultFileExtension is no set, default to "properties"
			IPath path = new Path(file.getCanonicalPath());
			if (path.getFileExtension() == null) {
				file = path.addFileExtension(getDefaultFileExtension()).toFile();
			}

			Writer writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8"); //$NON-NLS-1$
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			gson.toJson(toMap(context), Map.class, writer);
			writer.close();
		}
		else if (container instanceof String || String.class.equals(container)) {
			Gson gson = new GsonBuilder().create();
			container = gson.toJson(toMap(context));
		}

		return container;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistenceDelegate#read(java.lang.Object, java.lang.Object, java.lang.String)
	 */
	@Override
	public Object read(Object context, Object container, String key) throws IOException {
		Assert.isNotNull(container);

		Gson gson = new GsonBuilder().create();
		Map<String, Object> data = null;

		if (container instanceof URI) {
			URI uri = (URI)container;

			// Only "file:" URIs are supported
			if (!"file".equalsIgnoreCase(uri.getScheme())) { //$NON-NLS-1$
				throw new IOException("Unsupported URI schema '" + uri.getScheme() + "'"); //$NON-NLS-1$ //$NON-NLS-2$
			}

			// Create the file object from the given URI
			File file = new File(uri.normalize());

			// The file must be absolute
			if (!file.isAbsolute()) {
				throw new IOException("URI must denote an absolute file path."); //$NON-NLS-1$
			}

			Reader reader = new InputStreamReader(new FileInputStream(file), "UTF-8"); //$NON-NLS-1$
			data = gson.fromJson(reader, Map.class);
			reader.close();
		}
		else if (container instanceof String) {
			data = gson.fromJson((String)container, Map.class);
		}

		return data != null ? fromMap(data, context) : context;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistenceDelegate#delete(java.lang.Object, java.lang.Object, java.lang.String)
	 */
	@Override
	public boolean delete(Object context, Object container, String key) throws IOException {
		Assert.isNotNull(container);

		if (container instanceof URI) {
			URI uri = (URI)container;

			// Only "file:" URIs are supported
			if (!"file".equalsIgnoreCase(uri.getScheme())) { //$NON-NLS-1$
				throw new IOException("Unsupported URI schema '" + uri.getScheme() + "'"); //$NON-NLS-1$ //$NON-NLS-2$
			}

			// Create the file object from the given URI
			File file = new File(uri.normalize());

			// The file must be absolute
			if (!file.isAbsolute()) {
				throw new IOException("URI must denote an absolute file path."); //$NON-NLS-1$
			}

			// If the file defaultFileExtension is no set, default to "properties"
			IPath path = new Path(file.getCanonicalPath());
			if (path.getFileExtension() == null) {
				file = path.addFileExtension(getDefaultFileExtension()).toFile();
			}

			return file.delete();
		}

		return false;
	}

	/**
	 * Convert the given context to map.
	 *
	 * @param context The context. Must not be <code>null</code>.
	 * @return Map representing the context.
	 *
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	protected Map<String, Object> toMap(final Object context) throws IOException {
		Map<String, Object> result = new HashMap<String,Object>();

		Map<String,Object> attrs = null;
		if (context instanceof Map) {
			attrs = (Map<String, Object>)context;
		}
		else if (context instanceof IPropertiesContainer) {
			IPropertiesContainer container = (IPropertiesContainer)context;
			attrs = new HashMap<String,Object>(container.getProperties());
		}

		if (attrs != null) {
			for (Entry<String, Object> entry : attrs.entrySet()) {
				if (!entry.getKey().endsWith(".transient")) { //$NON-NLS-1$
					result.put(entry.getKey(), entry.getValue());
				}
			}
		}

		return result;
	}

	/**
	 * Convert a map into the needed context object.
	 *
	 * @param map The map representing the context. Must not be <code>null</code>.
	 * @param context The context to put the map values in or <code>null</code>.
	 * @return The context object.
	 *
	 * @throws IOException
	 */
	protected Object fromMap(Map<String,Object> map, Object context) throws IOException {
		if (context == null || Map.class.equals(context.getClass())) {
			return map;
		}
		else if (context instanceof Map) {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			Map<String,Object> newMap = new HashMap<String, Object>((Map)context);
			newMap.putAll(map);
			return newMap;
		}
		else if (IPropertiesContainer.class.equals(context.getClass())) {
			IPropertiesContainer container = new PropertiesContainer();
			container.setProperties(map);

			return container;
		}
		else if (context instanceof IPropertiesContainer) {
			IPropertiesContainer container = (IPropertiesContainer)context;
			container.setProperties(map);

			return container;
		}

		return null;
	}
}
