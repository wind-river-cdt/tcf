/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tests.statushandler;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.tcf.te.runtime.interfaces.IConditionTester;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.runtime.properties.PropertiesContainer;
import org.eclipse.tcf.te.runtime.statushandler.StatusHandlerManager;
import org.eclipse.tcf.te.runtime.statushandler.interfaces.IStatusHandler;
import org.eclipse.tcf.te.runtime.statushandler.interfaces.IStatusHandlerConstants;
import org.eclipse.tcf.te.runtime.statushandler.status.QuestionStatus;
import org.eclipse.tcf.te.runtime.statushandler.status.YesNoCancelStatus;
import org.eclipse.tcf.te.runtime.utils.Host;
import org.eclipse.tcf.te.tests.CoreTestCase;
import org.eclipse.tcf.te.tests.activator.UIPlugin;

/**
 * Status handler test cases.
 */
public class StatusHandlerTestCase extends CoreTestCase {

	/**
	 * Provides a test suite to the caller which combines all single
	 * test bundled within this category.
	 *
	 * @return Test suite containing all test for this test category.
	 */
	public static Test getTestSuite() {
		TestSuite testSuite = new TestSuite("Test status handler contributions"); //$NON-NLS-1$

			// add ourself to the test suite
			testSuite.addTestSuite(StatusHandlerTestCase.class);

		return testSuite;
	}

	/**
	 * Test the basic status handler extension contribution mechanism.
	 */
	public void testContributions() {
		assertNotNull("Unexpected return value 'null'.", StatusHandlerManager.getInstance()); //$NON-NLS-1$

		int testHandlerCount = 0;

		IStatusHandler[] handlers = StatusHandlerManager.getInstance().getHandlers(false);
		for (IStatusHandler handler : handlers) {
			if (handler.getId().startsWith("org.eclipse.tcf.te.tests")) { //$NON-NLS-1$
				testHandlerCount++;
			}
		}

		assertEquals("Unexpected number of contributed test status handler.", 2, testHandlerCount); //$NON-NLS-1$
	}

	/**
	 * Test status handler extension contribution mechanism with context objects.
	 */
	public void testContributionsWithContext() {
		assertNotNull("Unexpected return value 'null'.", StatusHandlerManager.getInstance()); //$NON-NLS-1$

		List<String> handlerIds = new ArrayList<String>();

		IStatusHandler[] handlers = StatusHandlerManager.getInstance().getHandler(this);
		for (IStatusHandler handler : handlers) {
			handlerIds.add(handler.getId());
		}

		assertTrue("Test case enabled test status handler not active.", handlerIds.contains("org.eclipse.tcf.te.tests.handler1")); //$NON-NLS-1$ //$NON-NLS-2$
		assertFalse("Interrupt condition enabled test status handler is active.", handlerIds.contains("org.eclipse.tcf.te.tests.handler2")); //$NON-NLS-1$ //$NON-NLS-2$

		handlerIds.clear();

		IConditionTester context = new IConditionTester() {
			/* (non-Javadoc)
			 * @see org.eclipse.tcf.te.runtime.interfaces.IConditionTester#isConditionFulfilled()
			 */
			@Override
			public boolean isConditionFulfilled() {
			    return false;
			}
			/* (non-Javadoc)
			 * @see org.eclipse.tcf.te.runtime.interfaces.IConditionTester#cleanup()
			 */
			@Override
			public void cleanup() {
			}
		};

		handlers = StatusHandlerManager.getInstance().getHandler(context);
		for (IStatusHandler handler : handlers) {
			handlerIds.add(handler.getId());
		}

		assertTrue("Test case enabled test status handler not active.", handlerIds.contains("org.eclipse.tcf.te.tests.handler1")); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue("Interrupt condition enabled test status handler not active.", handlerIds.contains("org.eclipse.tcf.te.tests.handler2")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Test the default status handler.
	 */
	public void testDefaultStatusHandler() {
		assertNotNull("Unexpected return value 'null'.", StatusHandlerManager.getInstance()); //$NON-NLS-1$
		assertFalse("Failed to toggle interactive mode.", Host.isInteractive()); //$NON-NLS-1$

		IStatusHandler handler = null;

		IStatusHandler[] handlers = StatusHandlerManager.getInstance().getHandlers(false);
		for (IStatusHandler candidate : handlers) {
			if (candidate.getId().equals("org.eclipse.tcf.te.statushandler.default")) { //$NON-NLS-1$
				handler = candidate;
				break;
			}
		}

		assertNotNull("Failed to determine default status handler.", handler); //$NON-NLS-1$

		IPropertiesContainer data = new PropertiesContainer();
		data.setProperty(IStatusHandlerConstants.PROPERTY_TITLE, "Statushandler Test"); //$NON-NLS-1$

		IStatus status = new Status(IStatus.INFO, UIPlugin.getUniqueIdentifier(), "Info"); //$NON-NLS-1$
		handler.handleStatus(status, data, null);

		status = new Status(IStatus.WARNING, UIPlugin.getUniqueIdentifier(), "Warning"); //$NON-NLS-1$
		handler.handleStatus(status, data, null);

		status = new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(), "Error"); //$NON-NLS-1$
		handler.handleStatus(status, data, null);

		status = new QuestionStatus(UIPlugin.getUniqueIdentifier(), "Question"); //$NON-NLS-1$
		handler.handleStatus(status, data, null);

		status = new QuestionStatus(UIPlugin.getUniqueIdentifier(), "Question", new Throwable()); //$NON-NLS-1$
		handler.handleStatus(status, data, null);

		status = new QuestionStatus(UIPlugin.getUniqueIdentifier(), 1, "Question", new Throwable()); //$NON-NLS-1$
		handler.handleStatus(status, data, null);

		status = new YesNoCancelStatus(UIPlugin.getUniqueIdentifier(), "YesNoCancel"); //$NON-NLS-1$
		handler.handleStatus(status, data, null);

		status = new YesNoCancelStatus(UIPlugin.getUniqueIdentifier(), "YesNoCancel", new Throwable()); //$NON-NLS-1$
		handler.handleStatus(status, data, null);

		status = new YesNoCancelStatus(UIPlugin.getUniqueIdentifier(), 1, "YesNoCancel", new Throwable()); //$NON-NLS-1$
		handler.handleStatus(status, data, null);
	}
}
