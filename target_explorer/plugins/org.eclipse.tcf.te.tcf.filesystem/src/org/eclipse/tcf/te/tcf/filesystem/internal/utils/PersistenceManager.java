/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * William Chen (Wind River)	[360494]Provide an "Open With" action in the pop
 * 								up menu of file system nodes of Target Explorer.
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.internal.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.tcf.te.tcf.filesystem.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.filesystem.internal.preferences.PreferencePage;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;

/**
 * A facility class to load and save persistent data such including resolved content types, file's
 * properties, and time stamps etc.
 */
public class PersistenceManager {
	// The XML element of unresolvable.
	private static final String ELEMENT_UNRESOLVABLE = "unresolvable"; //$NON-NLS-1$

	// The root element of "unresolvables"
	private static final String ELEMENT_UNRESOLVED = "unresolved"; //$NON-NLS-1$

	// The attribute "contentType" to specify the content type id of the file.
	private static final String ATTR_CONTENT_TYPE = "contentType"; //$NON-NLS-1$

	// The XML element of resolvable.
	private static final String ELEMENT_RESOLVABLE = "resolvable"; //$NON-NLS-1$

	// The root element of "resolvables"
	private static final String ELEMENT_RESOLVED = "resolved"; //$NON-NLS-1$

	// The root element of the memento for content type resolving.
	private static final String CONTENT_TYPE_ROOT = "contentTypes"; //$NON-NLS-1$

	// The XML file name used to store the resolved content types.
	private static final String CONTENT_TYPE_FILE = "contentTypes.xml"; //$NON-NLS-1$

	// The attribute "value"
	private static final String ATTR_VALUE = "value"; //$NON-NLS-1$

	// The attribute "local name" of a qualified name.
	private static final String ATTR_LOCAL_NAME = "localName"; //$NON-NLS-1$

	// The attribute "qualifier" of a qualified name.
	private static final String ATTR_QUALIFIER = "qualifier"; //$NON-NLS-1$

	// The attribute of a node's URI
	private static final String ATTR_URI = "URI"; //$NON-NLS-1$

	// The element "property" to record a file's property
	private static final String ELEMENT_PROPERTY = "property"; //$NON-NLS-1$

	// The element "file" to specify a file's entry.
	private static final String ELEMENT_FILE = "file"; //$NON-NLS-1$

	// The root element of properties.
	private static final String PERSISTENT_ROOT = "properties"; //$NON-NLS-1$

	// Time stamp file used to persist the time stamps of each file.
	private static final String TIMESTAMP_FILE = "timestamps.xml"; //$NON-NLS-1$

	// The file used to store persistent properties of each file.
	private static final String PERSISTENT_FILE = "persistent.xml"; //$NON-NLS-1$

	// The singleton instance.
	private static PersistenceManager instance;

	// The time stamp for each file.
	private Map<URI, Long> timestamps;

	// The persistent properties of the files.
	private Map<URI, Map<QualifiedName, String>> properties;

	// Already known resolved content type of file nodes specified by their URIs.
	private Map<URI, IContentType> resolved;

	// Already known unresolvable file nodes specified by their URIs.
	private Map<URI, URI> unresolved;

	/**
	 * Get the singleton cache manager.
	 *
	 * @return The singleton cache manager.
	 */
	public static PersistenceManager getInstance() {
		if (instance == null) {
			instance = new PersistenceManager();
		}
		return instance;
	}

	/**
	 * Create a Persistent Manager instance.
	 */
	private PersistenceManager() {
		loadTimestamps();
		loadPersistentProperties();
		loadContentTypes();
	}

	/**
	 * If the node is already considered unresolvable.
	 *
	 * @param node The file node.
	 * @return true if it is not resolvable or else false.
	 */
	public boolean isUnresovled(FSTreeNode node) {
		return unresolved.get(node.getLocationURI()) != null;
	}

	/**
	 * Get the resolved content type of the node.
	 *
	 * @param node The file node.
	 * @return the content type of the node if it is resolvable or null.
	 */
	public IContentType getResolved(FSTreeNode node) {
		return resolved.get(node.getLocationURI());
	}

	/**
	 * Add the node and its content type to the resolved list.
	 *
	 * @param node The file node.
	 * @param contentType Its content type.
	 */
	public void addResovled(FSTreeNode node, IContentType contentType) {
		resolved.put(node.getLocationURI(), contentType);
	}

