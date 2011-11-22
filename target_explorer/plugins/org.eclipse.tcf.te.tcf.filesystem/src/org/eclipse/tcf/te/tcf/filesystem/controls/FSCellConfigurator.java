/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * William Chen (Wind River) - [361324] Add more file operations in the file 
 * 												system of Target Explorer.
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.controls;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.tcf.te.tcf.filesystem.internal.celleditor.FSCellListener;
import org.eclipse.tcf.te.tcf.filesystem.internal.celleditor.FSCellModifier;
import org.eclipse.tcf.te.tcf.filesystem.internal.celleditor.FSCellValidator;
import org.eclipse.tcf.te.tcf.filesystem.internal.handlers.RenameFilesHandler;
import org.eclipse.tcf.te.ui.views.interfaces.IViewerConfigurator;
import org.eclipse.ui.navigator.CommonViewer;

/**
 * FSCellConfigurator implements <code>IViewerConfigurator</code> to configure the
 * editor for renaming in the file system tree of Target Explorer.
 */
public class FSCellConfigurator implements IViewerConfigurator {
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.interfaces.IViewerConfigurator#configure(org.eclipse.ui.navigator.CommonViewer)
	 */
	@Override
	public void configure(final CommonViewer viewer) {
		addEditingSupport(viewer);
	}

	/**
	 * Add the editing support for the specified tree viewer with those editing assistants which
	 * have the specified viewId.
	 * 
	 * @param viewer The tree viewer to which the editing support is added.
	 */
	static void addEditingSupport(final TreeViewer viewer) {
		// Define an editor activation strategy for the common viewer so as to be invoked only programmatically.
		ColumnViewerEditorActivationStrategy activationStrategy = new ColumnViewerEditorActivationStrategy(viewer) {
			@Override
			protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
				// Activated only when a single node is selected and invoked programmatically.
				boolean singleSelect = ((IStructuredSelection) viewer.getSelection()).size() == 1;
				return singleSelect && (event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC);
			}
		};
		TreeViewerEditor.create(viewer, null, activationStrategy, ColumnViewerEditor.DEFAULT);
		
		// Set the column properties, the cell editor, and the modifier.
		viewer.setColumnProperties(new String[] { FSCellModifier.PROPERTY_NAME });
		TextCellEditor cellEditor = new TextCellEditor((Composite) viewer.getControl(), SWT.BORDER);
		cellEditor.setValidator(new FSCellValidator(viewer));
		cellEditor.addListener(new FSCellListener(cellEditor));
		viewer.setCellEditors(new CellEditor[] { cellEditor });
		viewer.setCellModifier(new FSCellModifier());
		
		// Trace the currently focused tree viewer.
		Control control = viewer.getControl();
		control.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				RenameFilesHandler.setCurrentViewer(viewer);
			}

			@Override
			public void focusLost(FocusEvent e) {
			}
		});
	}
}
