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

import java.io.File;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.OpRefresh;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;

public class FSRefreshTests extends OperationTestBase {

	@Override
    protected void setUp() throws Exception {
	    super.setUp();
	    String path = getTestRoot() + getPathSep() + getTestPath();
	    File dir = new File(path);
	    File newdir = new File(dir, "newdir"); //$NON-NLS-1$
	    newdir.mkdir();
    }

	public void testRefresh() throws Exception {
		OpRefresh refresh = new OpRefresh(testRoot);
		refresh.run(new NullProgressMonitor());
	    String path = getTestRoot() + getPathSep() + getTestPath()+getPathSep()+"newdir"; //$NON-NLS-1$
		FSTreeNode node = getFSNode(path);
		assertNotNull(node);
	}

	@Override
    protected void tearDown() throws Exception {
	    String path = getTestRoot() + getPathSep() + getTestPath();
	    File dir = new File(path);
	    File newdir = new File(dir, "newdir"); //$NON-NLS-1$
	    newdir.delete();
		super.tearDown();
    }
}
