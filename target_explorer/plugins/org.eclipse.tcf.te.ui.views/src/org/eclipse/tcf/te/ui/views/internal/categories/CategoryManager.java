/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.views.internal.categories;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.tcf.te.runtime.events.ChangeEvent;
import org.eclipse.tcf.te.runtime.events.EventManager;
import org.eclipse.tcf.te.runtime.persistence.interfaces.IURIPersistenceService;
import org.eclipse.tcf.te.runtime.services.ServiceManager;
import org.eclipse.tcf.te.ui.views.activator.UIPlugin;
import org.eclipse.tcf.te.ui.views.extensions.CategoriesExtensionPointManager;
import org.eclipse.tcf.te.ui.views.interfaces.categories.ICategoryManager;


/**
 * Category manager implementation.
 */
public class CategoryManager implements ICategoryManager {
	// The map maintaining the id list per category id
	private Map<String, List<String>> cat2id = new HashMap<String, List<String>>();
	// The map maintaining the category id list per id
	private Map<String, List<String>> id2cat = new HashMap<String, List<String>>();

	// The map maintaining the transient id list per category id
	private final Map<String, List<String>> _t_cat2id = new HashMap<String, List<String>>();
	// The map maintaining the transient category id list per id
	private final Map<String, List<String>> _t_id2cat = new HashMap<String, List<String>>();

	/**
	 * Constructor.
	 */
	public CategoryManager() {
		super();
		initialize();
	}

	/**
	 * Returns the root path where to persist the maps between the sessions.
	 *
	 * @return The root path or <code>null</code>.
	 */
	private IPath getRoot() {
		try {
			File file = UIPlugin.getDefault().getStateLocation().toFile();
			boolean exists = file.exists();
			if (!exists) {
				exists = file.mkdirs();
			}
			if (exists && file.canRead() && file.isDirectory()) {
				return new Path(file.toString());
			}
		} catch (IllegalStateException e) {
			/* ignored on purpose */
		}

		File file = new Path(System.getProperty("user.home")).append(".tcf").toFile(); //$NON-NLS-1$ //$NON-NLS-2$
		if (file.canRead() && file.isDirectory()) {
			return new Path(file.toString());
		}

		return null;
	}

