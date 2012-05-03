/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.ui.internal.handler;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.tcf.te.launch.ui.model.LaunchModel;
import org.eclipse.tcf.te.launch.ui.model.LaunchNode;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.navigator.CommonViewer;

/**
 * Refresh handler implementation.
 */
public class RefreshHandler extends AbstractHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Get the current selection
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
			// Determine the active part
			final IWorkbenchPart part = HandlerUtil.getActivePart(event);

			// Loop over the selection and refresh the elements
			Iterator<?> iterator = ((IStructuredSelection)selection).iterator();
			while (iterator.hasNext()) {
				final Object element = iterator.next();

				// Refresh the element if there is a valid delegate
				if (canRefresh(element)) {
					// Determine the elements parent element
					Object parentElement = null;
					CommonViewer viewer = (CommonViewer)part.getAdapter(CommonViewer.class);
					if (viewer != null && viewer.getContentProvider() instanceof ITreeContentProvider) {
						ITreeContentProvider cp = (ITreeContentProvider)viewer.getContentProvider();
						parentElement = cp.getParent(element);
					}
					final Object finParentElement = parentElement;

					// Refresh the element and the tree
					if (refresh(element)) {
						PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
							@Override
							public void run() {
								CommonViewer viewer = (CommonViewer)part.getAdapter(CommonViewer.class);
								if (viewer != null) {
									if (finParentElement != null) {
										viewer.refresh(finParentElement, true);
									} else {
										viewer.refresh(true);
									}
								}
							}
						});
					}
				}
			}
		}

		return null;
	}

	/**
	 * Check if an element can be refreshed.
	 * @param element The element to check.
	 * @return
	 */
	public boolean canRefresh(Object element) {
		if (element instanceof LaunchNode) {
			return true;
		}
		return false;
	}

	private boolean refresh(Object element) {
		Assert.isNotNull(element);

		LaunchNode node = (LaunchNode) element;
		LaunchModel model = node.getModel();
		return model.refresh();
	}
}
