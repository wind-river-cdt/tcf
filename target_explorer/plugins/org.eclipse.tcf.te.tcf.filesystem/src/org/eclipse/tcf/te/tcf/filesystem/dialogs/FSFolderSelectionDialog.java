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
package org.eclipse.tcf.te.tcf.filesystem.dialogs;

import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tcf.te.tcf.filesystem.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.filesystem.controls.FSTreeContentProvider;
import org.eclipse.tcf.te.tcf.filesystem.controls.FSTreeLabelProvider;
import org.eclipse.tcf.te.tcf.filesystem.internal.handlers.MoveFilesHandler;
import org.eclipse.tcf.te.tcf.filesystem.internal.nls.Messages;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;

/**
 * <p>
 * The folder selection dialog for a remote file system. To populate the tree of the selection
 * dialog with the file system, you should call <code>
 * ElementTreeSelectionDialog.setInput</code> to specify the peer model of the remote target. In
 * order to validate the destination folder, you should also specify the nodes to be moved. The file
 * selection dialog is of single selection. You can get the selected result by calling
 * <code>getFirstResult</code>. The type of selected folder is an instance of FSTreeNode.
 * </p>
 * <p>
 * The following is a snippet of example code:
 * 
 * <pre>
 * FSFolderSelectionDialog dialog = new FSFolderSelectionDialog(shell);
 * dialog.setInput(peer);
 * dialog.setMovedNodes(nodes);
 * if (dialog.open() == Window.OK) {
 * 	Object obj = dialog.getFirstResult();
 * 	Assert.isTrue(obj instanceof FSTreeNode);
 * 	FSTreeNode folder = (FSTreeNode) obj;
 * 	// Use folder ...
 * }
 * </pre>
 * 
 * @see MoveFilesHandler
 */
public class FSFolderSelectionDialog extends ElementTreeSelectionDialog {
	// Label provider used by the file system tree.
	private FSTreeLabelProvider labelProvider;
	// The nodes that are being moved.
	private List<FSTreeNode> movedNodes;

	/**
	 * Create an FSFolderSelectionDialog using the specified shell as the parent.
	 * 
	 * @param parentShell The parent shell.
	 */
	public FSFolderSelectionDialog(Shell parentShell) {
		this(parentShell, new FSTreeLabelProvider(), new FSTreeContentProvider());
	}

	/**
	 * Create an FSFolderSelectionDialog using the specified shell, an FSTreeLabelProvider, and a
	 * content provider that provides the tree nodes.
	 * 
	 * @param parentShell The parent shell.
	 * @param labelProvider The label provider.
	 * @param contentProvider The content provider.
	 */
	private FSFolderSelectionDialog(Shell parentShell, FSTreeLabelProvider labelProvider, ITreeContentProvider contentProvider) {
		super(parentShell, labelProvider, contentProvider);
		this.labelProvider = labelProvider;
		setTitle(Messages.FSFolderSelectionDialog_MoveDialogTitle);
		setMessage(Messages.FSFolderSelectionDialog_MoveDialogMessage);
		this.setAllowMultiple(false);
		this.addFilter(new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (element instanceof FSTreeNode) {
					FSTreeNode node = (FSTreeNode) element;
					return node.isDirectory() || node.type != null && node.type
					                .equals("FSPendingNode"); //$NON-NLS-1$
				}
				return false;
			}
		});
		this.setStatusLineAboveButtons(false);
		this.setValidator(new ISelectionStatusValidator() {

			@Override
			public IStatus validate(Object[] selection) {
				return isValidFolder(selection);
			}
		});
	}

	/**
	 * Set the nodes that are about to be moved.
	 * 
	 * @param movedNodes The nodes.
	 */
	public void setMovedNodes(List<FSTreeNode> movedNodes) {
		this.movedNodes = movedNodes;
	}

	/**
	 * Create the tree viewer and set it to the label provider.
	 */
	@Override
	protected TreeViewer doCreateTreeViewer(Composite parent, int style) {
		TreeViewer viewer = super.doCreateTreeViewer(parent, style);
		viewer.getTree().setLinesVisible(false);
		labelProvider.setParentViewer(viewer);
		return viewer;
	}

	/**
	 * If the specified selection is a valid folder to be selected.
	 * 
	 * @param selection The selected folders.
	 * @return An error status if it is invalid or an OK status indicating it is valid.
	 */
	IStatus isValidFolder(Object[] selection) {
		String pluginId = UIPlugin.getUniqueIdentifier();
		IStatus error = new Status(IStatus.ERROR, pluginId, null);
		if (selection == null || selection.length == 0) {
			return error;
		}
		if (!(selection[0] instanceof FSTreeNode)) {
			return error;
		}
		FSTreeNode target = (FSTreeNode) selection[0];
		for (FSTreeNode node : movedNodes) {
			if (node == target || node.isAncestorOf(target)) {
				return error;
			}
		}
		return new Status(IStatus.OK, pluginId, null);
	}
}
