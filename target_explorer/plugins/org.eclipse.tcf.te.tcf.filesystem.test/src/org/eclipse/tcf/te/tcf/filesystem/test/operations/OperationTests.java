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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class OperationTests extends TestCase {
	public static Test suite() {
		TestSuite suite = new TestSuite("Operation Tests");
		suite.addTestSuite(FSCopyTests.class);
		suite.addTestSuite(FSCreateFileTests.class);
		suite.addTestSuite(FSCreateFolderTests.class);
		suite.addTestSuite(FSDeleteTests.class);
		suite.addTestSuite(FSMoveTests.class);
		suite.addTestSuite(FSRefreshTests.class);
		suite.addTestSuite(FSRenameTests.class);
		return suite;
	}
}
