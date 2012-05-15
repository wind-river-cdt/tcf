/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * William Chen (Wind River)	[360494]Provide an "Open With" action in the pop
 * 								up menu of file system nodes of Target Explorer.
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.core.internal.utils;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.tcf.te.runtime.persistence.interfaces.IURIPersistenceService;
import org.eclipse.tcf.te.runtime.services.ServiceManager;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;

/**
 * A facility class to load and save persistent data such including resolved content types, file's
 * properties, and time stamps etc.
 */
public class PersistenceManager {
	// The singleton instance.
	private static volatile PersistenceManager instance;

	// The time stamp for each file.
	Map<URI, FileState> digests;

	// Already known resolved content type of file nodes specified by their URIs.
	Map<URI, IContentType> resolved;

	// Already known unresolvable file nodes specified by their URIs.
	Map<URI, URI> unresolved;

	// The persistent properties of the files.
	Map<URI, Map<QualifiedName, String>> properties;
	
	// The file used to store persistent properties of each file.
	private static final String PERSISTENT_FILE = "persistent.ini"; //$NON-NLS-1$

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
	@SuppressWarnings("unchecked")
    private PersistenceManager() {
		SafeRunner.run(new ISafeRunnable(){
			@Override
            public void handleException(Throwable exception) {
				// Ignore on purpose
            }
			@Override
            public void run() throws Exception {
				IURIPersistenceService service = ServiceManager.getInstance()
				                .getService(IURIPersistenceService.class);
				File location = CacheManager.getCacheRoot();
				File resolvedFile = new File(location, "resolved.ini"); //$NON-NLS-1$
				resolved = new HashMap<URI, IContentType>();
				if (resolvedFile.exists()) {
					resolved = (Map<URI, IContentType>) service.read(resolved, resolvedFile
					                .getAbsoluteFile().toURI());
				}
				File unresolvedFile = new File(location, "unresolved.ini");//$NON-NLS-1$
				unresolved = new HashMap<URI, URI>();
				if (unresolvedFile.exists()) {
					unresolved = (Map<URI, URI>) service.read(unresolved, unresolvedFile
					                .getAbsoluteFile().toURI());
				}
				File digestFile = new File(location, "digests.ini");//$NON-NLS-1$
				digests = new HashMap<URI, FileState>();
				if (digestFile.exists()) {
					digests = (Map<URI, FileState>) service.read(digests, digestFile
					                .getAbsoluteFile().toURI());
				}
				File persistentFile = new File(location, PERSISTENT_FILE);
				properties = new HashMap<URI, Map<QualifiedName, String>>();
				if (persistentFile.exists()) {
					properties = (Map<URI, Map<QualifiedName, String>>) service.read(properties, persistentFile.getAbsoluteFile().toURI());
				}
            }});
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
	 * Set the time stamp of the FSTreeNode with the specified location.
	 *
	 * @param uri The FSTreeNode's location URI.
	 * @param digest The new base time stamp to be set.
	 */
	public void setFileDigest(URI uri, FileState digest) {
		digests.put(uri, digest);
	}

	/**
	 * Remove the time stamp entry with the specified URI.
	 *
	 * @param uri The URI key.
	 */
	public void removeFileDigest(URI uri) {
		digests.remove(uri);
	}

	/**
	 * Get the time stamp of the FSTreeNode with the specified location.
	 *
	 * @param uri The FSTreeNode's location URI.
	 * @return The FSTreeNode's base time stamp.
	 */
	public FileState getFileDigest(FSTreeNode node) {
		URI uri = node.getLocationURI();
		FileState digest = digests.get(uri);
		if(digest == null) {
			digest = new FileState(node);
			digests.put(uri, digest);
		}
		digest.setNode(node);
		return digest;
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
	 * Dispose the cache manager so that it has a chance to save the digests and the persistent
	 * properties.
	 */
	public void dispose() {
		SafeRunner.run(new ISafeRunnable(){
			@Override
            public void handleException(Throwable exception) {
				// Ignore on purpose.
            }
			@Override
            public void run() throws Exception {
				IURIPersistenceService service = ServiceManager.getInstance().getService(IURIPersistenceService.class);
				File location = CacheManager.getCacheRoot();
				File resolvedFile = new File(location, "resolved.ini"); //$NON-NLS-1$
				service.write(resolved, resolvedFile.getAbsoluteFile().toURI());
				File unresolvedFile = new File(location, "unresolved.ini"); //$NON-NLS-1$
				service.write(unresolved, unresolvedFile.getAbsoluteFile().toURI());
				File digestFile = new File(location, "digests.ini"); //$NON-NLS-1$
				service.write(digests, digestFile.getAbsoluteFile().toURI());
				File persistentFile = new File(location, PERSISTENT_FILE);
				service.write(properties, persistentFile.getAbsoluteFile().toURI());
            }});
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
