/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tests.tcf.filesystem;

import java.io.File;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.services.IFileSystem;
import org.eclipse.tcf.services.IFileSystem.DoneStat;
import org.eclipse.tcf.services.IFileSystem.FileAttrs;
import org.eclipse.tcf.services.IFileSystem.FileSystemException;
import org.eclipse.tcf.te.tcf.core.Tcf;
import org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSCreateFile;
import org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSCreateFolder;
import org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSOperation;
import org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSRefresh;
import org.eclipse.tcf.te.tcf.filesystem.model.FSModel;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.tcf.te.tests.tcf.TcfTestCase;

@SuppressWarnings("restriction")
public class FSPeerTestCase extends TcfTestCase {

	protected FSTreeNode testRoot;
	protected FSTreeNode testFolder;
	protected FSTreeNode testFile;
	protected FSTreeNode test1Folder;
	protected FSTreeNode test1File;
	protected FSTreeNode test11Folder;
	protected FSTreeNode test11File;
	protected FSTreeNode test12Folder;
	protected FSTreeNode test12File;
	protected FSTreeNode test2Folder;
	protected FSTreeNode test2File;
	protected FSTreeNode test21Folder;
	protected FSTreeNode test21File;
	protected FSTreeNode test22Folder;
	protected FSTreeNode test22File;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		assertNotNull(peerModel);
		assertNotNull(peer);

		printMessage("The work directory of File System tests is located at:" + getTestRoot()); //$NON-NLS-1$
		testRoot = FSModel.findTreeNode(peerModel, getTestRoot());
		assertNotNull(testRoot);
		String path = getTestRoot() + getPathSep() + getTestPath();
		testFolder = prepareFolder(path, getTestPath(), testRoot);
		testFile = prepareFile(path + getPathSep() + "test.txt", "test.txt", testFolder); //$NON-NLS-1$ //$NON-NLS-2$
		test1Folder = prepareFolder(path + getPathSep() + "test1", "test1", testFolder); //$NON-NLS-1$ //$NON-NLS-2$
		String path1 = path + getPathSep() + "test1"; //$NON-NLS-1$
		test1File = prepareFile(path1 + getPathSep() + "test1.txt", "test1.txt", test1Folder); //$NON-NLS-1$ //$NON-NLS-2$
		test11Folder = prepareFolder(path1 + getPathSep() + "test11", "test11", test1Folder); //$NON-NLS-1$ //$NON-NLS-2$
		String path11 = path1 + getPathSep() + "test11"; //$NON-NLS-1$
		test11File = prepareFile(path11 + getPathSep() + "test11.txt", "test11.txt", test11Folder); //$NON-NLS-1$ //$NON-NLS-2$
		test12Folder = prepareFolder(path1 + getPathSep() + "test12", "test12", test1Folder); //$NON-NLS-1$ //$NON-NLS-2$
		String path12 = path1 + getPathSep() + "test12"; //$NON-NLS-1$
		test12File = prepareFile(path12 + getPathSep() + "test12.txt", "test12.txt", test12Folder); //$NON-NLS-1$ //$NON-NLS-2$
		test2Folder = prepareFolder(path + getPathSep() + "test2", "test2", testFolder); //$NON-NLS-1$ //$NON-NLS-2$
		String path2 = path + getPathSep() + "test2"; //$NON-NLS-1$
		test2File = prepareFolder(path2 + getPathSep() + "test2.txt", "test2.txt", test2Folder); //$NON-NLS-1$ //$NON-NLS-2$
		test21Folder = prepareFolder(path2 + getPathSep() + "test21", "test21", test2Folder); //$NON-NLS-1$ //$NON-NLS-2$
		String path21 = path2 + getPathSep() + "test21"; //$NON-NLS-1$
		test21File = prepareFile(path21 + getPathSep() + "test21.txt", "test21.txt", test21Folder); //$NON-NLS-1$ //$NON-NLS-2$
		test22Folder = prepareFolder(path2 + getPathSep() + "test22", "test22", test2Folder); //$NON-NLS-1$ //$NON-NLS-2$
		String path22 = path2 + getPathSep() + "test22"; //$NON-NLS-1$
		test22File = prepareFile(path22 + getPathSep() + "test22.txt", "test22.txt", test22Folder); //$NON-NLS-1$ //$NON-NLS-2$
	}

	protected FSTreeNode prepareFolder(String folderPath, String folderName, FSTreeNode parentFolder) throws Exception {
		printDebugMessage("Prepare folder " + folderPath); //$NON-NLS-1$
		FSTreeNode testFolder = getFSNode(folderPath);
		if (testFolder == null) {
			FSCreateFolder fs = new FSCreateFolder(parentFolder, folderName);
			fs.run(new NullProgressMonitor());
			testFolder = getFSNode(folderPath);
		}
		return testFolder;
	}

	protected boolean pathExists(String path) {
		IChannel channel = null;
		try {
			channel = FSOperation.openChannel(peer);
			if (channel != null) {
				IFileSystem service = FSOperation.getBlockingFileSystem(channel);
				if (service != null) {
					final boolean[] exists = new boolean[1];
					service.lstat(path, new DoneStat() {
						@Override
						public void doneStat(IToken token, FileSystemException error, FileAttrs attrs) {
							exists[0] = error == null;
						}
					});
					return exists[0];
				}
			}
		}
		catch (Exception e) {
		}
		finally {
			if (channel != null) Tcf.getChannelManager().closeChannel(channel);
		}
		return false;
	}

	protected FSTreeNode getFSNode(String path) {
		FSTreeNode node = FSModel.findTreeNode(peerModel, path);
		if (node == null) {
			FSRefresh refresh = new FSRefresh(testRoot, null);
			try {
				refresh.run(new NullProgressMonitor());
			} catch (Exception e) {}

			node = FSModel.findTreeNode(peerModel, path);
		}
		return node;
	}

	protected FSTreeNode prepareFile(String filePath, String fileName, FSTreeNode parentFolder) throws Exception {
		printDebugMessage("Prepare file " + filePath); //$NON-NLS-1$
		FSTreeNode testFile = getFSNode(filePath);
		if (testFile == null) {
			FSCreateFile fs = new FSCreateFile(parentFolder, fileName);
			fs.run(new NullProgressMonitor());
			testFile = getFSNode(filePath);
		}
		return testFile;
	}

	protected String getTestPath() {
		return "test"; //$NON-NLS-1$
	}

	private String rootDir;

	protected String getTestRoot() {
		if (rootDir == null) {
			String wdir = System.getProperty("user.dir"); //$NON-NLS-1$
			if (wdir == null) wdir = System.getProperty("user.home"); //$NON-NLS-1$
			rootDir = wdir + getPathSep() + ".tmp_test_root"; //$NON-NLS-1$
			File file = new File(rootDir);
			if (!file.exists()) {
				file.mkdirs();
			}
		}
		return rootDir;
	}

	protected String getPathSep() {
		return File.separator;
	}
}
