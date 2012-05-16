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
import org.eclipse.tcf.te.launch.ui.model.LaunchModel;
import org.eclipse.tcf.te.launch.ui.model.LaunchNode;
import org.eclipse.tcf.te.runtime.events.ChangeEvent;
import org.eclipse.tcf.te.runtime.events.EventManager;
import org.eclipse.ui.handlers.HandlerUtil;

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
			// Loop over the selection and refresh the elements
			Iterator<?> iterator = ((IStructuredSelection)selection).iterator();
			while (iterator.hasNext()) {
				final Object element = iterator.next();

				// Refresh the element if there is a valid delegate
				if (canRefresh(element)) {
					// Refresh the element and the tree
					refresh(element);
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

	private void refresh(Object element) {
		Assert.isNotNull(element);

		if (element instanceof LaunchNode) {
			LaunchNode node = (LaunchNode)element;
			LaunchModel model = node.getModel();
			if (model.refresh()) {
				EventManager.getInstance().fireEvent(new ChangeEvent(this, ChangeEvent.ID_CHANGED, null, null));
			}
		}
	}
}