	/**
	 * Initialize the category manager.
	 */
	private void initialize() {
		IPath root = getRoot();
		if (root == null) {
			return;
		}

		// Clear out the transient maps
		_t_cat2id.clear();
		_t_id2cat.clear();

		cat2id.clear();
		id2cat.clear();

		try {
			// Get the persistence service
			IURIPersistenceService uRIPersistenceService = ServiceManager.getInstance().getService(IURIPersistenceService.class);
			if (uRIPersistenceService == null) {
				throw new IOException("Persistence service instance unavailable."); //$NON-NLS-1$
			}
			// Save the history to the persistence storage
			cat2id = (Map<String,List<String>>)uRIPersistenceService.read(cat2id, root.append("cat2id.json").toFile().toURI()); //$NON-NLS-1$
		} catch (IOException e) {
		}

		try {
			// Get the persistence service
			IURIPersistenceService uRIPersistenceService = ServiceManager.getInstance().getService(IURIPersistenceService.class);
			if (uRIPersistenceService == null) {
				throw new IOException("Persistence service instance unavailable."); //$NON-NLS-1$
			}
			// Save the history to the persistence storage
			id2cat = (Map<String,List<String>>)uRIPersistenceService.read(id2cat, root.append("id2cat.json").toFile().toURI()); //$NON-NLS-1$
		} catch (IOException e) {
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.categories.ICategoryManager#flush()
	 */
	@Override
	public void flush() {
		IPath root = getRoot();
		if (root == null) {
			return;
		}

		try {
			// Get the persistence service
			IURIPersistenceService uRIPersistenceService = ServiceManager.getInstance().getService(IURIPersistenceService.class);
			if (uRIPersistenceService == null) {
				throw new IOException("Persistence service instance unavailable."); //$NON-NLS-1$
			}
			// Save the history to the persistence storage
			uRIPersistenceService.write(cat2id, root.append("cat2id.json").toFile().toURI()); //$NON-NLS-1$
		} catch (IOException e) {
		}

		try {
			// Get the persistence service
			IURIPersistenceService uRIPersistenceService = ServiceManager.getInstance().getService(IURIPersistenceService.class);
			if (uRIPersistenceService == null) {
				throw new IOException("Persistence service instance unavailable."); //$NON-NLS-1$
			}
			// Save the history to the persistence storage
			uRIPersistenceService.write(id2cat, root.append("id2cat.json").toFile().toURI()); //$NON-NLS-1$
		} catch (IOException e) {
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.categories.ICategoryManager#add(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean add(String categoryId, String id) {
		Assert.isNotNull(categoryId);
		Assert.isNotNull(id);

		boolean added = false;

		List<String> ids = cat2id.get(categoryId);
		if (ids == null) {
			ids = new ArrayList<String>();
			cat2id.put(categoryId, ids);
		}
		if (!ids.contains(id)) {
			added |= ids.add(id);
		}

		List<String> cats = id2cat.get(id);
		if (cats == null) {
			cats = new ArrayList<String>();
			id2cat.put(id, cats);
		}
		if (!cats.contains(categoryId)) {
			added |= cats.add(categoryId);
		}

		if (added) {
			flush();
			EventManager.getInstance().fireEvent(
							new ChangeEvent(CategoriesExtensionPointManager.getInstance().getCategory(categoryId, false), ChangeEvent.ID_ADDED, null, id));
		}

		return added;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.interfaces.categories.ICategoryManager#addTransient(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean addTransient(String categoryId, String id) {
		Assert.isNotNull(categoryId);
		Assert.isNotNull(id);

		boolean added = false;

		List<String> ids = _t_cat2id.get(categoryId);
		if (ids == null) {
			ids = new ArrayList<String>();
			_t_cat2id.put(categoryId, ids);
		}
		if (!ids.contains(id)) {
			added |= ids.add(id);
		}

		List<String> cats = _t_id2cat.get(id);
		if (cats == null) {
			cats = new ArrayList<String>();
			_t_id2cat.put(id, cats);
		}
		if (!cats.contains(categoryId)) {
			added |= cats.add(categoryId);
		}

		if (added) {
			EventManager.getInstance().fireEvent(
							new ChangeEvent(CategoriesExtensionPointManager.getInstance().getCategory(categoryId, false), ChangeEvent.ID_ADDED, null, id));
		}

		return added;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.categories.ICategoryManager#remove(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean remove(String categoryId, String id) {
		Assert.isNotNull(categoryId);
		Assert.isNotNull(id);

		boolean removed = false;

		List<String> ids = cat2id.get(categoryId);
		if (ids != null) {
			removed |= ids.remove(id);
			if (ids.isEmpty()) {
				cat2id.remove(categoryId);
			}
		}
		ids = _t_cat2id.get(categoryId);
		if (ids != null) {
			removed |= ids.remove(id);
			if (ids.isEmpty()) {
				_t_cat2id.remove(categoryId);
			}
		}

		List<String> cats = id2cat.get(id);
		if (cats != null) {
			removed |= cats.remove(categoryId);
			if (cats.isEmpty()) {
				id2cat.remove(id);
			}
		}
		cats = _t_id2cat.get(id);
		if (cats != null) {
			removed |= cats.remove(categoryId);
			if (cats.isEmpty()) {
				_t_id2cat.remove(id);
			}
		}

		if (removed) {
			flush();
			EventManager.getInstance().fireEvent(
							new ChangeEvent(CategoriesExtensionPointManager.getInstance().getCategory(categoryId, false), ChangeEvent.ID_REMOVED, id, null));
		}

		return removed;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.categories.ICategoryManager#belongsTo(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean belongsTo(String categoryId, String id) {
		Assert.isNotNull(categoryId);
		Assert.isNotNull(id);

		boolean belongsTo = false;

		List<String> ids = cat2id.get(categoryId);
		if (ids != null && ids.contains(id)) {
			belongsTo = true;
		} else {
			ids = _t_cat2id.get(categoryId);
			if (ids != null && ids.contains(id)) {
				belongsTo = true;
			}
		}

		return belongsTo;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.categories.ICategoryManager#getCategoryIds(java.lang.String)
	 */
	@Override
	public String[] getCategoryIds(String id) {
		Assert.isNotNull(id);

		List<String> allCategories = new ArrayList<String>();

		List<String> cats = id2cat.get(id);
		if (cats != null) {
			allCategories.addAll(cats);
		}

		cats = _t_id2cat.get(id);
		if (cats != null) {
			for (String cat : cats) {
				if (!allCategories.contains(cat)) {
					allCategories.add(cat);
				}
			}
		}

		return allCategories.toArray(new String[allCategories.size()]);
	}
}
