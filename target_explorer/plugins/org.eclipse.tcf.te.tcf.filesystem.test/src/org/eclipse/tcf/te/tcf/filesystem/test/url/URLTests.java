/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.test.url;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class URLTests extends TestCase {
	public static Test suite() {
		TestSuite suite = new TestSuite("URL Tests");
		suite.addTestSuite(TcfURLTests.class);
		suite.addTestSuite(URLReadTests.class);
		suite.addTestSuite(URLWriteTests.class);
		return suite;
	}
}
