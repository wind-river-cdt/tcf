/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.ui.views.monitor.console;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;

/**
 * Communication monitor console factory implementation.
 */
public class Factory {
	// Map of active consoles per peer
	private static final Map<IPeer, Console> CONSOLES = new HashMap<IPeer, Console>();

	/**
	 * Show the console. Called by {@link Console}.
	 *
	 * @param peer The peer. Must not be <code>null</code>.
	 * @param createNew If <code>true</code>, a new console instance will be created and returned.
	 *
	 * @return The console instance or <code>null</code>.
	 */
	public static Console showConsole(IPeer peer, boolean createNew) {
		Assert.isNotNull(peer);
		// Get the console for the channel
		Console console = getConsole(peer, createNew);
		if (console != null) {
			// Get the console manager
			IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();
			// Add the console to the manager if not yet done
			if (!isConsoleAlreadyAdded(console)) manager.addConsoles(new IConsole[] {console});
			// Show the console view with the monitor console
			manager.showConsoleView(console);
		}

		return console;
	}

	/**
	 * Checks if the given console is already added to the console manager.
	 *
	 * @param console The console. Must not be <code>null</code>.
	 * @return <code>True</code> if the console exist, <code>false</code> otherwise.
	 */
	private static boolean isConsoleAlreadyAdded(IConsole console) {
		Assert.isNotNull(console);
		// Assume the console to be not yet added
		boolean alreadyAdded = false;
		// Check all consoles if it is the same instance as the passed in one
		for (IConsole candidate : ConsolePlugin.getDefault().getConsoleManager().getConsoles()) {
			if (candidate == console) {
				alreadyAdded = true;
				break;
			}
		}
		return alreadyAdded;
	}

	/**
	 * Returns the communication monitor console instance associated with the given peer.
	 *
	 * @param peer The peer. Must not be <code>null</code>.
	 * @param createNew If <code>true</code>, a new console instance will be created and returned.
	 *
	 * @return The console instance or <code>null</code>.
	 */
	public static Console getConsole(IPeer peer, boolean createNew) {
		Assert.isNotNull(peer);
		// Lookup the peer specific console
		Console console = CONSOLES.get(peer);
		if (console == null && createNew) {
			// Create a new console instance
			console = new Console(peer);
			// And store the new console in the map
			CONSOLES.put(peer, console);
		}

		// Add the console to the manager if not yet done
		if (console != null && !isConsoleAlreadyAdded(console)) {
			ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] {console});
		}

		return console;
	}

	/**
	 * Shutdown all known consoles.
	 */
	public static void shutdown() {
		// Shutdown all channel specific consoles
		for (Console console : CONSOLES.values()) console.shutdown();
		// Clear the map
		CONSOLES.clear();
	}
}
