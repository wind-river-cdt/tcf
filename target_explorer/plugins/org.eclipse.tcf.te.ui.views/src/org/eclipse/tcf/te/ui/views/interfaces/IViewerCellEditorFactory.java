/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.views.interfaces;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TreeViewer;

/**
 * An interface to provide the cell editing support for Target Explorer in an abstract way.
 * <p>
 * This interface should be implemented by classes that wish to add cell editing support to Target
 * Explorer when it is created.
 */
public interface IViewerCellEditorFactory {
	/**
	 * Initialize this cell editor factory with the tree viewer that is used as the common viewer in
	 * Target Explorer.
	 * 
	 * @param viewer The tree viewer in Target Explorer.
	 */
	void init(TreeViewer viewer);

	/**
	 * Returns the column properties of the viewer. The properties must correspond with the columns
	 * of the table control. They are used to identify the column in a cell modifier.
	 * 
	 * @return the list of column properties
	 * @see org.eclipse.jface.viewers.ColumnViewer#getColumnProperties
	 * @see org.eclipse.jface.viewers.ColumnViewer#setColumnProperties
	 */
	String[] getColumnProperties();

	/**
	 * Return the CellEditors for the viewer, or <code>null</code> if no cell editors are set.
	 * 
	 * @return CellEditor[]
	 * @see org.eclipse.jface.viewers.ColumnViewer#getCellEditors
	 * @see org.eclipse.jface.viewers.ColumnViewer#setCellEditors
	 */
	CellEditor[] getCellEditors();

	/**
	 * Returns the cell modifier of this viewer, or <code>null</code> if none has been set.
	 * 
	 * @return the cell modifier, or <code>null</code>
	 * @see org.eclipse.jface.viewers.ColumnViewer#getCellModifier
	 * @see org.eclipse.jface.viewers.ColumnViewer#setCellModifier
	 */
	ICellModifier getCellModifier();
}
