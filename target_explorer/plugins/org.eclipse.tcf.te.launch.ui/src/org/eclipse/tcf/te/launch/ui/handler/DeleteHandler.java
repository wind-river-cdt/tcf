/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.ui.handler;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.te.launch.ui.activator.UIPlugin;
import org.eclipse.tcf.te.launch.ui.model.LaunchModel;
import org.eclipse.tcf.te.launch.ui.model.LaunchNode;
import org.eclipse.tcf.te.launch.ui.nls.Messages;
import org.eclipse.tcf.te.ui.views.Managers;
import org.eclipse.tcf.te.ui.views.interfaces.ICategory;
import org.eclipse.ui.handlers.HandlerUtil;

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
			// Loop over the selection and delete the elements providing
			Iterator<?> iterator = ((IStructuredSelection)selection).iterator();
			while (iterator.hasNext()) {
				final Object element = iterator.next();

				// Delete the element if there is a valid delegate
				if (canDelete(element)) {
					// Delete the element and refresh the tree
					delete(element);
				}
			}
		}

		return null;
	}

	/**
	 * Check if an element can be deleted.
	 * @param element The element to check.
	 * @return
	 */
	public boolean canDelete(Object element) {
		if (element instanceof LaunchNode) {
			LaunchNode node = (LaunchNode)element;
			return node.isType(LaunchNode.TYPE_LAUNCH_CONFIG) &&
							(node.getModel().getModelRoot() instanceof ICategory || !node.getLaunchConfiguration().isReadOnly());
		}
		return false;
	}

	private void delete(Object element) {
		Assert.isNotNull(element);

		if (element instanceof LaunchNode) {
			LaunchNode node = (LaunchNode)element;
			if (node.getModel().getModelRoot() instanceof ICategory) {
				Managers.getCategoryManager().remove(((ICategory)node.getModel().getModelRoot()).getId(), LaunchModel.getCategoryId(node.getLaunchConfiguration()));
			}
			else {
				if (MessageDialog.openQuestion(
								UIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell(),
								Messages.DeleteHandlerDelegate_question_title, NLS.bind(Messages.DeleteHandlerDelegate_question_message, node.getLaunchConfiguration().getName()))) {
					try {
						node.getLaunchConfiguration().delete();
					}
					catch (Exception e) {
					}
				}
			}
		}
	}
}
