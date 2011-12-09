/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.trees;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;

/**
 * The implementation of the tree viewer extension which is used to parse
 * the columns and the filters declared in this extension for a specified tree viewer.
 */
public class TreeViewerExtension {
	// The extension point id constant.
	private static final String EXTENSION_POINT_ID = "org.eclipse.tcf.te.ui.viewers"; //$NON-NLS-1$
	// The column descriptors from the definitions of defined tree viewer extensions.
	private ColumnDescriptor[] columns;
	// The filter descriptors from the definitions of defined tree viewer extensions.
	private FilterDescriptor[] filterDescriptors;
	// The id of the tree viewer for which the extension is parsed.
	private String viewerId;
	// The tree viewer for which the extension is parsed.
	private TreeViewer viewer;

	/**
	 * Create an instance and parse all tree viewer extensions to get the 
	 * column descriptors and filter descriptors for the specified viewer.
	 * 
	 * @param viewerId The tree viewer's id.
	 * @param viewer The tree viewer to parse the extension for.
	 */
	public TreeViewerExtension(String viewerId, TreeViewer viewer) {
		this.viewerId = viewerId;
		this.viewer = viewer;
		parseExtension();
	}

	/**
	 * Get the column descriptors parsed from the tree viewer extension.
	 * 
	 * @return The column descriptors.
	 */
	public ColumnDescriptor[] getColumns() {
		return columns;
	}

	/**
	 * Get the filter descriptors parsed from the tree viewer extension.
	 * 
	 * @return The filter descriptors.
	 */
	public FilterDescriptor[] getFilterDescriptors() {
		return filterDescriptors;
	}

	/**
	 * Parse the tree viewer's extensions.
	 */
	private void parseExtension() {
		Assert.isNotNull(viewerId);
		List<ColumnDescriptor> columns = Collections.synchronizedList(new ArrayList<ColumnDescriptor>());
		List<FilterDescriptor> descriptors = Collections.synchronizedList(new ArrayList<FilterDescriptor>());
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint(EXTENSION_POINT_ID);
		IConfigurationElement[] configurations = extensionPoint.getConfigurationElements();
		for (IConfigurationElement configuration : configurations) {
			String name = configuration.getName();
			if ("viewer".equals(name)) { //$NON-NLS-1$
				String aViewerId = configuration.getAttribute("viewerId"); //$NON-NLS-1$
				if (viewerId.equals(aViewerId)) {
					IConfigurationElement[] children = configuration.getChildren("column"); //$NON-NLS-1$
					if (children != null && children.length > 0) {
						for (IConfigurationElement child : children) {
							createColumnDescriptor(columns, child, viewer);
						}
					}
					children = configuration.getChildren("filter"); //$NON-NLS-1$
					if (children != null && children.length > 0) {
						for (IConfigurationElement child : children) {
							createFilterDescriptor(descriptors, child);
						}
					}
				}
			}
		}
		this.columns = columns.toArray(new ColumnDescriptor[columns.size()]);
		this.filterDescriptors = descriptors.toArray(new FilterDescriptor[descriptors.size()]);
	}

	/**
	 * Create an filter descriptor from the specified configuration element and 
	 * add it to the filter list.
	 * 
	 * @param descriptors The filter list to add the created descriptor to.
	 * @param configuration The extension configuration element to create the descriptor from.
	 */
	private void createFilterDescriptor(List<FilterDescriptor> descriptors, final IConfigurationElement configuration) {
		String id = configuration.getAttribute("id"); //$NON-NLS-1$
		Assert.isNotNull(id);
		final FilterDescriptor descriptor = new FilterDescriptor();
		descriptor.setId(id);
		descriptors.add(descriptor);
		SafeRunner.run(new SafeRunnable() {
			@Override
			public void run() throws Exception {
				initFilter(descriptor, configuration);
			}
		});
	}

	/**
	 * Initialize the filter descriptor from the specified configuration element.
	 * 
	 * @param descriptor The new descriptor to be initialized.
	 * @param configuration The configuration element to initialize the filter.
	 * @throws CoreException Thrown during parsing.
	 */
	void initFilter(FilterDescriptor descriptor, IConfigurationElement configuration) throws CoreException {
		String attribute = configuration.getAttribute("name"); //$NON-NLS-1$
		Assert.isNotNull(attribute);
		descriptor.setName(attribute);
		attribute = configuration.getAttribute("description"); //$NON-NLS-1$
		if (attribute != null) {
			descriptor.setDescription(attribute);
		}
		attribute = configuration.getAttribute("enabled"); //$NON-NLS-1$
		if (attribute != null) {
			descriptor.setEnabled(Boolean.valueOf(attribute).booleanValue());
		}
		attribute = configuration.getAttribute("class"); //$NON-NLS-1$
		Assert.isNotNull(attribute);
		ViewerFilter filter = (ViewerFilter) configuration.createExecutableExtension("class"); //$NON-NLS-1$
		Assert.isNotNull(filter);
		descriptor.setFilter(filter);
	}

