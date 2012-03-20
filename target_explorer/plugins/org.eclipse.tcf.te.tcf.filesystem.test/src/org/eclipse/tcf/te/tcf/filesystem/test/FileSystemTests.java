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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.tcf.te.tcf.filesystem.test.callbacks.CallbackTests;
import org.eclipse.tcf.te.tcf.filesystem.test.operations.OperationTests;
import org.eclipse.tcf.te.tcf.filesystem.test.url.URLTests;
import org.eclipse.tcf.te.tcf.filesystem.test.utils.UtilTests;
import org.eclipse.tcf.te.tests.tcf.TcfTestCase;

public class FileSystemTests extends TcfTestCase {
	public static Test suite() {
		TestSuite suite = new TestSuite("File System Tests");
		suite.addTest(CallbackTests.suite());
		suite.addTest(OperationTests.suite());
		suite.addTest(URLTests.suite());
		suite.addTest(UtilTests.suite());
		suite.addTestSuite(FileSystemTests.class);
		return suite;
	}
	public void testDummy(){}
}
