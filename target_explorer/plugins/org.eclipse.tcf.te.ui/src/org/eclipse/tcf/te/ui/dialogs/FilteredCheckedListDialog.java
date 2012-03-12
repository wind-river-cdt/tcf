/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.dialogs;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tcf.te.ui.activator.UIPlugin;
import org.eclipse.tcf.te.ui.interfaces.IDescriptionLabelProvider;
import org.eclipse.ui.dialogs.SelectionStatusDialog;

/**
 * A selection dialog with a checked list and a filter text field. 
 */
public class FilteredCheckedListDialog extends SelectionStatusDialog implements ISelectionChangedListener {
	// The pattern filter used filter the content of the list.
	TablePatternFilter patternFilter;
	// The text field used to enter filters.
	Text filterText;
	// The check-box style table used to display a list and select elements. 
	CheckboxTableViewer tableViewer;
	// The label provider used to provide labels, images and descriptions for the listed items.
	IDescriptionLabelProvider labelProvider;
	// The initial filter displayed in the filter text field.
	String filter;
	// The elements to be selected.
	Object[] elements;
	// Currently selected items.
	Set<Object> checkedItems;

	/**
	 * Constructor used to instantiate a dialog with a specified parent.
	 * 
	 * @param shell The parent shell.
	 */
	public FilteredCheckedListDialog(Shell shell) {
		super(shell);
		checkedItems = new HashSet<Object>();
	}

	/**
	 * Set the initial filter text to be displayed in the filter text field.
	 * 
	 * @param filterText The initial filter text.
	 */
	public void setFilterText(String filterText) {
		this.filter = filterText;
	}

	/**
	 * Set the label provider used to provide labels, images and descriptive text
	 * for items in the list.
	 * 
	 * @param labelProvider The new label provider.
	 */
	public void setLabelProvider(IDescriptionLabelProvider labelProvider) {
		this.labelProvider = labelProvider;
	}

	/**
	 * Set elements to be displayed in the list for selection.
	 * 
	 * @param elements The elements.
	 */
	public void setElements(Object[] elements) {
		this.elements = elements;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		IStructuredSelection structuredSelection = (IStructuredSelection) event.getSelection();
		Object element = structuredSelection.getFirstElement();
		String description = labelProvider.getDescription(element);
		if (description == null) description = element == null ? "" : "Enable "+labelProvider.getText(element)+"."; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		updateStatus(new Status(IStatus.OK, UIPlugin.getUniqueIdentifier(), description));
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.SelectionStatusDialog#computeResult()
	 */
	@Override
	protected void computeResult() {
		setSelectionResult(checkedItems.toArray());
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		createMessageArea(composite);
		createPatternFilterText(composite);
		createTable(composite);
		initializeData();
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
		boolean hasItems = tableViewer.getTable().getItemCount() > 0;
		if (hasItems && e.keyCode == SWT.ARROW_DOWN) {
			tableViewer.getTable().setFocus();
		}
	}

	/**
	 * Called when a traverse event happens to the filter text field.
	 * 
	 * @param e The traverse event.
	 */
	protected void filterTextKeyTraversed(TraverseEvent e) {
		if (e.detail == SWT.TRAVERSE_RETURN) {
			e.doit = false;
			if (tableViewer.getTable().getItemCount() == 0) {
				Display.getCurrent().beep();
			}
			else {
				// if the initial filter text hasn't changed, do not try to match
				boolean hasFocus = tableViewer.getTable().setFocus();
				boolean textChanged = !filter.equals(filterText.getText().trim());
				if (hasFocus && textChanged && filterText.getText().trim().length() > 0) {
					TableItem[] items = tableViewer.getTable().getItems();
					for (TableItem item : items) {
						if (patternFilter.match(item.getText())) {
							tableViewer.getTable().setSelection(new TableItem[] { item });
							ISelection sel = tableViewer.getSelection();
							tableViewer.setSelection(sel, true);
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
		patternFilter.setPattern(filterText.getText());
		tableViewer.refresh();
		for (Object item : checkedItems) {
			tableViewer.setChecked(item, true);
		}
	}

	/**
	 * Called when a list item is checked or unchecked.
	 * 
	 * @param event The check event.
	 */
	protected void tableCheckStateChanged(CheckStateChangedEvent event) {
		if (event.getChecked()) checkedItems.add(event.getElement());
		else checkedItems.remove(event.getElement());
	}
	
	/**
	 * Create the filter text field and add listeners to respond to control input.
	 * 
	 * @param composite The parent composite.
	 */
	private void createPatternFilterText(Composite composite) {
		filterText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		GridData filterTextGridData = new GridData(GridData.FILL_HORIZONTAL);
		filterText.setLayoutData(filterTextGridData);
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
	}

	/**
	 * Create the checked list and add check state changed listener to monitor events.
	 * 
	 * @param composite The parent composite.
	 */
	private void createTable(Composite composite) {
		tableViewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		tableViewer.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				tableCheckStateChanged(event);
			}
		});
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = 120;
		data.widthHint = 200;
		tableViewer.getTable().setLayoutData(data);
		
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		tableViewer.setLabelProvider(labelProvider);
		patternFilter = new TablePatternFilter(labelProvider);
		tableViewer.addFilter(patternFilter);
		tableViewer.addSelectionChangedListener(this);
	}

	/**
	 * Initialize the list items in the table and force focus to it.
	 */
	private void initializeData() {
		tableViewer.setInput(elements);
		List<Object> selection = getInitialElementSelections();
		if (selection != null) {
			for (Object element : selection) {
				tableViewer.setChecked(element, true);
				checkedItems.add(element);
			}
		}
		filterText.forceFocus();
	}
}