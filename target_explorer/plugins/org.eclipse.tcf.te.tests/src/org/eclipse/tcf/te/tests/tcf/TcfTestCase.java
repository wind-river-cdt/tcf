/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tests.tcf;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.IPath;
import org.eclipse.tcf.core.TransientPeer;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.protocol.JSON;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.runtime.utils.Host;
import org.eclipse.tcf.te.runtime.utils.net.IPAddressUtil;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.ILocatorModel;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelLookupService;
import org.eclipse.tcf.te.tcf.locator.model.Model;
import org.eclipse.tcf.te.tcf.locator.nodes.PeerModel;
import org.eclipse.tcf.te.tests.CoreTestCase;

/**
 * TCF test case implementation.
 * <p>
 * Launches a TCF agent at local host and make it available for a test case.
 */
public class TcfTestCase extends CoreTestCase {
	// The agent launcher instance
	private AgentLauncher launcher;
	// The peer instance
	protected IPeer peer;
	// The peer model instance
	protected IPeerModel peerModel;

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tests.CoreTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
	    super.setUp();
	    launchAgent();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tests.CoreTestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		if (launcher != null) launcher.dispose();
		peer = null;
		peerModel = null;
	    super.tearDown();
	}

	/**
	 * Launches a TCF agent at local host.
	 */
	protected void launchAgent() {
		launcher = getlauncher();
		peerModel = getPeerModel(launcher);
		peer = peerModel.getPeer();
	}

	public static AgentLauncher getlauncher() {
		// Get the agent location
		IPath path = getAgentLocation();
		assertNotNull("Cannot determine TCF agent location.", path); //$NON-NLS-1$
		// Add the agent executable name
		path = path.append("agent"); //$NON-NLS-1$
		if (Host.isWindowsHost()) path = path.addFileExtension("exe"); //$NON-NLS-1$
		assertTrue("Invalid agent location: " + path.toString(), path.toFile().isFile()); //$NON-NLS-1$

		Throwable error = null;
		String message = null;

		// If the agent is not marked executable on Linux, we have to change that
		if (Host.isLinuxHost() && !path.toFile().canExecute()) {
			try {
				Runtime.getRuntime().exec(new String[] { "chmod", "u+x", path.toString() }); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (IOException e) {
				error = e;
				message = e.getLocalizedMessage();
			}
		}
		assertNull("Failed to make the agent executable for the current user.", message); //$NON-NLS-1$

		error = null;
		message = null;

		assertTrue("Agent should be executable but is not.", path.toFile().canExecute()); //$NON-NLS-1$

		// Create the agent launcher
		AgentLauncher launcher = new AgentLauncher(path);
		try {
			launcher.launch();
		} catch (Throwable e) {
			error = e;
			message = e.getLocalizedMessage();
		}
		assertNull("Failed to launch agent: " + message, error); //$NON-NLS-1$

		error = null;
		message = null;

		assertNotNull("Process handle not associated with launcher.", launcher.getProcess()); //$NON-NLS-1$
		assertNotNull("Process output reader not associated with launcher.", launcher.getOutputReader()); //$NON-NLS-1$

		Process process = launcher.getProcess();
		int exitCode = -1;
		try {
			exitCode = process.exitValue();
		} catch (IllegalThreadStateException e) {
			error = e;
			message = e.getLocalizedMessage();
		}
		assertNotNull("Agent process died with exit code " + exitCode, error); //$NON-NLS-1$

		error = null;
		message = null;

		// The agent is started with "-S" to write out the peer attributes in JSON format.
		String output = null;
		int counter = 10;
		while (counter > 0 && output == null) {
			// Try to read in the output
			output = launcher.getOutputReader().getOutput();
			if ("".equals(output)) { //$NON-NLS-1$
				output = null;
				waitAndDispatch(200);
			}
			counter--;
		}
		assertNotNull("Failed to read output from agent.", output); //$NON-NLS-1$

		// Strip away "Server-Properties:"
		output = output.replace("Server-Properties:", " "); //$NON-NLS-1$ //$NON-NLS-2$
		output = output.trim();
		parseOne(output);
		return launcher;
	}
	
	public static IPeerModel getPeerModel(AgentLauncher launcher) {
		// The agent is started with "-S" to write out the peer attributes in JSON format.
		String output = null;
		int counter = 10;
		while (counter > 0 && output == null) {
			// Try to read in the output
			output = launcher.getOutputReader().getOutput();
			if ("".equals(output)) { //$NON-NLS-1$
				output = null;
				waitAndDispatch(200);
			}
			counter--;
		}
		assertNotNull("Failed to read output from agent.", output); //$NON-NLS-1$

		// Strip away "Server-Properties:"
		output = output.replace("Server-Properties:", " "); //$NON-NLS-1$ //$NON-NLS-2$
		output = output.trim();

		// Read into an object
		
		
		Object object = parseOne(output);

		@SuppressWarnings("unchecked")
        final Map<String, String> attrs = new HashMap<String, String>((Map<String, String>)object);

		// Lookup the corresponding peer object
		final ILocatorModel model = Model.getModel();
		assertNotNull("Failed to access locator model instance.", model); //$NON-NLS-1$

		// The expected peer id is "<transport>:<canonical IP>:<port>"
		String transport = attrs.get(IPeer.ATTR_TRANSPORT_NAME);
		assertNotNull("Unexpected return value 'null'.", transport); //$NON-NLS-1$
		String port = attrs.get(IPeer.ATTR_IP_PORT);
		assertNotNull("Unexpected return value 'null'.", port); //$NON-NLS-1$
		final String ip = IPAddressUtil.getInstance().getCanonicalAddress();
		assertNotNull("Unexpected return value 'null'.", ip); //$NON-NLS-1$

		final String id = transport + ":" + ip + ":" + port; //$NON-NLS-1$ //$NON-NLS-2$
		final AtomicReference<IPeerModel> node = new AtomicReference<IPeerModel>();

		final IPeerModel[] peerModel = new IPeerModel[1]; 
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				node.set(model.getService(ILocatorModelLookupService.class).lkupPeerModelById(id));
				// If the peer model is not found by id, try the agent id as fallback.
				if (node.get() == null) {
					String agentID = attrs.get(IPeer.ATTR_AGENT_ID);
					assertNotNull("Unexpected return value 'null'.", agentID); //$NON-NLS-1$
					IPeerModel[] candidates = model.getService(ILocatorModelLookupService.class).lkupPeerModelByAgentId(agentID);
					if (candidates != null && candidates.length > 0) node.set(candidates[0]);
				}
				
				// If the peer model is still not found, we create a transient peer
				IPeer peer = null;
				if (node.get() == null) {
					attrs.put(IPeer.ATTR_ID, id);
					attrs.put(IPeer.ATTR_IP_HOST, ip);
					peer = new TransientPeer(attrs);
					peerModel[0] = new PeerModel(model, peer);
				} else {
					peerModel[0] = node.get();
					peer = peerModel[0].getPeer();
				}
				assertNotNull("Failed to determine the peer to use for the tests.", peer); //$NON-NLS-1$
			}
		};
		assertFalse("Test is running in TCF dispatch thread.", Protocol.isDispatchThread()); //$NON-NLS-1$
		Protocol.invokeAndWait(runnable);

		return peerModel[0];
	}
	
	private static Object parseOne(final String _output) {
		final Object[] _object = new Object[1];
		final String[] _message = new String[1];
		final Throwable[] _error = new Throwable[1];
		
		Protocol.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				try {
				_object[0] = JSON.parseOne(_output.getBytes("UTF-8")); //$NON-NLS-1$
				} catch (IOException e) {
					_error[0] = e;
					_message[0] = e.getLocalizedMessage();
				}
			}
		});
		assertNull("Failed to parse server properties: " + _message[0], _error[0]); //$NON-NLS-1$
		assertTrue("Server properties object is not of expected type Map.", _object[0] instanceof Map); //$NON-NLS-1$
		return _object[0];
	}
	
	/**
	 * Returns the agent location.
	 *
	 * @return The agent location or <code>null</code> if not found.
	 */
	public static IPath getAgentLocation() {
		return getDataLocation("agent", true, true); //$NON-NLS-1$
	}
}
