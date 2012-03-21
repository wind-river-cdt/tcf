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

import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;


public class FSCreateFileTests extends OperationTestBase {
	protected FSTreeNode newFile;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		String path = test1Folder.getLocation() + getPathSep() + "newfile.txt"; //$NON-NLS-1$
		FSTreeNode node = getFSNode(path);
		if (node != null) {
			delete(node);
		}
	}

	public void testCreate() throws Exception {
		newFile = createFile("newfile.txt", test1Folder); //$NON-NLS-1$
		String path = test1Folder.getLocation() + getPathSep() + "newfile.txt"; //$NON-NLS-1$
		assertTrue(pathExists(path));
	}

	@Override
	protected void tearDown() throws Exception {
		if (newFile != null) {
			delete(newFile);
		}
		super.tearDown();
	}

}
