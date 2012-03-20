/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.test.utils;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class UtilTests extends TestCase {
	public static Test suite() {
		TestSuite suite = new TestSuite("Utility Tests");
		suite.addTestSuite(CacheManagerTest.class);
		suite.addTestSuite(ContentTypeHelperTest.class);
		suite.addTestSuite(StateManagerTest.class);
		suite.addTestSuite(UserManagerTest.class);
		return suite;
	}
}
