/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.views.internal;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;

/**
 * The job listener called after the restoring job is done,
 * to set the expanded states of the tree viewer in
 * a safe UI thread.
 */
public class RestoreDone extends JobChangeAdapter {
	// The tree viewer whose expanding state is going to be restored.
	private TreeViewer viewer;
	
	/**
	 * Create a job listener with the specified tree viewer.
	 * 
	 * @param viewer The tree viewer whose state is going to be restored.
	 */
	public RestoreDone(TreeViewer viewer) {
		this.viewer = viewer;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.JobChangeAdapter#done(org.eclipse.core.runtime.jobs.IJobChangeEvent)
	 */
	@Override
    public void done(IJobChangeEvent event) {
		IStatus result = event.getResult();
		if(result.isOK() && result instanceof RestoreStatus) {
			final RestoreStatus status = (RestoreStatus) result;
			Tree tree = viewer.getTree();
			if(!tree.isDisposed()) {
				Display display = tree.getDisplay();
				if(!display.isDisposed()) {
					display.asyncExec(new Runnable(){
						@Override
                        public void run() {
							expandTreeViewer(status);
                        }});
				}
			}
		}
    }

	/**
	 * Expand the tree viewer using the restoring status where the expanded paths are retrieved.
	 * 
	 * @param status The status that keeps the expanded paths.
	 */
	void expandTreeViewer(RestoreStatus status) {
		IElementComparer comparer = viewer.getComparer();
		if(comparer instanceof ViewViewerComparer) {
			((ViewViewerComparer)comparer).setByDefault(false);
		}
		viewer.setExpandedTreePaths(status.getExpandedPaths());
		if(comparer instanceof ViewViewerComparer) {
			((ViewViewerComparer)comparer).setByDefault(true);
		}
	}
}
