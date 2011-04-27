/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.ui.internal.model;

import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.ILocatorModel;
import org.eclipse.tm.te.tcf.locator.nodes.LocatorModel;
import org.eclipse.tm.te.tcf.ui.internal.navigator.ModelListener;


/**
 * Target Explorer: Helper class to instantiate and initialize the TCF locator model.
 */
public final class Model {
	// Reference to the locator model
	/* default */ static ILocatorModel fLocatorModel;

	/**
	 * Returns the locator model. If not yet initialized,
	 * initialize the locator model.
	 *
	 * @return The locator model.
	 */
	public static ILocatorModel getModel() {
		// Access to the locator model must happen in the TCF dispatch thread
		if (fLocatorModel == null) {
			if (Protocol.isDispatchThread()) {
				initialize();
			} else {
				Protocol.invokeAndWait(new Runnable() {
					public void run() {
						initialize();
					}
				});
			}
		}
		return fLocatorModel;
	}

	/**
	 * Initialize the root node. Must be called within the TCF dispatch thread.
	 */
	protected static void initialize() {
		assert Protocol.isDispatchThread();

		fLocatorModel = new LocatorModel();
		// Register the model listener
		fLocatorModel.addListener(new ModelListener(fLocatorModel));
		// Start the scanner
		fLocatorModel.startScanner(5000, 120000);
	}

	/**
	 * Dispose the root node.
	 */
	public static void dispose() {
		if (fLocatorModel == null) return;

		// Access to the locator model must happen in the TCF dispatch thread
		if (Protocol.isDispatchThread()) {
			fLocatorModel.dispose();
		} else {
			Protocol.invokeAndWait(new Runnable() {
				public void run() {
					fLocatorModel.dispose();
				}
			});
		}

		fLocatorModel = null;
	}

}