	/**
	 * Add the node as an unresolvable node.
	 *
	 * @param node The file node.
	 */
	public void addUnresolved(FSTreeNode node) {
		unresolved.put(node.getLocationURI(), node.getLocationURI());
	}

	/**
	 * If the option of "autosaving" is set to on.
	 *
	 * @return true if it is auto saving or else false.
	 */
	public boolean isAutoSaving() {
		IPreferenceStore preferenceStore = UIPlugin.getDefault().getPreferenceStore();
		boolean autoSaving = preferenceStore
		                .getBoolean(PreferencePage.PREF_AUTOSAVING);
		return autoSaving;
	}

	/**
	 * If the option of "in-place editor" is set to on.
	 *
	 * @return true if it uses in-place editor when renaming files/folders.
	 */
	public boolean isInPlaceEditor() {
		IPreferenceStore preferenceStore = UIPlugin.getDefault().getPreferenceStore();
		boolean autoSaving = preferenceStore
		                .getBoolean(PreferencePage.PREF_RENAMING_IN_PLACE_EDITOR);
		return autoSaving;
	}

	/**
	 * If the option of "copy permissions" is set to on.
	 *
	 * @return true if it should copy source file permissions.
	 */
	public boolean isCopyPermission() {
		IPreferenceStore preferenceStore = UIPlugin.getDefault().getPreferenceStore();
		boolean autoSaving = preferenceStore
		                .getBoolean(PreferencePage.PREF_COPY_PERMISSION);
		return autoSaving;
	}

	/**
	 * If the option of "copy ownership" is set to on.
	 *
	 * @return true if it should copy source file ownership.
	 */
	public boolean isCopyOwnership() {
		IPreferenceStore preferenceStore = UIPlugin.getDefault().getPreferenceStore();
		boolean autoSaving = preferenceStore
		                .getBoolean(PreferencePage.PREF_COPY_OWNERSHIP);
		return autoSaving;
	}

	/**
	 * Load the persistent properties from the persistent file in the cache's root directory.
	 */
	private void loadPersistentProperties() {
		IMemento memento = readMemento(PERSISTENT_FILE, PERSISTENT_ROOT);
		properties = Collections.synchronizedMap(new HashMap<URI, Map<QualifiedName, String>>());
		IMemento[] children = memento.getChildren(ELEMENT_FILE);
		if (children != null && children.length > 0) {
			for (IMemento child : children) {
				try {
					String str = child.getString(ATTR_URI);
					URI uri = new URI(str);
					Map<QualifiedName, String> nodeProperties = loadFileProperties(child);
					properties.put(uri, nodeProperties);
				}
				catch (URISyntaxException e) {
				}
			}
		}
	}

	/**
	 * Load the content type information from the content type file.
	 */
	private void loadContentTypes() {
		IMemento memento = readMemento(CONTENT_TYPE_FILE, CONTENT_TYPE_ROOT);
		resolved = Collections.synchronizedMap(new HashMap<URI, IContentType>());
		unresolved = Collections.synchronizedMap(new HashMap<URI, URI>());
		IMemento mResolved = memento.getChild(ELEMENT_RESOLVED);
		if (mResolved != null) {
			IMemento[] children = mResolved.getChildren(ELEMENT_RESOLVABLE);
			if (children != null && children.length > 0) {
				for (IMemento child : children) {
					try {
						String str = child.getString(ATTR_URI);
						URI uri = new URI(str);
						String id = child.getString(ATTR_CONTENT_TYPE);
						IContentType contentType = Platform.getContentTypeManager()
						                .getContentType(id);
						if (contentType != null) {
							resolved.put(uri, contentType);
						}
					}
					catch (URISyntaxException e) {
					}
				}
			}
		}
		IMemento mUnresolved = memento.getChild(ELEMENT_UNRESOLVED);
		if (mUnresolved != null) {
			IMemento[] children = mUnresolved.getChildren(ELEMENT_UNRESOLVABLE);
			if (children != null && children.length > 0) {
				for (IMemento child : children) {
					try {
						String str = child.getString(ATTR_URI);
						URI uri = new URI(str);
						unresolved.put(uri, uri);
					}
					catch (URISyntaxException e) {
					}
				}
			}
		}
	}

