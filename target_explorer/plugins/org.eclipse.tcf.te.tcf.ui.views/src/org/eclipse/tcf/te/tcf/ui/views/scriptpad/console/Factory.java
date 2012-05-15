/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.ui.views.scriptpad.console;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleFactory;
import org.eclipse.ui.console.IConsoleManager;

/**
 * Script Pad console factory implementation.
 */
public class Factory implements IConsoleFactory {
	// Reference to the console
	private static volatile Console console;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.console.IConsoleFactory#openConsole()
	 */
	@Override
    public void openConsole() {
		showConsole();
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
	 * Shows the Script Pad console.
	 */
	public static void showConsole() {
		// Create a new console if not yet created
		if (console == null) console = new Console();

		// Get the console manager
		IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();
		// Add the console to the manager if not yet done
		if (!isConsoleAlreadyAdded(console)) manager.addConsoles(new IConsole[] {console});
		// Show the console view with the TCF communication monitor console
		manager.showConsoleView(console);
	}

	/**
	 * Returns the Script Pad console.
	 *
	 * @return The Script Pad console or <code>null</code>.
	 */
	public static Console getConsole() {
		return console;
	}

	/**
	 * Shutdown the Script Pad console.
	 */
	public static void shutdownConsole() {
		if (console != null) {
			console.shutdown();
			console = null;
		}
	}
}
