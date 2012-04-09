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

import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;


public class FSMoveTests extends OperationTestBase {
	protected FSTreeNode originalFolder;

	public void testMove() throws Exception {
		originalFolder = test22File.parent;
		test22File = move(test22File, test1Folder);
		String origPath = originalFolder.getLocation() + getPathSep() + test22File.name;
		assertFalse(pathExists(origPath));
		String nowPath = test1Folder.getLocation() + getPathSep() + test22File.name;
		assertTrue(pathExists(nowPath));
	}

	@Override
	protected void tearDown() throws Exception {
		move(test22File, originalFolder);
		super.tearDown();
	}
}
