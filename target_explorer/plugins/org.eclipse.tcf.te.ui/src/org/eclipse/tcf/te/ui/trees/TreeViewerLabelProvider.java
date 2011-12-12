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

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * File system tree control label provider implementation.
 */
public class TreeViewerLabelProvider extends LabelProvider implements ITableLabelProvider {
	// Reference to the parent tree viewer
	private TreeViewer viewer;

	/**
	 * Constructor.
	 *
	 * @param viewer The tree viewer or <code>null</code>.
	 */
	public TreeViewerLabelProvider(TreeViewer viewer) {
		super();
		this.viewer = viewer;
	}
	
	/**
	 * Get the specific column's ContextColumn object.
	 * 
	 * @param columnIndex the column index.
	 * @return The ContextColumn object describing the column.
	 */
	private ColumnDescriptor getColumn(int columnIndex) {
		Tree tree = viewer.getTree();
		TreeColumn column = tree.getColumn(columnIndex);
		ColumnDescriptor context = (ColumnDescriptor) column.getData();
		return context;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element) {
		return getColumnText(element, 0);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
	 */
	@Override
	public Image getImage(Object element) {
		return getColumnImage(element, 0);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
	 */
	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		ColumnDescriptor column = getColumn(columnIndex);
		ILabelProvider labelProvider = column.getLabelProvider();
		if(labelProvider != null) {
			return labelProvider.getImage(element);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
	 */
	@Override
	public String getColumnText(Object element, int columnIndex) {
		ColumnDescriptor column = getColumn(columnIndex);
		ILabelProvider labelProvider = column.getLabelProvider();
		if(labelProvider != null) {
			return labelProvider.getText(element);
		}
		return ""; //$NON-NLS-1$
	}
}
