/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.ui.internal.handlers;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tcf.te.runtime.callback.Callback;
import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.runtime.properties.PropertiesContainer;
import org.eclipse.tcf.te.tcf.filesystem.core.interfaces.IConfirmCallback;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.IOpExecutor;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.OpDelete;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.filesystem.ui.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.filesystem.ui.internal.ImageConsts;
import org.eclipse.tcf.te.tcf.filesystem.ui.internal.operations.UiExecutor;
import org.eclipse.tcf.te.tcf.filesystem.ui.nls.Messages;
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

	// The key to access the selection in the state.
	private static final String KEY_SELECTION = "selection"; //$NON-NLS-1$
	// The key to access the processed state in the state.
	private static final String KEY_PROCESSED = "processed"; //$NON-NLS-1$
	// The deletion confirmation callback
	private IConfirmCallback confirmCallback = new DeletionConfirmCallback();
	// The confirmation call for read only files.
	private IConfirmCallback readonlyCallback = new ReadOnlyConfirmCallback();

	/**
	 * Set the confirmation callback
	 *
	 * @param confirmCallback The confirmation callback
	 */
	public void setConfirmCallback(IConfirmCallback confirmCallback) {
		this.confirmCallback = confirmCallback;
	}

	@Deprecated
	public boolean canDelete(Object element) {
		if (element instanceof FSTreeNode) {
			FSTreeNode node = (FSTreeNode) element;
			if (!node.isSystemRoot() && !node.isRoot()) {
				return node.isWindowsNode() && !node.isReadOnly()
								|| !node.isWindowsNode() && node.isWritable();
			}
		}
		return false;
	}

	@Deprecated
	public void delete(Object element, IPropertiesContainer state, ICallback callback) {
		Assert.isNotNull(element);
		Assert.isNotNull(state);

		UUID lastProcessed = (UUID) state.getProperty(KEY_PROCESSED);
		if (lastProcessed == null || !lastProcessed.equals(state.getUUID())) {
			state.setProperty(KEY_PROCESSED, state.getUUID());
			if(confirmCallback != null) {
				IStructuredSelection selection = (IStructuredSelection) state.getProperty(KEY_SELECTION);
				if(!confirmCallback.requires(selection) || confirmCallback.confirms(selection) == IConfirmCallback.YES) {
					List<FSTreeNode> nodes = selection.toList();
					IOpExecutor executor = new UiExecutor(callback);
					executor.execute(new OpDelete(nodes, readonlyCallback));
				}
			}
		}
	}

	static class ReadOnlyConfirmCallback implements IConfirmCallback {
		/*
		 * (non-Javadoc)
		 * @see org.eclipse.tcf.te.tcf.filesystem.core.interfaces.IConfirmCallback#requires(java.lang.Object)
		 */
		@Override
        public boolean requires(Object object) {
			if(object instanceof FSTreeNode) {
				FSTreeNode node = (FSTreeNode) object;
				return node.isWindowsNode() && node.isReadOnly() || !node.isWindowsNode() && !node.isWritable();
			}
			return false;
        }

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.tcf.te.tcf.filesystem.core.interfaces.IConfirmCallback#confirms(java.lang.Object)
		 */
		@Override
        public int confirms(Object object) {
			final FSTreeNode node = (FSTreeNode) object;
			final int[] results = new int[1];
			Display display = PlatformUI.getWorkbench().getDisplay();
			display.syncExec(new Runnable() {
				@Override
				public void run() {
					Shell parent = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					String title = Messages.FSDelete_ConfirmDelete;
					String message = NLS.bind(Messages.FSDelete_ConfirmMessage, node.name);
					final Image titleImage = UIPlugin.getImage(ImageConsts.DELETE_READONLY_CONFIRM);
					MessageDialog qDialog = new MessageDialog(parent, title, null, message, MessageDialog.QUESTION, new String[] { Messages.FSDelete_ButtonYes, Messages.FSDelete_ButtonYes2All, Messages.FSDelete_ButtonNo, Messages.FSDelete_ButtonCancel }, 0) {
						@Override
						public Image getQuestionImage() {
							return titleImage;
						}
					};
					results[0] = qDialog.open();
				}
			});
			return results[0];
        }

	}

	static class DeletionConfirmCallback implements IConfirmCallback {
		/*
		 * (non-Javadoc)
		 * @see org.eclipse.tcf.te.tcf.filesystem.core.interfaces.IConfirmCallback#requires(java.lang.Object)
		 */
		@Override
	    public boolean requires(Object object) {
		    return true;
	    }

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.tcf.te.tcf.filesystem.interfaces.IConfirmCallback#confirms(java.lang.Object)
		 */
		@Override
	    public int confirms(Object object) {
			IStructuredSelection selection = (IStructuredSelection) object;
			List<FSTreeNode> nodes = selection.toList();
			String question;
			if (nodes.size() == 1) {
				FSTreeNode node = nodes.get(0);
				question = NLS.bind(Messages.DeleteFilesHandler_DeleteOneFileConfirmation, node.name);
			}
			else {
				question = NLS.bind(Messages.DeleteFilesHandler_DeleteMultipleFilesConfirmation, Integer.valueOf(nodes.size()));
			}
			Shell parent = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			if (MessageDialog.openQuestion(parent, Messages.DeleteFilesHandler_ConfirmDialogTitle, question)) {
				return IConfirmCallback.YES;
			}
			return IConfirmCallback.NO;
	    }
	}
}
