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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.tcf.te.ui.views.activator.UIPlugin;
import org.eclipse.tcf.te.ui.views.interfaces.categories.ICategoryManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * Category manager implementation.
 */
public class CategoryManager implements ICategoryManager {
	// The map maintaining the id list per category id
	private final Map<String, List<String>> cat2id = new HashMap<String, List<String>>();
	// The map maintaining the category id list per id
	private final Map<String, List<String>> id2cat = new HashMap<String, List<String>>();

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
		if (root == null) return;

		// Clear out the transient maps
		_t_cat2id.clear();
		_t_id2cat.clear();

		// Create the Gson instance
		Gson gson = new GsonBuilder().create();

		// The first file to read is the category to id list map
		File file = root.append("cat2id.json").toFile(); //$NON-NLS-1$
		try {
			cat2id.clear();
			Reader reader = null;
			try {
				reader = new InputStreamReader(new FileInputStream(file), "UTF-8"); //$NON-NLS-1$
				cat2id.putAll(gson.fromJson(reader, Map.class));
			} finally {
				if (reader != null) reader.close();
			}
		} catch (IOException e) {
			/* ignored on purpose */
		}

		// The second file to read is the id to category list map
		file = root.append("id2cat.json").toFile(); //$NON-NLS-1$
		try {
			id2cat.clear();
			Reader reader = null;
			try {
				reader = new InputStreamReader(new FileInputStream(file), "UTF-8"); //$NON-NLS-1$
				id2cat.putAll(gson.fromJson(reader, Map.class));
			} finally {
				if (reader != null) reader.close();
			}
		} catch (IOException e) {
			/* ignored on purpose */
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.categories.ICategoryManager#flush()
	 */
	@Override
    public void flush() {
		IPath root = getRoot();
		if (root == null) return;

		// Create the Gson instance
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		// The first file to write is the category to id list map
		File file = root.append("cat2id.json").toFile(); //$NON-NLS-1$
		try {
			Writer writer = null;
			try {
				writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8"); //$NON-NLS-1$
				gson.toJson(cat2id, Map.class, writer);
			} finally {
				if (writer != null) writer.close();
			}
		} catch (IOException e) {
			/* ignored on purpose */
		}

		// The second file to write is the id to category list map
		file = root.append("id2cat.json").toFile(); //$NON-NLS-1$
		try {
			Writer writer = null;
			try {
				writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8"); //$NON-NLS-1$
				gson.toJson(id2cat, Map.class, writer);
			} finally {
				if (writer != null) writer.close();
			}
		} catch (IOException e) {
			/* ignored on purpose */
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
		if (!ids.contains(id)) added |= ids.add(id);

		List<String> cats = id2cat.get(id);
		if (cats == null) {
			cats = new ArrayList<String>();
			id2cat.put(id, cats);
		}
		if (!cats.contains(categoryId)) added |= cats.add(categoryId);

		if (added) flush();

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
		if (!ids.contains(id)) added |= ids.add(id);

		List<String> cats = _t_id2cat.get(id);
		if (cats == null) {
			cats = new ArrayList<String>();
			_t_id2cat.put(id, cats);
		}
		if (!cats.contains(categoryId)) added |= cats.add(categoryId);

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
			if (ids.isEmpty()) cat2id.remove(categoryId);
		}
		ids = _t_cat2id.get(categoryId);
		if (ids != null) {
			removed |= ids.remove(id);
			if (ids.isEmpty()) _t_cat2id.remove(categoryId);
		}

		List<String> cats = id2cat.get(id);
		if (cats != null) {
			removed |= cats.remove(categoryId);
			if (cats.isEmpty()) id2cat.remove(id);
		}
		cats = _t_id2cat.get(id);
		if (cats != null) {
			removed |= cats.remove(categoryId);
			if (cats.isEmpty()) _t_id2cat.remove(id);
		}

		if (removed) flush();

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
		if (cats != null) allCategories.addAll(cats);

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
