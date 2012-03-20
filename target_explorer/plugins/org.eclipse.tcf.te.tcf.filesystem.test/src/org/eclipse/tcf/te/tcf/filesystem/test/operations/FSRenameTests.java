/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.test.operations;

import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;

public class FSRenameTests extends OperationTestBase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		String path = testFolder.getLocation() + getPathSep() + "hello.txt";
		if (pathExists(path)) {
			FSTreeNode node = getFSNode(path);
			delete(node);
		}
	}

	public void testRename() throws Exception {
		testFile = rename(testFile, "hello.txt");
		String path = testFolder.getLocation() + getPathSep() + "hello.txt";
		assertTrue(pathExists(path));
	}

	@Override
	protected void tearDown() throws Exception {
		rename(testFile, "test.txt");
	}

}
