/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.debug.test;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * 
 */
public class UtilsSuite extends TestSuite {

    public UtilsSuite() throws Exception {
        addTest(new TestSuite(CacheTests.class));
        addTest(new TestSuite(TransactionTests.class));
        addTest(new TestSuite(QueryTests.class));
        addTest(new TestSuite(RangeCacheTests.class));
    }

    /**
     * Returns the suite.  This is required to
     * use the JUnit Launcher.
     */
    public static Test suite() throws Exception {
        return new UtilsSuite();
    }

}
