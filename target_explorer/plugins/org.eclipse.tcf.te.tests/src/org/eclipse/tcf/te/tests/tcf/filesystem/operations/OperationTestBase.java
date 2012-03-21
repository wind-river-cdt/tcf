/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tests.tcf.filesystem.operations;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSCopy;
import org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSCreateFile;
import org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSCreateFolder;
import org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSDelete;
import org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSMove;
import org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSRename;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.tcf.te.tests.tcf.filesystem.FSPeerTestCase;

@SuppressWarnings("restriction")
public class OperationTestBase extends FSPeerTestCase {

	protected FSTreeNode copy(FSTreeNode file, FSTreeNode folder) throws Exception {
		printDebugMessage("Copy " + file.getLocation() + " to " + folder.getLocation() + "..."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		List<FSTreeNode> files = new ArrayList<FSTreeNode>();
		files.add(file);
		FSCopy copy = new FSCopy(files, folder);
		copy.run(new NullProgressMonitor());
		String location = folder.getLocation();
		String path = location + getPathSep() + file.name;
		return getFSNode(path);
	}

	protected FSTreeNode createFile(String fileName, FSTreeNode folder) throws Exception {
		printDebugMessage("Create " + fileName + " at " + folder.getLocation()); //$NON-NLS-1$ //$NON-NLS-2$
		FSCreateFile create = new FSCreateFile(folder, fileName);
		create.run(new NullProgressMonitor());
		String location = folder.getLocation();
		String path = location + getPathSep() + fileName;
		return getFSNode(path);
	}

	protected FSTreeNode createFolder(String folderName, FSTreeNode folder) throws Exception {
		printDebugMessage("Create " + folderName + " at " + folder.getLocation()); //$NON-NLS-1$ //$NON-NLS-2$
		FSCreateFolder create = new FSCreateFolder(folder, folderName);
		create.run(new NullProgressMonitor());
		String location = folder.getLocation();
		String path = location + getPathSep() + folderName;
		return getFSNode(path);
	}

	protected FSTreeNode move(FSTreeNode src, FSTreeNode dest) throws Exception {
		printDebugMessage("Move " + src.getLocation() + " to " + dest.getLocation()); //$NON-NLS-1$ //$NON-NLS-2$
		List<FSTreeNode> nodes = new ArrayList<FSTreeNode>();
		nodes.add(src);
		FSMove fsmove = new FSMove(nodes, dest);
		fsmove.run(new NullProgressMonitor());
		String path = dest.getLocation() + getPathSep() + src.name;
		return getFSNode(path);
	}

	protected FSTreeNode rename(FSTreeNode node, String newName) throws Exception {
		printDebugMessage("Rename " + node.name + " to " + newName); //$NON-NLS-1$ //$NON-NLS-2$
		FSRename fsmove = new FSRename(node, newName);
		fsmove.run(new NullProgressMonitor());
		String newPath = node.parent.getLocation()+getPathSep()+newName;
		return getFSNode(newPath);
	}

	protected void delete(FSTreeNode node) throws Exception {
		printDebugMessage("Delete " + node.getLocation() + "..."); //$NON-NLS-1$ //$NON-NLS-2$
		List<FSTreeNode> files = new ArrayList<FSTreeNode>();
		files.add(node);
		FSDelete delete = new FSDelete(files);
		delete.run(new NullProgressMonitor());
	}
}
