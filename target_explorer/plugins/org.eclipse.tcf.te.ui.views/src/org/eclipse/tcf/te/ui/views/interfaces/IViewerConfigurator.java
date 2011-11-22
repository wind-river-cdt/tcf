/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * William Chen (Wind River) - [361324] Add more file operations in the file 
 * 												system of Target Explorer.
 *******************************************************************************/
package org.eclipse.tcf.te.ui.views.interfaces;

import org.eclipse.ui.navigator.CommonViewer;

/**
 * An interface to configure the common viewer of Target Explorer in an abstract way.
 * <p>
 * This interface should be implemented by classes that wish to configure the common viewer when
 * Target Explorer is created.
 */
public interface IViewerConfigurator {
	/**
	 * Configure the common viewer of Target Explorer.
	 * 
	 * @param viewer The common viewer of Target Explorer.
	 */
	void configure(CommonViewer viewer);
}
