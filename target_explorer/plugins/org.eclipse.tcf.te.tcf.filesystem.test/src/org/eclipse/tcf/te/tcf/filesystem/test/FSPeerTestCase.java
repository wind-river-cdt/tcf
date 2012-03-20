/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.test;

import java.io.File;
import java.util.Map;

import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.services.IFileSystem;
import org.eclipse.tcf.services.IFileSystem.DoneStat;
import org.eclipse.tcf.services.IFileSystem.FileAttrs;
import org.eclipse.tcf.services.IFileSystem.FileSystemException;
import org.eclipse.tcf.te.tcf.core.Tcf;
import org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSCreateFile;
import org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSCreateFolder;
import org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSOperation;
import org.eclipse.tcf.te.tcf.filesystem.model.FSModel;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.ILocatorModel;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelRefreshService;
import org.eclipse.tcf.te.tcf.locator.model.Model;

@SuppressWarnings("restriction")
public class FSPeerTestCase extends FSTestCase {
	protected static IPeerModel testPeer;
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
		waitForTestPeer();
		assertNotNull(testPeer);
		testRoot = getFSNode(getTestRoot());
		assertNotNull(testRoot);
		String path = getTestRoot() + getPathSep() + getTestPath();
		testFolder = prepareFolder(path, getTestPath(), testRoot);
		testFile = prepareFile(path + getPathSep() + "test.txt", "test.txt", testFolder);
		test1Folder = prepareFolder(path + getPathSep() + "test1", "test1", testFolder);
		String path1 = path + getPathSep() + "test1";
		test1File = prepareFile(path1 + getPathSep() + "test1.txt", "test1.txt", test1Folder);
		test11Folder = prepareFolder(path1 + getPathSep() + "test11", "test11", test1Folder);
		String path11 = path1 + getPathSep() + "test11";
		test11File = prepareFile(path11 + getPathSep() + "test11.txt", "test11.txt", test11Folder);
		test12Folder = prepareFolder(path1 + getPathSep() + "test12", "test12", test1Folder);
		String path12 = path1 + getPathSep() + "test12";
		test12File = prepareFile(path12 + getPathSep() + "test12.txt", "test12.txt", test12Folder);
		test2Folder = prepareFolder(path + getPathSep() + "test2", "test2", testFolder);
		String path2 = path + getPathSep() + "test2";
		test2File = prepareFolder(path2 + getPathSep() + "test2.txt", "test2.txt", test2Folder);
		test21Folder = prepareFolder(path2 + getPathSep() + "test21", "test21", test2Folder);
		String path21 = path2 + getPathSep() + "test21";
		test21File = prepareFile(path21 + getPathSep() + "test21.txt", "test21.txt", test21Folder);
		test22Folder = prepareFolder(path2 + getPathSep() + "test22", "test22", test2Folder);
		String path22 = path2 + getPathSep() + "test22";
		test22File = prepareFile(path22 + getPathSep() + "test22.txt", "test22.txt", test22Folder);
	}

	protected FSTreeNode prepareFolder(String folderPath, String folderName, FSTreeNode parentFolder) throws Exception {
		log("Prepare folder " + folderPath);
		FSTreeNode testFolder = getFSNode(folderPath);
		if (testFolder == null) {
			FSCreateFolder fs = new FSCreateFolder(parentFolder, folderName);
			fs.run(getProgressMonitor());
			testFolder = getFSNode(folderPath);
		}
		return testFolder;
	}

	protected boolean pathExists(String path) {
		// Remove its self from the clipped nodes.
		IChannel channel = null;
		try {
			channel = FSOperation.openChannel(testPeer.getPeer());
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
		return FSModel.findTreeNode(testPeer, path);
	}

	protected FSTreeNode prepareFile(String filePath, String fileName, FSTreeNode parentFolder) throws Exception {
		log("Prepare file " + filePath);
		FSTreeNode testFile = getFSNode(filePath);
		if (testFile == null) {
			FSCreateFile fs = new FSCreateFile(parentFolder, fileName);
			fs.run(getProgressMonitor());
			testFile = getFSNode(filePath);
		}
		return testFile;
	}

	protected String getTestPath() {
		return "test";
	}

	private String rootDir;
	protected String getTestRoot() {
		if(rootDir == null) {
			String wdir = System.getProperty("user.dir");
			if(wdir == null) 
				wdir = System.getProperty("user.home");
			rootDir = wdir + getPathSep() + ".tmp_test_root";
			File file = new File(rootDir);
			if(!file.exists()) {
				file.mkdirs();
			}
		}
		return rootDir;
	}

	protected String getPathSep() {
		return File.separator;
	}

	@SuppressWarnings("unchecked")
    private void waitForTestPeer() {
		if (testPeer == null) {
			log("Waiting for the peer " + peer.getID() + " to come up ...");
			IPeerModel pm = null;
			while (pm == null) {
				ILocatorModel locator = Model.getModel();
				final ILocatorModelRefreshService service = (ILocatorModelRefreshService) locator
				                .getAdapter(ILocatorModelRefreshService.class);
				if (Protocol.isDispatchThread()) {
					service.refresh();
				}
				else {
					Protocol.invokeAndWait(new Runnable() {
						@Override
						public void run() {
							service.refresh();
						}
					});
				}
				try {
					Thread.sleep(1000);
				}
				catch (InterruptedException e) {
				}
				Map<String, IPeerModel> map = (Map<String, IPeerModel>) locator.getAdapter(Map.class);
				pm = map.get(peer.getID());
			}
			log("Got the peer!");
			testPeer = pm;
		}
	}
}