	/**
	 * Save the content type information to the content type file.
	 */
	private void saveContentTypes() {
		XMLMemento memento = XMLMemento.createWriteRoot(CONTENT_TYPE_ROOT);
		IMemento mResolved = memento.createChild(ELEMENT_RESOLVED);
		for (URI key : resolved.keySet()) {
			IContentType iContentType = resolved.get(key);
			IMemento mResolvable = mResolved.createChild(ELEMENT_RESOLVABLE);
			mResolvable.putString(ATTR_URI, key.toString());
			mResolvable.putString(ATTR_CONTENT_TYPE, iContentType.getId());
		}
		IMemento mUnresolved = memento.createChild(ELEMENT_UNRESOLVED);
		for (URI key : unresolved.keySet()) {
			IMemento mUnresolvable = mUnresolved.createChild(ELEMENT_UNRESOLVABLE);
			mUnresolvable.putString(ATTR_URI, key.toString());
		}
		writeMemento(memento, CONTENT_TYPE_FILE);
	}

	/**
	 * Load a file's properties from the memento node.
	 *
	 * @param memento The memento node.
	 * @return The properties as a map.
	 */
	private Map<QualifiedName, String> loadFileProperties(IMemento memento) {
		Map<QualifiedName, String> properties = Collections
		                .synchronizedMap(new HashMap<QualifiedName, String>());
		IMemento[] children = memento.getChildren(ELEMENT_PROPERTY);
		if (children != null && children.length > 0) {
			for (IMemento child : children) {
				String qualifier = child.getString(ATTR_QUALIFIER);
				String localName = child.getString(ATTR_LOCAL_NAME);
				QualifiedName name = new QualifiedName(qualifier, localName);
				String value = child.getString(ATTR_VALUE);
				properties.put(name, value);
			}
		}
		return properties;
	}

	/**
	 * Read the memento from a memento file using the specified root element name.
	 *
	 * @param mementoFile The memento file.
	 * @param mementoRoot The memento's root element name.
	 * @return A memento of this file or an empty memento if the file does not exist.
	 */
	private IMemento readMemento(String mementoFile, String mementoRoot) {
		File location = CacheManager.getInstance().getCacheRoot();
		File stateFile = new File(location, mementoFile);
		if (stateFile.exists()) {
			BufferedReader reader = null;
			try {
				FileInputStream input = new FileInputStream(stateFile);
				reader = new BufferedReader(new InputStreamReader(input, "utf-8")); //$NON-NLS-1$
				IMemento memento = XMLMemento.createReadRoot(reader);
				return memento;
			}
			catch (IOException e) {
			}
			catch (WorkbenchException e) {
			}
			finally {
				if (reader != null) {
					try {
						reader.close();
					}
					catch (Exception e) {
					}
				}
			}
		}
		return XMLMemento.createWriteRoot(mementoRoot);
	}

	/**
	 * Save the time stamps to the persistent file.
	 */
	private void savePersistentProperties() {
		XMLMemento memento = XMLMemento.createWriteRoot(PERSISTENT_ROOT);
		for (URI key : properties.keySet()) {
			Map<QualifiedName, String> nodeProperties = properties.get(key);
			if (!nodeProperties.keySet().isEmpty()) {
				IMemento mFile = memento.createChild(ELEMENT_FILE);
				mFile.putString(ATTR_URI, key.toString());
				saveFileProperties(mFile, nodeProperties);
			}
		}
		writeMemento(memento, PERSISTENT_FILE);
	}

	/**
	 * Save the file's properties to a memento.
	 *
	 * @param memento The memento object.
	 * @param properties The file properties.
	 */
	private void saveFileProperties(IMemento memento, Map<QualifiedName, String> properties) {
		for (QualifiedName name : properties.keySet()) {
			IMemento mProperty = memento.createChild(ELEMENT_PROPERTY);
			mProperty.putString(ATTR_QUALIFIER, name.getQualifier());
			mProperty.putString(ATTR_LOCAL_NAME, name.getLocalName());
			mProperty.putString(ATTR_VALUE, properties.get(name));
		}
	}