	/**
	 * Create a column descriptor from the specified configuration element and add it to
	 * the column descriptor list.
	 * 
	 * @param columns The column descriptor.
	 * @param configuration The configuration element to read the descriptor from.
	 * @param viewer The tree viewer to add the column to.
	 */
	private void createColumnDescriptor(List<ColumnDescriptor> columns, final IConfigurationElement configuration, final TreeViewer viewer) {
		String id = configuration.getAttribute("id"); //$NON-NLS-1$
		Assert.isNotNull(id);
		final ColumnDescriptor column = new ColumnDescriptor(id);
		columns.add(column);
		SafeRunner.run(new SafeRunnable() {
			@Override
			public void run() throws Exception {
				initColumn(column, configuration, viewer);
			}
		});
	}

	/**
	 * Initialize the column descriptor by reading the attributes from the configuration element.
	 * 
	 * @param column The column descriptor to be initialized.
	 * @param configuration The configuration element.
	 * @param viewer The tree viewer of the column.
	 * @throws CoreException Thrown during parsing.
	 */
	@SuppressWarnings("rawtypes")
	void initColumn(ColumnDescriptor column, IConfigurationElement configuration, TreeViewer viewer) throws CoreException {
		String name = configuration.getAttribute("name"); //$NON-NLS-1$
		Assert.isNotNull(name);
		column.setName(name);
		column.setDescription(configuration.getAttribute("description")); //$NON-NLS-1$
		String attribute = configuration.getAttribute("moveable"); //$NON-NLS-1$
		if (attribute != null) {
			column.setMoveable(Boolean.valueOf(attribute).booleanValue());
		}
		attribute = configuration.getAttribute("resizable"); //$NON-NLS-1$
		if (attribute != null) {
			column.setResizable(Boolean.valueOf(attribute).booleanValue());
		}
		attribute = configuration.getAttribute("visible"); //$NON-NLS-1$
		if (attribute != null) {
			column.setVisible(Boolean.valueOf(attribute).booleanValue());
		}
		attribute = configuration.getAttribute("style"); //$NON-NLS-1$
		if (attribute != null) {
			column.setStyle(parseSWTAttribute(attribute));
		}
		attribute = configuration.getAttribute("alignment"); //$NON-NLS-1$
		if (attribute != null) {
			column.setAlignment(parseSWTAttribute(attribute));
		}
		attribute = configuration.getAttribute("width"); //$NON-NLS-1$
		if (attribute != null) {
			try {
				column.setWidth(Integer.parseInt(attribute));
			}
			catch (NumberFormatException e) {
			}
		}
		attribute = configuration.getAttribute("image"); //$NON-NLS-1$
		if (attribute != null) {
			String symbolicName = configuration.getContributor().getName();
			URL resource = Platform.getBundle(symbolicName).getResource(attribute);
			Image image = ImageDescriptor.createFromURL(resource).createImage();
			column.setImage(image);
		}
		attribute = configuration.getAttribute("labelProvider"); //$NON-NLS-1$
		if (attribute != null) {
			TreeColumnLabelProvider labelProvider = (TreeColumnLabelProvider) configuration.createExecutableExtension("labelProvider"); //$NON-NLS-1$
			if (labelProvider != null) {
				labelProvider.setViewer(viewer);
				column.setLabelProvider(labelProvider);
			}
		}
		attribute = configuration.getAttribute("comparator"); //$NON-NLS-1$
		if (attribute != null) {
			Comparator comparator = (Comparator) configuration.createExecutableExtension("comparator"); //$NON-NLS-1$
			if (comparator != null) {
				column.setComparator(comparator);
			}
		}
	}

	/**
	 * Parse the alignment/style attribute from a string to integer value.
	 * 
	 * @param attribute The attribute value.
	 * @return The alignment/style value.
	 */
	private int parseSWTAttribute(String attribute) {
		if (attribute.equals("SWT.LEFT")) { //$NON-NLS-1$
			return SWT.LEFT;
		}
		else if (attribute.equals("SWT.RIGHT")) { //$NON-NLS-1$
			return SWT.RIGHT;
		}
		else if (attribute.equals("SWT.CENTER")) { //$NON-NLS-1$
			return SWT.CENTER;
		}
		return SWT.LEFT;
	}
}
