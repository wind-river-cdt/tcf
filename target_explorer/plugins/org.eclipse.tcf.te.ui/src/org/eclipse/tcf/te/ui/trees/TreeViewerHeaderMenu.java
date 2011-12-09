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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.tcf.te.ui.trees.ColumnDescriptor;

/**
 * The header context menu for the execution context viewer. This context menu
 * provides the list of columns that users can configure them by simply checking
 * or unchecking them.
 */
public class TreeViewerHeaderMenu extends Menu implements SelectionListener, Listener, DisposeListener {
	//The menu control used to configure the columns.
	private Menu treeMenu;
	//The execution context tree to be configured.
	private Tree tree;

	/**
	 * Create a header menu for the execution context viewer.
	 * 
	 * @param tree The execution context tree.
	 */
	public TreeViewerHeaderMenu(Tree tree) {
		super(tree);
		this.tree = tree;
		this.tree.addListener(SWT.MenuDetect, this);
		this.tree.addDisposeListener(this);
		treeMenu = this.tree.getMenu();
	}

	/**
	 * Create the menu.
	 */
	public void create() {
		int count = tree.getColumnCount();
		if (count > 0) {
			for (int i = 0; i < count; i++) {
				createMenuItem(i);
			}
		}
	}

	/**
	 * Create the menu item for the column with the specified index.
	 * 
	 * @param columnIndex The column's index.
	 */
	private void createMenuItem(int columnIndex) {
		TreeColumn treeColumn = tree.getColumn(columnIndex);
		ColumnDescriptor column = (ColumnDescriptor) treeColumn.getData();
		MenuItem menuItem = new MenuItem(this, SWT.CHECK);
		menuItem.setText(column.getName());
		menuItem.setSelection(column.isVisible());
		menuItem.addSelectionListener(this);
		menuItem.setData(column);
		menuItem.setEnabled(columnIndex != 0);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
    public void widgetSelected(SelectionEvent e) {
		MenuItem item = (MenuItem) e.getSource();
		ColumnDescriptor column = (ColumnDescriptor) item.getData();
		column.setVisible(item.getSelection());
		column.getTreeColumn().setWidth(column.isVisible() ? column.getWidth() : 0);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
    public void widgetDefaultSelected(SelectionEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 */
	@Override
    public void handleEvent(Event event) {
		Tree t = (Tree) event.widget;
		Point pt = t.getDisplay().map(null, t, event.x, event.y);
		Rectangle clientArea = t.getClientArea();
		boolean isHeader = ((pt.y - clientArea.y) <= t.getHeaderHeight());
		t.setMenu(isHeader ? this : treeMenu);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
	 */
	@Override
    public void widgetDisposed(DisposeEvent e) {
		if (treeMenu != null && !treeMenu.isDisposed())
			treeMenu.dispose();
		if (!isDisposed())
			dispose();
	}
	
	/**
	 * Override the super method to allow the subclassing.
	 */
	@Override
	protected void checkSubclass() {
	}
}
