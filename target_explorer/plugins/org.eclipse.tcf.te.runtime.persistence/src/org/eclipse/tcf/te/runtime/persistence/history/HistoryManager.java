/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.runtime.persistence.history;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.tcf.te.runtime.events.ChangeEvent;
import org.eclipse.tcf.te.runtime.events.EventManager;
import org.eclipse.tcf.te.runtime.persistence.activator.CoreBundleActivator;
import org.eclipse.tcf.te.runtime.persistence.interfaces.IURIPersistenceService;
import org.eclipse.tcf.te.runtime.services.ServiceManager;

/**
 * History manager implementation.
 */
public class HistoryManager {
	// the maximum length of the history per id
	private final static int HISTORY_LENGTH = 5;

	// The map maintaining the history
	private Map<String, List<String>> history = new HashMap<String, List<String>>();

	/*
	 * Thread save singleton instance creation.
	 */
	private static class LazyInstance {
		public static HistoryManager instance = new HistoryManager();
	}

	/**
	 * Returns the singleton instance of the history point manager.
	 */
	public static HistoryManager getInstance() {
		return LazyInstance.instance;
	}

	/**
	 * Constructor.
	 */
	HistoryManager() {
		super();
		initialize();
	}

	/**
	 * Initialize the history manager.
	 */
	private void initialize() {
		history.clear();
		try {
			// Get the persistence service
			IURIPersistenceService uRIPersistenceService = ServiceManager.getInstance().getService(IURIPersistenceService.class);
			if (uRIPersistenceService == null) {
				throw new IOException("Persistence service instance unavailable."); //$NON-NLS-1$
			}
			// Save the history to the persistence storage
			history = (Map<String,List<String>>)uRIPersistenceService.read(history, getURI());
		} catch (IOException e) {
		}
	}

	// Get the URI for history persistence
	private URI getURI() {
		IPath pluginPath = CoreBundleActivator.getDefault().getStateLocation();
		pluginPath = pluginPath.append(".history"); //$NON-NLS-1$

		return pluginPath.toFile().toURI();
	}

	/**
	 * Write the history to disk.
	 */
	public void flush() {
		try {
			// Get the persistence service
			IURIPersistenceService uRIPersistenceService = ServiceManager.getInstance().getService(IURIPersistenceService.class);
			if (uRIPersistenceService == null) {
				throw new IOException("Persistence service instance unavailable."); //$NON-NLS-1$
			}
			// Save the history to the persistence storage
			uRIPersistenceService.write(history, getURI());
		} catch (IOException e) {
		}
	}

	/**
	 * Get the history for a given history id.
	 * @param historyId The history id.
	 * @return The list of ids within the history ids list or an empty list.
	 */
	public String[] getHistory(String historyId) {
		Assert.isNotNull(historyId);

		List<String> ids = history.get(historyId);
		if (ids == null) {
			ids = new ArrayList<String>();
		}

		return ids.toArray(new String[ids.size()]);
	}

	/**
	 * Get the fist entry of a history ids list.
	 * @param historyId The history id.
	 * @return The first entry of the history ids list or null if no history is available for that id.
	 */
	public String getFirst(String historyId) {
		String[] history = getHistory(historyId);
		return history.length > 0 ? history[0] : null;
	}

	/**
	 * Add a new history entry to the top of the history ids list.
	 * If the list size exceeds the HISTORY_LENGTH, the last element of the list will be removed.
	 * @param historyId The history id.
	 * @param id The id to be added to the top of history ids list.
	 * @return <code>true</code> if the id
	 */
	public boolean add(String historyId, String id) {
		Assert.isNotNull(historyId);
		Assert.isNotNull(id);

		List<String> ids = history.get(historyId);
		if (ids == null) {
			ids = new ArrayList<String>();
			history.put(historyId, ids);
		}
		if (ids.contains(id)) {
			ids.remove(id);
		}

		ids.add(0, id);

		while (ids.size() > HISTORY_LENGTH) {
			ids.remove(HISTORY_LENGTH);
		}

		flush();

		EventManager.getInstance().fireEvent(new ChangeEvent(this, ChangeEvent.ID_ADDED, historyId, historyId));

		return true;
	}

	/**
	 * Remove a id from the history ids list.
	 * @param historyId The history id.
	 * @param id The id to be removed from the history ids list.
	 * @return <code>true</code> if the id was removed from the history ids list.
	 */
	public boolean remove(String historyId, String id) {
		Assert.isNotNull(historyId);
		Assert.isNotNull(id);

		boolean removed = false;

		List<String> ids = history.get(historyId);
		if (ids != null) {
			removed |= ids.remove(id);
			if (ids.isEmpty()) {
				history.remove(historyId);
			}
		}

		if (removed) {
			flush();
			EventManager.getInstance().fireEvent(new ChangeEvent(this, ChangeEvent.ID_REMOVED, historyId, historyId));
		}

		return removed;
	}

}
