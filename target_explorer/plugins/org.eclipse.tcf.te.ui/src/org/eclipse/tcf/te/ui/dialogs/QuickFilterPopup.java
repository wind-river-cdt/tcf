/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.dialogs;

import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.tcf.te.ui.nls.Messages;

/**
 * A pop up dialog to input the filter text for the quick filter.
 * The filter will to listen to the change of the input and
 * filter the tree viewer accordingly.
 */
class QuickFilterPopup extends PopupDialog {
	// The quick filter used filter the content of the tree viewer.
	QuickFilter quickFilter;
	// The text field used to enter filters.
	Text filterText;
	// The initial filter displayed in the filter text field.
	String filter;
	// The tree viewer that it works on.
	TreeViewer treeViewer;
	
	/**
	 * Create a pop up for the specified tree viewer using the quick filter.
	 * 
	 * @param viewer The tree viewer to be filtered.
	 * @param qFilter The quick filter used to filter the tree viewer.
	 */
	public QuickFilterPopup(TreeViewer viewer, QuickFilter qFilter) {
	    super(viewer.getTree().getShell(), SWT.TOOL, true, true, false, false, false, null, null);
	    quickFilter = qFilter;
	    treeViewer = viewer;
	    filter = Messages.QuickFilterPopup_PromptMessage;
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.PopupDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
    protected Control createDialogArea(Composite parent) {
	    Composite composite = (Composite) super.createDialogArea(parent);
	    GridLayout layout = (GridLayout) composite.getLayout();
	    layout.marginHeight = 2;
		filterText = new Text(composite, SWT.SINGLE);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		filterText.setLayoutData(data);
		filterText.setText(filter);
		filterText.setFont(composite.getFont());
		filterText.getAccessible().addAccessibleListener(new AccessibleAdapter() {
			@Override
			public void getName(AccessibleEvent e) {
				getAccessibleName(e);
			}
		});
		filterText.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				filterTextFocusGained(e);
			}
		});
		filterText.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				filterTextMouseUp(e);
			}
		});
		filterText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				filterTextKeyPressed(e);
			}
		});
		// enter key set focus to tree
		filterText.addTraverseListener(new TraverseListener() {
			@Override
			public void keyTraversed(TraverseEvent e) {
				filterTextKeyTraversed(e);
			}
		});
		filterText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				filterTextModifyText(e);
			}
		});
	    return composite;
    }

	/**
	 * Get accessible name for the filter text field.
	 * 
	 * @param e The accessible event.
	 */
	protected void getAccessibleName(AccessibleEvent e) {
		String filterTextString = filterText.getText();
		if (filterTextString.length() == 0) {
			e.result = filter;
		}
		else {
			e.result = filterTextString;
		}
	}

	/**
	 * Called when the filter text field gains focus.
	 * 
	 * @param e The focus event.
	 */
	protected void filterTextFocusGained(FocusEvent e) {
		if (filter.equals(filterText.getText().trim())) {
			filterText.selectAll();
		}
	}

	/**
	 * Called when a mouse up event happens to the filter text field.
	 * 
	 * @param e The mouse up event.
	 */
	protected void filterTextMouseUp(MouseEvent e) {
		if (filter.equals(filterText.getText().trim())) {
			filterText.selectAll();
		}
	}

	/**
	 * Called when a key event happens to the filter text field.
	 * 
	 * @param e The key event.
	 */
	protected void filterTextKeyPressed(KeyEvent e) {
		boolean hasItems = treeViewer.getTree().getItemCount() > 0;
		if (hasItems && e.keyCode == SWT.ARROW_DOWN) {
			treeViewer.getTree().setFocus();
		}
	}

	/**
	 * Called when a traverse event happens to the filter text field.
	 * 
	 * @param e The traverse event.
	 */
	protected void filterTextKeyTraversed(TraverseEvent e) {
		if (e.detail == SWT.TRAVERSE_ESCAPE) {
			quickFilter.resetViewer();
		}
		if (e.detail == SWT.TRAVERSE_RETURN) {
			e.doit = false;
			if (treeViewer.getTree().getItemCount() == 0) {
				Display.getCurrent().beep();
			}
			else {
				// if the initial filter text hasn't changed, do not try to match
				boolean hasFocus = treeViewer.getTree().setFocus();
				boolean textChanged = !filter.equals(filterText.getText().trim());
				if (hasFocus && textChanged && filterText.getText().trim().length() > 0) {
					TreeItem[] items = treeViewer.getTree().getItems();
					for (TreeItem item : items) {
						if (quickFilter.match(item.getText())) {
							treeViewer.getTree().setSelection(new TreeItem[] { item });
							ISelection sel = treeViewer.getSelection();
							treeViewer.setSelection(sel, true);
							break;
						}
					}
				}
			}
		}
	}
	
	/**
	 * Called when a text modification event happens.
	 * 
	 * @param e The modification event.
	 */
	protected void filterTextModifyText(ModifyEvent e) {
		quickFilter.setPattern(filterText.getText());
		treeViewer.refresh();
	}
}
