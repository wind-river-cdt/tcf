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

/**
 * The persistable column state used by TreeViewerState to save and restore the tree viewer's column
 * state.
 */
class ColumnState {
	// The column's id.
	private String columnId;
	// If the column is visible.
	private boolean visible;
	// The column's width.
	private int width;
	// The column's order in the tree header.
	private int order;

	/**
	 * Get the column's id.
	 * 
	 * @return The column's id.
	 */
	public String getColumnId() {
		return columnId;
	}

	/**
	 * Set the column's id.
	 * 
	 * @param columnId The new column Id.
	 */
	public void setColumnId(String columnId) {
		this.columnId = columnId;
	}

	/**
	 * Return if the column is visible.
	 * 
	 * @return true if the column is visible
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * Set the column's visible state.
	 * 
	 * @param visible The new visible state.
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/**
	 * Get the column's width.
	 * 
	 * @return The column's width.
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Set the column's width.
	 * 
	 * @param width The column's width.
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * Get the column's order.
	 * 
	 * @return the column's order.
	 */
	public int getOrder() {
		return order;
	}

	/**
	 * Set the column's order.
	 * 
	 * @param order The column's order.
	 */
	public void setOrder(int order) {
		this.order = order;
	}
}
