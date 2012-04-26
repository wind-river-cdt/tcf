/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.ui.handler;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.ui.views.Managers;
import org.eclipse.tcf.te.ui.views.ViewsUtil;
import org.eclipse.tcf.te.ui.views.interfaces.IUIConstants;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Command handler implementation: Remove the selected nodes from the "Favorites" categories.
 */
public class RemoveFromFavoritesCommandHandler extends AbstractHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Get the active selection
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
			boolean refresh = false;
			Iterator<?> iterator = ((IStructuredSelection)selection).iterator();
			while (iterator.hasNext()) {
				Object element = iterator.next();
				if (element instanceof IPeerModel) {
					String peerId = ((IPeerModel)element).getPeerId();
					if (Managers.getCategoryManager().belongsTo(IUIConstants.ID_CAT_FAVORITES, peerId)) {
						Managers.getCategoryManager().remove(IUIConstants.ID_CAT_FAVORITES, peerId);
						refresh = true;
					}
				}
			}

			// Refresh the view
			if (refresh) ViewsUtil.refresh(IUIConstants.ID_EXPLORER);
		}

		return null;
	}

}
