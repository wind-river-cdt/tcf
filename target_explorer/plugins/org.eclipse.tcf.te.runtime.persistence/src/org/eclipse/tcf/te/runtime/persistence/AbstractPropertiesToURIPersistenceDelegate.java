/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.runtime.persistence;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Properties file persistence delegate implementation.
 * <p>
 * The persistence delegates reads and writes a simple grouped properties file format.
 */
public abstract class AbstractPropertiesToURIPersistenceDelegate extends AbstractPropertiesPersistenceDelegate {

	private String defaultFileExtension;

	/**
	 * Constructor.
	 *
	 * @param defaultFileExtension The default file extension.
	 */
	protected AbstractPropertiesToURIPersistenceDelegate(String defaultFileExtension) {
		super();
		Assert.isNotNull(defaultFileExtension);
		this.defaultFileExtension = defaultFileExtension;
	}

	/**
	 * Return the default extension.
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

			// Create the writer object
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8")); //$NON-NLS-1$
			try {
				// Write the first level of attributes
				writeMap(writer, key, toMap(context));
			} finally {
				writer.close();
			}
		}

		return container;
	}

	/**
	 * Write the map data.
	 *
	 * @param writer The writer. Must not be <code>null</code>.
	 * @param section The section name or <code>null</code>.
	 * @param data The data. Must not be <code>null</code>.
	 */
	private void writeMap(BufferedWriter writer, String section, Map<String, Object> data) throws IOException {
		Assert.isNotNull(writer);
		Assert.isNotNull(data);

		// Will contain the list of map keys where the value is a map type itself.
		List<String> childMapKeys = new ArrayList<String>();
		// Will contain the list of map keys where the value is not an map type.
		List<String> childKeys = new ArrayList<String>();

		// Get all the map keys and filter the map type values
		for (String key : data.keySet()) {
			if (data.get(key) instanceof Map) {
				childMapKeys.add(key);
			}
			else {
				childKeys.add(key);
			}
		}

		// Sort both lists
		Collections.sort(childMapKeys);
		Collections.sort(childKeys);

		// If the child key list is not empty, write the section
		if (!childKeys.isEmpty()) {
			// Write a new line except it is the "core" section
			if (section != null && !CORE_SECTION_NAME.equals(section)) {
				writer.newLine();
			}

			// Write the header
			if (section != null) {
				writer.write("[" + section.trim() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
				writer.newLine();
			}

			for (String key : childKeys) {
				if (section != null) {
					writer.write('\t');
				}
				writer.write(key);
				writer.write(" = "); //$NON-NLS-1$

				Object value = data.get(key);
				if (value instanceof List) {
					writer.write(Arrays.deepToString(((List<?>)value).toArray()));
				} else {
					writer.write(value.toString());
				}

				writer.newLine();
			}
		}

		// If there are map type values, write them now
		if (!childMapKeys.isEmpty()) {
			for (String key : childMapKeys) {
				// Calculate the section name
				String newSection = section == null || CORE_SECTION_NAME.equals(section) ? key.trim() : section + "." + key.trim(); //$NON-NLS-1$
				// Write it
				writeMap(writer, newSection, (Map<String, Object>)data.get(key));
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistenceDelegate#read(java.lang.Object, java.lang.Object, java.lang.String)
	 */
	@Override
	public Object read(Object context, Object container, String key) throws IOException {
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

			@SuppressWarnings("unchecked")
			Map<String, Object> data = context instanceof Map ? (Map<String,Object>)context : new HashMap<String, Object>();

			// Create the reader object
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8")); //$NON-NLS-1$
			try {
				readMap(reader, key, data);
			} finally {
				reader.close();
			}

			if (key != null && data.containsKey(key) && data.get(key) instanceof Map) {
				data = (Map<String,Object>)data.get(key);
			}

			context = fromMap(data, context);
		}

		return context;
	}

	private static Pattern SECTION = Pattern.compile("\\s*\\[([^\\]]+)\\]\\s*"); //$NON-NLS-1$
	private static Pattern PROPERTY = Pattern.compile("\\s*(.+\\s*=\\s*.+)"); //$NON-NLS-1$

	/**
	 * Read the data.
	 *
	 * @param reader The reader. Must not be <code>null</code>.
	 * @param section The section name or <code>null</code>.
	 * @param data The data. Must not be <code>null</code>.
	 */
	private void readMap(BufferedReader reader, String section, Map<String, Object> data) throws IOException {
		Assert.isNotNull(reader);
		Assert.isNotNull(data);

		// The sections by name for easier access.
		// The "core" section is the incoming data object
		Map<String, Map<String, Object>> sections = new HashMap<String, Map<String, Object>>();
		sections.put(section == null ? CORE_SECTION_NAME : section, data);

		String currentSection = section == null ? CORE_SECTION_NAME : section;
		String line = reader.readLine();
		while (line != null) {
			Matcher matcher = SECTION.matcher(line);
			if (matcher.matches()) {
				// Section names are case-sensitive too
				currentSection = matcher.group(1);
				if (sections.get(currentSection) == null) {
					sections.put(currentSection, new HashMap<String, Object>());
				}
			} else {
				matcher = PROPERTY.matcher(line);
				if (matcher.matches()) {
					String property = matcher.group(1);
					String[] pieces = property.split("=", 2); //$NON-NLS-1$
					Map<String, Object> sectionMap = sections.get(currentSection);
					sectionMap.put(pieces[0].trim(), pieces[1].trim());
				}
			}

			line = reader.readLine();
		}

		// Recreate the sections hierarchy
		for (String sectionName : sections.keySet()) {
			if (CORE_SECTION_NAME.equals(sectionName)) {
				continue;
			}
			Map<String, Object> sectionMap = sections.get(sectionName);
			if (sectionName.contains(".")) { //$NON-NLS-1$
				// Split the section name and recreate the missing hierarchy
				String[] pieces = sectionName.split("\\."); //$NON-NLS-1$
				Map<String, Object> parentSection = data;
				for (String subSectionName : pieces) {
					if (CORE_SECTION_NAME.equals(subSectionName)) {
						continue;
					}

					if (sectionName.endsWith(subSectionName)) {
						parentSection.put(subSectionName, sectionMap);
					} else {
						Map<String, Object> subSection = (Map<String, Object>)parentSection.get(subSectionName);
						if (subSection == null) {
							subSection = new HashMap<String, Object>();
							parentSection.put(subSectionName, subSection);
						}
						parentSection = subSection;
					}
				}
			} else {
				// Place it into the root object, but check if it may exist
				Map<String, Object> oldSection = (Map<String, Object>)data.get(sectionName);
				if (oldSection != null) {
					oldSection.putAll(sectionMap);
				}
				else {
					data.put(sectionName, sectionMap);
				}
			}
		}
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
}
