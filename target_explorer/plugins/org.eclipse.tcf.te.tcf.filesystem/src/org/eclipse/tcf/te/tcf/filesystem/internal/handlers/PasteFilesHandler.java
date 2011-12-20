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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.tcf.te.tcf.filesystem.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSClipboard;
import org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSCopy;
import org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSMove;
import org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSOperation;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * The handler that pastes the files or folders in the clip board.
 */
public class PasteFilesHandler extends AbstractHandler {

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		FSClipboard cb = UIPlugin.getDefault().getClipboard();
		if (!cb.isEmpty()) {
			// Get the files/folders from the clip board.
			int operations = cb.getOperation();
			IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelectionChecked(event);
			List<FSTreeNode> nodes = cb.getFiles();
			FSOperation operation = null;
			if (operations == FSClipboard.CUT) {
				FSTreeNode dest = (FSTreeNode) selection.getFirstElement();
				operation = new FSMove(nodes, dest);
			}
			else if (operations == FSClipboard.COPY) {
				FSTreeNode hovered = (FSTreeNode) selection.getFirstElement();
				FSTreeNode dest = getCopyDestination(hovered, nodes);
				operation = new FSCopy(nodes, dest);
			}
			if (operation != null) operation.doit();
		}
		return null;
	}

	/**
	 * Return an appropriate destination directory for copying according to
	 * the specified hovered node.  If the hovered node is a file, then return 
	 * its parent directory. If the hovered node is a directory, then return its
	 * self if it is not a node being copied. Return its parent directory if it is
	 * a node being copied.
	 * @param hovered
	 * @param nodes
	 * @return
	 */
	private FSTreeNode getCopyDestination(FSTreeNode hovered, List<FSTreeNode> nodes) {
		if (hovered.isFile()) {
			return hovered.parent;
		}
		else if (hovered.isDirectory()) {
			for (FSTreeNode node : nodes) {
				if (node == hovered) {
					return hovered.parent;
				}
			}
		}
		return hovered;
	}
}
