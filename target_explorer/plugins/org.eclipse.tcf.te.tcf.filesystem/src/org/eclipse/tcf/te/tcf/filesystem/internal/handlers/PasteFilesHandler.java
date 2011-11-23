/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * William Chen (Wind River) - [361324] Add more file operations in the file 
 * 												system of Target Explorer.
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.internal.handlers;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.tcf.te.tcf.filesystem.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSClipboard;
import org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSCopy;
import org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSMove;
import org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSOperation;
import org.eclipse.tcf.te.tcf.filesystem.model.FSModel;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.ui.handlers.HandlerUtil;
/**
 * The handler that pastes the files or folders in the clip board.
 */
public class PasteFilesHandler extends AbstractHandler {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		FSClipboard cb = UIPlugin.getDefault().getClipboard();
		if (!cb.isEmpty()) {
			// Get the files/folders from the clip board.
			List<FSTreeNode> nodes = new ArrayList<FSTreeNode>();
			List<URL> files = cb.getFiles();
			for (URL file : files) {
				FSTreeNode node = FSModel.getInstance().getTreeNode(file);
				nodes.add(node);
			}
			// Get the destination folder.
			IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelectionChecked(event);
			FSTreeNode dest = (FSTreeNode) selection.getFirstElement();
			int operation = cb.getOperation();
			FSOperation fsop;
			if (operation == FSClipboard.COPY) {
				// Copy action.
				fsop = new FSCopy(nodes, dest);
			}
			else {
				// Cut action.
				fsop = new FSMove(nodes, dest);
			}
			fsop.doit();
		}
		return null;
	}

}
