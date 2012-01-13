/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.views.handler;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.tcf.te.runtime.callback.Callback;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.runtime.properties.PropertiesContainer;
import org.eclipse.tcf.te.ui.views.interfaces.handler.IRefreshHandlerDelegate;
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
			// Create the refresh state properties container
			final IPropertiesContainer state = new PropertiesContainer();
			// Store the selection to the state as reference
			state.setProperty("selection", selection); //$NON-NLS-1$

			// Loop over the selection and refresh the elements providing an IRefreshHandlerDelegate
			Iterator<?> iterator = ((IStructuredSelection)selection).iterator();
			while (iterator.hasNext()) {
				final Object element = iterator.next();

				// Determine the refresh handler delegate for the selected element
				IRefreshHandlerDelegate delegate = element instanceof IRefreshHandlerDelegate ? (IRefreshHandlerDelegate)element : null;
				if (delegate == null) delegate = (IRefreshHandlerDelegate)Platform.getAdapterManager().loadAdapter(element, IRefreshHandlerDelegate.class.getName());

				// Refresh the element if there is a valid delegate
				if (delegate != null && delegate.canRefresh(element)) {
					delegate.refresh(element, state, new Callback() {
						@Override
						protected void internalDone(Object caller, IStatus status) {
							PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
								@Override
								public void run() {
									CommonViewer viewer = (CommonViewer)part.getAdapter(CommonViewer.class);
									if (viewer != null) viewer.refresh(element, true);
								}
							});
						}
					});
				}
			}
		}

		return null;
	}

}
