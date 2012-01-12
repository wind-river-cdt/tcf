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

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;

/**
 * CommonViewerListener listens to the property change event from Target Explorer's
 *  tree and update the viewer accordingly.
 */
public class CommonViewerListener implements IPropertyChangeListener {
	// The common viewer of Target Explorer view.
	private TreeViewer viewer;

	/***
	 * Create an instance for the specified common viewer.
	 *
	 * @param viewer The common viewer from Target Explorer view.
	 */
	public CommonViewerListener(TreeViewer viewer) {
		this.viewer = viewer;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	@Override
    public void propertyChange(final PropertyChangeEvent event) {
		Tree tree = viewer.getTree();
		if (!tree.isDisposed()) {
			Display display = tree.getDisplay();
			if (display.getThread() == Thread.currentThread()) {
				Object object = event.getSource();
				if (object != null) {
					viewer.refresh(object);
				}
				else {
					viewer.refresh();
				}
			}
			else {
				display.asyncExec(new Runnable() {
					@Override
					public void run() {
						propertyChange(event);
					}
				});
			}
		}
    }
}
