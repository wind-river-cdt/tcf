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

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tcf.te.tcf.filesystem.core.interfaces.IConfirmCallback;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.IOpExecutor;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.OpDelete;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.filesystem.ui.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.filesystem.ui.internal.ImageConsts;
import org.eclipse.tcf.te.tcf.filesystem.ui.internal.operations.UiExecutor;
import org.eclipse.tcf.te.tcf.filesystem.ui.nls.Messages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Delete handler implementation.
 */
public class DeleteHandler extends AbstractHandler {
	// The confirmation call for read only files.
	private IConfirmCallback readonlyCallback = new ReadOnlyConfirmCallback();

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Get the current selection
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
			List<FSTreeNode> nodes = ((IStructuredSelection)selection).toList();
			if(confirmDeletion(nodes)) {
				IOpExecutor executor = new UiExecutor();
				executor.execute(new OpDelete(nodes, readonlyCallback));
			}
		}

		return null;
	}
	
	/**
	 * Confirm the deletion of the specified nodes.
	 * 
	 * @param nodes The nodes to be deleted.
	 * @return true if the user agrees to delete.
	 */
    private boolean confirmDeletion(List<FSTreeNode> nodes) {
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
			return true;
		}
		return false;
    }

    /**
     * The callback implementation for the user to confirm the deletion of
     * a read-only file/folder.
     */
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
}
