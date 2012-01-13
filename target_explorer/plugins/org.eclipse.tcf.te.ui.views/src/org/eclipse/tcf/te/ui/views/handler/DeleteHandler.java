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
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.tcf.te.runtime.callback.Callback;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.runtime.properties.PropertiesContainer;
import org.eclipse.tcf.te.ui.views.interfaces.handler.IDeleteHandlerDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.navigator.CommonViewer;

/**
 * Delete handler implementation.
 */
public class DeleteHandler extends AbstractHandler {

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
			// Create the delete state properties container
			final IPropertiesContainer state = new PropertiesContainer();
			// Store the selection to the state as reference
			state.setProperty("selection", selection); //$NON-NLS-1$

			// Loop over the selection and delete the elements providing an IDeleteHandlerDelegate
			Iterator<?> iterator = ((IStructuredSelection)selection).iterator();
			while (iterator.hasNext()) {
				final Object element = iterator.next();

				// Determine the delete handler delegate for the selected element
				IDeleteHandlerDelegate delegate = element instanceof IDeleteHandlerDelegate ? (IDeleteHandlerDelegate)element : null;
				if (delegate == null) delegate = (IDeleteHandlerDelegate)Platform.getAdapterManager().loadAdapter(element, IDeleteHandlerDelegate.class.getName());

				// Delete the element if there is a valid delegate
				if (delegate != null && delegate.canDelete(element)) {
					// Determine the elements parent element
					Object parentElement = null;
					CommonViewer viewer = (CommonViewer)part.getAdapter(CommonViewer.class);
					if (viewer != null && viewer.getContentProvider() instanceof ITreeContentProvider) {
						ITreeContentProvider cp = (ITreeContentProvider)viewer.getContentProvider();
						parentElement = cp.getParent(element);
					}
					final Object finParentElement = parentElement;

					// Delete the element and refresh the parent element
					delegate.delete(element, state, new Callback() {
						@Override
						protected void internalDone(Object caller, IStatus status) {
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
					});
				}
			}
		}

		return null;
	}
}
