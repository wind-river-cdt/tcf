/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.internal.handlers;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSDelete;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.filesystem.nls.Messages;
import org.eclipse.ui.handlers.HandlerUtil;
/**
 * The handler that deletes the selected files or folders from the file system.
 */
public class DeleteFilesHandler extends AbstractHandler {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil
		                .getCurrentSelection(event);
		List<FSTreeNode> nodes = selection.toList();
		String question;
		if (nodes.size() == 1) {
			FSTreeNode node = nodes.get(0);
			question = NLS.bind(Messages.DeleteFilesHandler_DeleteOneFileConfirmation, node.name);
		}
		else {
			question = NLS.bind(Messages.DeleteFilesHandler_DeleteMultipleFilesConfirmation, Integer.valueOf(nodes.size()));
		}
		Shell parent = HandlerUtil.getActiveShellChecked(event);
		if (MessageDialog.openQuestion(parent, Messages.DeleteFilesHandler_ConfirmDialogTitle, question)) {
			FSDelete delete = new FSDelete(nodes);
			delete.doit();
		}
		return null;
	}
}
