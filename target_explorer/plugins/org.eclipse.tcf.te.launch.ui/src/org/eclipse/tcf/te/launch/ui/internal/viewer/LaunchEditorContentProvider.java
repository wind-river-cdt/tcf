/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.ui.internal.viewer;


/**
 * Launch tree content provider implementation.
 */
public class LaunchEditorContentProvider extends LaunchNavigatorContentProvider {

	/**
	 * Constructor.
	 */
	public LaunchEditorContentProvider() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.launch.ui.controls.LaunchNavigatorContentProvider#isRootNodeVisible()
	 */
	@Override
	protected boolean isRootNodeVisible() {
		return false;
	}
}