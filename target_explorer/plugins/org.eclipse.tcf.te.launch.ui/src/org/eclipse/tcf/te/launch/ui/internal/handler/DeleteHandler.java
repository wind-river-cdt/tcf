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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.te.launch.ui.activator.UIPlugin;
import org.eclipse.tcf.te.launch.ui.model.LaunchNode;
import org.eclipse.tcf.te.launch.ui.nls.Messages;
import org.eclipse.tcf.te.runtime.callback.Callback;
import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.runtime.properties.PropertiesContainer;
import org.eclipse.tcf.te.runtime.utils.StatusHelper;
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

				// Delete the element if there is a valid delegate
				if (canDelete(element)) {
					// Determine the elements parent element
					Object parentElement = null;
					CommonViewer viewer = (CommonViewer)part.getAdapter(CommonViewer.class);
					if (viewer != null && viewer.getContentProvider() instanceof ITreeContentProvider) {
						ITreeContentProvider cp = (ITreeContentProvider)viewer.getContentProvider();
						parentElement = cp.getParent(element);
					}
					final Object finParentElement = parentElement;

					// Delete the element and refresh the parent element
					delete(element, state, new Callback() {
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

	// ***** DeleteHandlerDelegate content. Clean up. *****

	@Deprecated
	public boolean canDelete(Object element) {
		if (element instanceof LaunchNode) {
			LaunchNode node = (LaunchNode)element;
			return LaunchNode.TYPE_LAUNCH_CONFIG.equals(node.getType()) && !node.getLaunchConfiguration().isReadOnly();
		}
		return false;
	}

	@Deprecated
	public void delete(Object element, IPropertiesContainer state, final ICallback callback) {
		Assert.isNotNull(element);
		Assert.isNotNull(state);

		if (element instanceof LaunchNode) {
			final LaunchNode node = (LaunchNode)element;
			try {
				if (MessageDialog.openQuestion(
								UIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell(),
								Messages.DeleteHandlerDelegate_question_title, NLS.bind(Messages.DeleteHandlerDelegate_question_message, node.getLaunchConfiguration().getName()))) {
					node.getLaunchConfiguration().delete();
					if (callback != null) {
						callback.done(this, Status.OK_STATUS);
					}
				}
				else {
					if (callback != null) {
						callback.done(this, Status.CANCEL_STATUS);
					}
				}
			}
			catch (Exception e) {
				if (callback != null) {
					callback.done(this, StatusHelper.getStatus(e));
				}
			}
		}
	}
}
