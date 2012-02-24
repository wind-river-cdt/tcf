/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.internal.dnd;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tcf.te.runtime.callback.Callback;
import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSCopy;
import org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSMove;
import org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSOperation;
import org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSRefresh;
import org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSUpload;
import org.eclipse.tcf.te.tcf.filesystem.model.FSModel;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.filesystem.nls.Messages;
import org.eclipse.ui.PlatformUI;
/**
 * Common DnD operations shared by File Explorer and Target Explorer.  
 */
public class CommonDnD {

	/**
	 * If the current selection is draggable.
	 * 
	 * @param selection The currently selected nodes.
	 * @return true if it is draggable.
	 */
	public boolean isDraggable(IStructuredSelection selection) {
		if (selection.isEmpty()) {
			return false;
		}
		Object[] objects = selection.toArray();
		for (Object object : objects) {
			if (!isDraggableObject(object)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * If the specified object is a draggable element.
	 * 
	 * @param object The object to be dragged.
	 * @return true if it is draggable.
	 */
	private boolean isDraggableObject(Object object) {
		if (object instanceof FSTreeNode) {
			FSTreeNode node = (FSTreeNode) object;
			return !node.isRoot() && (node.isWindowsNode() && !node.isReadOnly() || !node.isWindowsNode() && node.isWritable());
		}
		return false;
	}

	/**
	 * Perform the drop operation over dragged files to the specified target folder.
	 * 
	 * @param viewer the tree viewer to be refreshed after dragging.
	 * @param files The files being dropped.
	 * @param operations the current dnd operations.
	 * @param target the target folder the files to be dropped to.
	 * @return true if the dropping is successful.
	 */
	public boolean dropFiles(TreeViewer viewer, String[] files, int operations, FSTreeNode target) {
		FSOperation operation = null;
		if ((operations & DND.DROP_MOVE) != 0) {
			String question;
			if (files.length == 1) {
				question = NLS.bind(Messages.FSDropTargetListener_MovingWarningSingle, files[0]);
			}
			else {
				question = NLS.bind(Messages.FSDropTargetListener_MovingWarningMultiple, Integer.valueOf(files.length));
			}
			Shell parent = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			if (MessageDialog.openQuestion(parent, Messages.FSDropTargetListener_ConfirmMoveTitle, question)) {
				operation = new FSUpload(files, target, getMoveCallback(viewer, files, target));
			}
		}
		else if ((operations & DND.DROP_COPY) != 0) {
			operation = new FSUpload(files, target, getCopyCallback(viewer, files, target));
		}
		if (operation != null) {
			IStatus status = operation.doit();
			return status != null && status.isOK();
		}
		return false;
	}

	/**
	 * Get the callback that refresh and select the files being dragged when the dragging gesture is
	 * copying.
	 * 
	 * @param viewer the tree viewer to be refreshed after dragging.
	 * @param files The files being dragged.
	 * @param target The target folder to drag the files to.
	 * @return callback that handles refreshing and selection.
	 */
	private ICallback getCopyCallback(final TreeViewer viewer, final String[] files, final FSTreeNode target) {
		return new Callback() {
			@Override
			protected void internalDone(Object caller, IStatus status) {
				FSRefresh refresh = new FSRefresh(target, getSelectionCallback(viewer, files, target));
				refresh.doit();
			}
		};
	}

	/**
	 * Get the callback that delete the dragged source files, refresh and select the files being
	 * dragged when the dragging gesture is moving.
	 * 
	 * @param viewer the tree viewer to be refreshed after dragging.
	 * @param files The files being dragged.
	 * @param target The target folder to drag the files to.
	 * @return callback that handles deletion, refreshing and selection.
	 */
	private ICallback getMoveCallback(final TreeViewer viewer, final String[] files, final FSTreeNode target) {
		return new Callback() {
			@Override
			protected void internalDone(Object caller, IStatus status) {
				boolean successful = true;
				if (status != null && status.isOK()) {
					for (String path : files) {
						File file = new File(path);
						successful &= file.delete();
					}
				}
				if (successful) {
					FSTreeNode root = FSModel.getFSModel(target.peerNode).getRoot();
					FSRefresh refresh = new FSRefresh(root, getSelectionCallback(viewer, files, target));
					refresh.doit();
				}
			}
		};
	}

	/**
	 * Get the callback that refresh the files being dragged after moving or copying.
	 * 
	 * @param viewer the tree viewer to be refreshed after dragging.
	 * @param paths The paths of the files being dragged.
	 * @param target The target folder to drag the files to.
	 * @return callback that handles refreshing and selection.
	 */
	ICallback getSelectionCallback(final TreeViewer viewer, final String[] paths, final FSTreeNode target) {
		return new Callback() {
			@Override
			protected void internalDone(Object caller, IStatus status) {
				List<FSTreeNode> nodes = new ArrayList<FSTreeNode>();
				List<FSTreeNode> children = target.unsafeGetChildren();
				for (String path : paths) {
					File file = new File(path);
					String name = file.getName();
					for (FSTreeNode child : children) {
						if (name.equals(child.name)) {
							nodes.add(child);
							break;
						}
					}
				}
				Assert.isNotNull(Display.getCurrent());
				if (viewer != null) {
					viewer.refresh(target);
					IStructuredSelection selection = new StructuredSelection(nodes.toArray());
					viewer.setSelection(selection, true);
				}
			}
		};
	}

	/**
	 * Perform the drop operation over dragged selection.
	 * 
	 * @param aTarget the target Object to be moved to.
	 * @param operations the current dnd operations.
	 * @param selection The local selection being dropped.
	 * @return true if the dropping is successful.
	 */
	public boolean dropLocalSelection(FSTreeNode target, int operations, IStructuredSelection selection) {
		List<FSTreeNode> nodes = selection.toList();
		FSOperation operation = null;
		if ((operations & DND.DROP_MOVE) != 0) {
			operation = new FSMove(nodes, target);
		}
		else if ((operations & DND.DROP_COPY) != 0) {
			FSTreeNode dest = getCopyDestination(target, nodes);
			operation = new FSCopy(nodes, dest);
		}
		if (operation != null) {
			IStatus status = operation.doit();
			return status != null && status.isOK();
		}
		return false;
	}

	/**
	 * Return an appropriate destination directory for copying according to the specified hovered
	 * node. If the hovered node is a file, then return its parent directory. If the hovered node is
	 * a directory, then return its self if it is not a node being copied. Return its parent
	 * directory if it is a node being copied.
	 * 
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

	/**
	 * Validate dropping when the elements being dragged are files.
	 * 
	 * @param target The target object.
	 * @param operation The DnD operation.
	 * @param transferType The transfered data type.
	 * @return true if it is valid for dropping.
	 */
	public boolean validateFilesDrop(Object target, int operation, TransferData transferType) {
		FileTransfer transfer = FileTransfer.getInstance();
		String[] elements = (String[]) transfer.nativeToJava(transferType);
		if (elements.length > 0) {
			boolean moving = (operation & DND.DROP_MOVE) != 0;
			boolean copying = (operation & DND.DROP_COPY) != 0;
			FSTreeNode hovered = (FSTreeNode) target;
			if (hovered.isFile() && copying) {
				hovered = hovered.parent;
			}
			return hovered.isDirectory() && hovered.isWritable() && (moving || copying);
		}
		return false;
	}

	/**
	 * Validate dropping when the elements being dragged are local selection.
	 * 
	 * @param target The target object.
	 * @param operation The DnD operation.
	 * @param transferType The transfered data type.
	 * @return true if it is valid for dropping.
	 */
	public boolean validateLocalSelectionDrop(Object target, int operation, TransferData transferType) {
		FSTreeNode hovered = (FSTreeNode) target;
		LocalSelectionTransfer transfer = LocalSelectionTransfer.getTransfer();
		IStructuredSelection selection = (IStructuredSelection) transfer.getSelection();
		List<FSTreeNode> nodes = selection.toList();
		boolean moving = (operation & DND.DROP_MOVE) != 0;
		boolean copying = (operation & DND.DROP_COPY) != 0;
		if (hovered.isDirectory() && hovered.isWritable() && (moving || copying)) {
			FSTreeNode head = nodes.get(0);
			String hid = head.peerNode.getPeerId();
			String tid = hovered.peerNode.getPeerId();
			if (hid.equals(tid)) {
				for (FSTreeNode node : nodes) {
					if (moving && node == hovered || node.isAncestorOf(hovered)) {
						return false;
					}
				}
				return true;
			}
		}
		else if (hovered.isFile() && copying) {
			hovered = hovered.parent;
			return validateLocalSelectionDrop(hovered, operation, transferType);
		}
		return false;
	}
}
