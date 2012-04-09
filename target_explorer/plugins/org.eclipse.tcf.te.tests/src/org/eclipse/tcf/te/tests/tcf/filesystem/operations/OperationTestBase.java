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
import org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.OpCacheCommit;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.OpCacheUpdate;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.OpCopy;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.OpCreateFile;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.OpCreateFolder;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.OpDelete;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.OpMove;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.OpRename;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;
import org.eclipse.tcf.te.tests.tcf.filesystem.FSPeerTestCase;

public class OperationTestBase extends FSPeerTestCase {

	protected FSTreeNode copy(FSTreeNode file, FSTreeNode folder) throws Exception {
		printDebugMessage("Copy " + file.getLocation() + " to " + folder.getLocation() + "..."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		List<FSTreeNode> files = new ArrayList<FSTreeNode>();
		files.add(file);
		OpCopy copy = new OpCopy(files, folder);
		copy.run(new NullProgressMonitor());
		String location = folder.getLocation();
		String path = location + getPathSep() + file.name;
		return getFSNode(path);
	}

	protected FSTreeNode createFile(String fileName, FSTreeNode folder) throws Exception {
		printDebugMessage("Create " + fileName + " at " + folder.getLocation()); //$NON-NLS-1$ //$NON-NLS-2$
		OpCreateFile create = new OpCreateFile(folder, fileName);
		create.run(new NullProgressMonitor());
		String location = folder.getLocation();
		String path = location + getPathSep() + fileName;
		return getFSNode(path);
	}

	protected FSTreeNode createFolder(String folderName, FSTreeNode folder) throws Exception {
		printDebugMessage("Create " + folderName + " at " + folder.getLocation()); //$NON-NLS-1$ //$NON-NLS-2$
		OpCreateFolder create = new OpCreateFolder(folder, folderName);
		create.run(new NullProgressMonitor());
		String location = folder.getLocation();
		String path = location + getPathSep() + folderName;
		return getFSNode(path);
	}

	protected FSTreeNode move(FSTreeNode src, FSTreeNode dest) throws Exception {
		printDebugMessage("Move " + src.getLocation() + " to " + dest.getLocation()); //$NON-NLS-1$ //$NON-NLS-2$
		List<FSTreeNode> nodes = new ArrayList<FSTreeNode>();
		nodes.add(src);
		OpMove fsmove = new OpMove(nodes, dest);
		fsmove.run(new NullProgressMonitor());
		String path = dest.getLocation() + getPathSep() + src.name;
		return getFSNode(path);
	}

	protected FSTreeNode rename(FSTreeNode node, String newName) throws Exception {
		printDebugMessage("Rename " + node.name + " to " + newName); //$NON-NLS-1$ //$NON-NLS-2$
		OpRename fsmove = new OpRename(node, newName);
		fsmove.run(new NullProgressMonitor());
		String newPath = node.parent.getLocation()+getPathSep()+newName;
		return getFSNode(newPath);
	}

	protected void updateCache(FSTreeNode testFile) throws Exception {
		OpCacheUpdate update = new OpCacheUpdate(testFile);
		update.run(new NullProgressMonitor());
	}

	protected void commitCache(FSTreeNode testFile) throws Exception {
		OpCacheCommit commit = new OpCacheCommit(testFile);
		commit.run(new NullProgressMonitor());
	}

	protected void delete(FSTreeNode node) throws Exception {
		printDebugMessage("Delete " + node.getLocation() + "..."); //$NON-NLS-1$ //$NON-NLS-2$
		List<FSTreeNode> files = new ArrayList<FSTreeNode>();
		files.add(node);
		OpDelete delete = new OpDelete(files, null);
		delete.run(new NullProgressMonitor());
	}
}
