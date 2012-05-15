/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.tcf.te.runtime.callback.Callback;
import org.eclipse.tcf.te.tcf.filesystem.core.interfaces.IOperation;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.IOpExecutor;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.OpCopy;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.OpMove;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.filesystem.ui.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.filesystem.ui.internal.dnd.CommonDnD;
import org.eclipse.tcf.te.tcf.filesystem.ui.internal.operations.FsClipboard;
import org.eclipse.tcf.te.tcf.filesystem.ui.internal.operations.UiExecutor;
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
		FsClipboard cb = UIPlugin.getClipboard();
		if (!cb.isEmpty()) {
			// Get the files/folders from the clip board.
			IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelectionChecked(event);
			List<FSTreeNode> nodes = cb.getFiles();
			IOpExecutor executor = null;
			IOperation operation = null;
			if (cb.isCutOp()) {
				FSTreeNode dest = (FSTreeNode) selection.getFirstElement();
				operation = new OpMove(nodes, dest, new MoveCopyCallback());
				executor = new UiExecutor(new Callback(){
					@Override
	                protected void internalDone(Object caller, IStatus status) {
						UIPlugin.getClipboard().clear();
	                }
				});
			}
			else if (cb.isCopyOp()) {
				FSTreeNode hovered = (FSTreeNode) selection.getFirstElement();
				FSTreeNode dest = getCopyDestination(hovered, nodes);
				boolean cpPerm = UIPlugin.isCopyPermission();
				boolean cpOwn = UIPlugin.isCopyOwnership();
				operation = new OpCopy(nodes, dest, cpPerm, cpOwn, new MoveCopyCallback());
				executor = new UiExecutor();
			}
			if (executor != null && operation != null) {
				executor.execute(operation);
			}
		}
		else {
			Clipboard clipboard = cb.getSystemClipboard();
			Object contents = clipboard.getContents(FileTransfer.getInstance());
			if (contents != null) {
				String[] files = (String[]) contents;
				// Get the files/folders from the clip board.
				IStructuredSelection selection = (IStructuredSelection) HandlerUtil
				                .getCurrentSelectionChecked(event);
				FSTreeNode hovered = (FSTreeNode) selection.getFirstElement();
				CommonDnD dnd = new CommonDnD();
				dnd.dropFiles(null, files, DND.DROP_COPY, hovered);
			}
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
			return hovered.getParent();
		}
		else if (hovered.isDirectory()) {
			for (FSTreeNode node : nodes) {
				if (node == hovered) {
					return hovered.getParent();
				}
			}
		}
		return hovered;
	}
}
