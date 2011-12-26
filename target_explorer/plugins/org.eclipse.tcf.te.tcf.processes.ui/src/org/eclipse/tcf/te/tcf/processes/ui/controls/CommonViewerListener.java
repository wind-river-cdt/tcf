/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.processes.ui.controls;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.navigator.CommonViewer;

/**
 * CommonViewerListener listens to the property change event from Target Explorer's
 *  tree and update the viewer accordingly.
 */
public class CommonViewerListener implements IPropertyChangeListener {
	// The common viewer of Target Explorer view.
	private CommonViewer viewer;
	
	/***
	 * Create an instance for the specified common viewer.
	 * 
	 * @param viewer The common viewer from Target Explorer view.
	 */
	public CommonViewerListener(CommonViewer viewer) {
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
				viewer.refresh();
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
