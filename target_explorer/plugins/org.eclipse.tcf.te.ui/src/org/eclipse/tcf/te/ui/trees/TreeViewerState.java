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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ui.IMemento;

/**
 * The class to describe the tree viewer's state including the visiblity of the 
 * tree columns and the enablement of the viewer filters.
 */
class TreeViewerState {
	// The state of the column's visibility.
	private List<ColumnState> columns;
	// The state of the filter's enablement.
	private List<FilterState> filters;

	/**
	 * Create an instance.
	 */
	public TreeViewerState() {
		columns = Collections.synchronizedList(new ArrayList<ColumnState>());
		filters = Collections.synchronizedList(new ArrayList<FilterState>());
	}

	/**
	 * Restore the viewer's state using the specified memento.
	 * 
	 * @param aMemento The memento to restore the viewer's state.
	 */
	public void restoreState(IMemento aMemento) {
		IMemento[] mColumns = aMemento.getChildren("column"); //$NON-NLS-1$
		if (mColumns != null && mColumns.length > 0) {
			for (IMemento mColumn : mColumns) {
				String columnId = mColumn.getString("id"); //$NON-NLS-1$
				Assert.isNotNull(columnId);
				Boolean value = mColumn.getBoolean("visible"); //$NON-NLS-1$
				boolean visible = value != null && value.booleanValue();
				Integer integer = mColumn.getInteger("width"); //$NON-NLS-1$
				int width = integer.intValue();
				integer = mColumn.getInteger("order"); //$NON-NLS-1$
				int order = integer.intValue();
				ColumnState column = new ColumnState();
				column.setColumnId(columnId);
				column.setVisible(visible);
				column.setWidth(width);
				column.setOrder(order);
				columns.add(column);
			}
		}
		IMemento[] mFilters = aMemento.getChildren("filter"); //$NON-NLS-1$
		if (mFilters != null && mFilters.length > 0) {
			for (IMemento mFilter : mFilters) {
				String filterId = mFilter.getString("id"); //$NON-NLS-1$
				Assert.isNotNull(filterId);
				Boolean value = mFilter.getBoolean("enabled"); //$NON-NLS-1$
				boolean enabled = value != null && value.booleanValue();
				FilterState filter = new FilterState();
				filter.setFilterId(filterId);
				filter.setEnabled(enabled);
				filters.add(filter);
			}
		}
	}

	/**
	 * Save the viewer's state to the specified memento.
	 * 
	 * @param aMemento The memento to save the viewer's state to.
	 */
	public void saveState(IMemento aMemento) {
		if (columns != null) {
			for (ColumnState column : columns) {
				String columnId = column.getColumnId();
				IMemento mColumn = aMemento.createChild("column"); //$NON-NLS-1$
				mColumn.putString("id", columnId); //$NON-NLS-1$
				boolean visible = column.isVisible();
				mColumn.putBoolean("visible", visible); //$NON-NLS-1$
				int width = column.getWidth();
				mColumn.putInteger("width", width); //$NON-NLS-1$
				int order = column.getOrder();
				mColumn.putInteger("order", order); //$NON-NLS-1$
			}
		}
		if (filters != null) {
			for (FilterState filter : filters) {
				IMemento mFilter = aMemento.createChild("filter"); //$NON-NLS-1$
				mFilter.putString("id", filter.getFilterId()); //$NON-NLS-1$
				boolean enabled = filter.isEnabled();
				mFilter.putBoolean("enabled", enabled); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Add a column state based on the specified column descriptor.
	 * 
	 * @param column The column's descriptor.
	 */
	public void addColumn(ColumnDescriptor column) {
		ColumnState state = new ColumnState();
		state.setColumnId(column.getId());
		state.setVisible(column.isVisible());
		state.setWidth(column.getWidth());
		state.setOrder(column.getOrder());
		columns.add(state);
    }

	/**
	 * Add a filter state based on the specified filter descriptor.
	 * 
	 * @param filter The filter's state.
	 */
	public void addFilter(FilterDescriptor filter) {
		FilterState state = new FilterState();
		state.setFilterId(filter.getId());
		state.setEnabled(filter.isEnabled());
		filters.add(state);
    }

	/**
	 * Get the column's state in a list.
	 * 
	 * @return The column's state list.
	 */
	public List<ColumnState> getColumnStates() {
		return columns;
	}
	
	/**
	 * Update the column descriptors using the current column states.
	 * 
	 * @param columnDescriptors The column descriptors to be updated.
	 */
	public void updateColumnDescriptor(ColumnDescriptor[] columnDescriptors) {
		if (columnDescriptors != null) {
			for(int i=0;i<columns.size();i++) {
				ColumnDescriptor columnDescriptor = columnDescriptors[i];
				ColumnState columnState = columns.get(i);
				columnDescriptor.setVisible(columnState.isVisible());
				columnDescriptor.setWidth(columnState.getWidth());
				columnDescriptor.setOrder(columnState.getOrder());
			}
		}
    }

	/**
	 * Update the filter descriptors using the current filter states.
	 * 
	 * @param filterDescriptors The filter descriptors to be updated.
	 */
	public void updateFilterDescriptor(FilterDescriptor[] filterDescriptors) {
		if (filterDescriptors != null) {
			for (int i=0;i<filters.size();i++) {
				FilterDescriptor filterDescriptor = filterDescriptors[i];
				FilterState filterState = filters.get(i);
				filterDescriptor.setEnabled(filterState.isEnabled());
			}
		}
    }

	/**
	 * Update the current column states using the specified column descriptors.
	 * 
	 * @param columnDescriptors The column descriptors which are used to update the column states.
	 */
	public void updateColumnState(ColumnDescriptor[] columnDescriptors) {
		if(columnDescriptors != null) {
			for(int i=0;i<columns.size();i++) {
				ColumnDescriptor columnDescriptor = columnDescriptors[i];
				ColumnState columnState = columns.get(i);
				columnState.setVisible(columnDescriptor.isVisible());
				columnState.setWidth(columnDescriptor.getWidth());
				columnState.setOrder(columnDescriptor.getOrder());
			}
		}
    }

	/**
	 * Update the current filter states using the specified filter descriptors.
	 * 
	 * @param filterDescriptors The filter descriptors which are used to update the filter states.
	 */
	public void updateFilterState(FilterDescriptor[] filterDescriptors) {
		if (filterDescriptors != null) {
			for(int i=0;i<filters.size();i++) {
				FilterDescriptor filterDescriptor = filterDescriptors[i];
				FilterState filterState = filters.get(i);
				filterState.setEnabled(filterDescriptor.isEnabled());
			}
		}	    
    }
}