	/**
	 * Write the memento to a memento file.
	 *
	 * @param memento The memento object.
	 * @param mementoFile The file to write to.
	 */
	private void writeMemento(XMLMemento memento, String mementoFile) {
		OutputStreamWriter writer = null;
		try {
			File location = CacheManager.getInstance().getCacheRoot();
			File stateFile = new File(location, mementoFile);
			FileOutputStream stream = new FileOutputStream(stateFile);
			writer = new OutputStreamWriter(stream, "utf-8"); //$NON-NLS-1$
			memento.save(writer);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if (writer != null) {
				try {
					writer.close();
				}
				catch (IOException e) {
				}
			}
		}
	}

	/**
	 * Get the file properties of the specified node from the properties map.
	 *
	 * @param node The file node.
	 * @return The file properties object or empty properties object if it does not exist.
	 */
	public Map<QualifiedName, String> getPersistentProperties(FSTreeNode node) {
		Map<QualifiedName, String> nodeProperties = properties.get(node.getLocationURI());
		if (nodeProperties == null) {
			nodeProperties = Collections.synchronizedMap(new HashMap<QualifiedName, String>());
			properties.put(node.getLocationURI(), nodeProperties);
		}
		return nodeProperties;
	}

	/**
	 * Load the time stamps from the time stamps file in the cache's root directory.
	 */
	private void loadTimestamps() {
		timestamps = Collections.synchronizedMap(new HashMap<URI, Long>());
		File location = CacheManager.getInstance().getCacheRoot();
		File tsFile = new File(location, TIMESTAMP_FILE);
		if (tsFile.exists()) {
			Properties properties = new Properties();
			InputStream input = null;
			try {
				input = new BufferedInputStream(new FileInputStream(tsFile));
				properties.loadFromXML(input);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			finally {
				if (input != null) {
					try {
						input.close();
					}
					catch (IOException e) {
					}
				}
			}
			Enumeration<String> keys = (Enumeration<String>) properties.propertyNames();
			while (keys.hasMoreElements()) {
				String key = keys.nextElement();
				String value = properties.getProperty(key);
				long timestamp = 0L;
				try {
					timestamp = Long.parseLong(value);
					timestamps.put(new URI(key), Long.valueOf(timestamp));
				}
				catch (Exception nfe) {
				}
			}
		}
	}

	/**
	 * Save the time stamps to the time stamps file.
	 */
	private void saveTimestamps() {
		Properties properties = new Properties();
		for (URI key : timestamps.keySet()) {
			Long timestamp = timestamps.get(key);
			properties.setProperty(key.toString(), timestamp.toString());
		}
		File location = CacheManager.getInstance().getCacheRoot();
		File fTimestamp = new File(location, TIMESTAMP_FILE);
		OutputStream output = null;
		try {
			output = new BufferedOutputStream(new FileOutputStream(fTimestamp));
			properties.storeToXML(output, null);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if (output != null) {
				try {
					output.close();
				}
				catch (Exception e) {
				}
			}
		}
	}

	/**
	 * Set the time stamp of the FSTreeNode with the specified location.
	 *
	 * @param uri The FSTreeNode's location URI.
	 * @param timestamp The new base time stamp to be set.
	 */
	public void setBaseTimestamp(URI uri, long timestamp) {
		timestamps.put(uri, Long.valueOf(timestamp));
	}

	/**
	 * Remove the time stamp entry with the specified URI.
	 *
	 * @param uri The URI key.
	 */
	public void removeBaseTimestamp(URI uri) {
		timestamps.remove(uri);
	}

	/**
	 * Get the time stamp of the FSTreeNode with the specified location.
	 *
	 * @param uri The FSTreeNode's location URI.
	 * @return The FSTreeNode's base time stamp.
	 */
	public long getBaseTimestamp(URI uri) {
		Long timestamp = timestamps.get(uri);
		return timestamp == null ? 0L : timestamp.longValue();
	}

	/**
	 * Dispose the cache manager so that it has a chance to save the timestamps and the persistent
	 * properties.
	 */
	public void dispose() {
		saveTimestamps();
		savePersistentProperties();
		saveContentTypes();
	}

	/**
	 * Returns if or if not the persistence manager needs to be disposed.
	 *
	 * @return <code>True</code> if the persistence manager needs disposal, <code>false</code> otherwise.
	 */
	public final static boolean needsDisposal() {
		return instance != null;
	}
}
