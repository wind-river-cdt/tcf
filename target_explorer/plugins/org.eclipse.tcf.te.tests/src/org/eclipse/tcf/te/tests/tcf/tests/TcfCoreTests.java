/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tests.tcf.tests;

import java.util.concurrent.atomic.AtomicReference;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.te.runtime.callback.Callback;
import org.eclipse.tcf.te.tcf.core.Tcf;
import org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager;
import org.eclipse.tcf.te.tests.activator.UIPlugin;
import org.eclipse.tcf.te.tests.tcf.TcfTestCase;

/**
 * Core TCF tests.
 */
public class TcfCoreTests extends TcfTestCase {

	/**
	 * Provides a test suite to the caller which combines all single
	 * test bundled within this category.
	 *
	 * @return Test suite containing all test for this test category.
	 */
	public static Test getTestSuite() {
		TestSuite testSuite = new TestSuite("Core TCF tests"); //$NON-NLS-1$

		// add ourself to the test suite
		testSuite.addTestSuite(TcfCoreTests.class);

		return testSuite;
	}

	/**
	 * Test the channel manager implementation.
	 */
	public void testChannelManager() {
		assertNotNull("Precondition Failure: peer is not available.", peer); //$NON-NLS-1$
		assertNotNull("Unexpected return value 'null'.", Tcf.getChannelManager()); //$NON-NLS-1$

		final AtomicReference<Callback> callback = new AtomicReference<Callback>();
		callback.set(new Callback());

		Tcf.getChannelManager().openChannel(peer, true, new IChannelManager.DoneOpenChannel() {

			@Override
			public void doneOpenChannel(Throwable error, IChannel channel) {
				callback.get().setResult(channel);
				IStatus status = new Status(error != null ? IStatus.ERROR : IStatus.OK,
											UIPlugin.getUniqueIdentifier(),
											error != null ? error.getLocalizedMessage() : null,
											error);
				callback.get().done(this, status);
			}
		});

		waitAndDispatch(0, callback.get().getDoneConditionTester(null));

		IStatus status = callback.get().getStatus();
		assertNotNull("Unexpected return value 'null'.", status); //$NON-NLS-1$
		assertTrue("Failed to open channel to test peer. Possible cause: " + status.getMessage(), status.isOK()); //$NON-NLS-1$

		IChannel channel = (IChannel)callback.get().getResult();
		assertNotNull("Unexpected return value 'null'.", channel); //$NON-NLS-1$
		assertTrue("Channel is not in expected open state.", channel.getState() == IChannel.STATE_OPEN); //$NON-NLS-1$

		Tcf.getChannelManager().closeChannel(channel);
		int counter = 10;
		while (counter > 0 && channel.getState() != IChannel.STATE_CLOSED) {
			waitAndDispatch(200);
		}
		assertTrue("Channel is not in expected closed state.", channel.getState() == IChannel.STATE_CLOSED); //$NON-NLS-1$
	}
}
