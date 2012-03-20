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

import java.io.File;

import org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSRefresh;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;

@SuppressWarnings("restriction")
public class FSRefreshTests extends OperationTestBase {
	
	@Override
    protected void setUp() throws Exception {
	    super.setUp();
	    String path = getTestRoot() + getPathSep() + getTestPath();
	    File dir = new File(path);
	    File newdir = new File(dir, "newdir");
	    newdir.mkdir();
    }

	public void testRefresh() throws Exception {
		FSRefresh refresh = new FSRefresh(testRoot, null);
		refresh.run(getProgressMonitor());
	    String path = getTestRoot() + getPathSep() + getTestPath()+getPathSep()+"newdir";
		FSTreeNode node = getFSNode(path);
		assertNotNull(node);
	}

	@Override
    protected void tearDown() throws Exception {
	    super.tearDown();
	    String path = getTestRoot() + getPathSep() + getTestPath();
	    File dir = new File(path);
	    File newdir = new File(dir, "newdir");
	    newdir.delete();
    }
}
