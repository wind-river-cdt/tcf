/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.ui.internal.viewer;


/**
 * Launches content provider for the common navigator of Target Explorer.
 */
public class LaunchFavoritesContentProvider extends LaunchNavigatorContentProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.ui.internal.viewer.LaunchNavigatorContentProvider#isRootNodeVisible()
	 */
	@Override
	protected boolean isRootNodeVisible() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.ui.internal.viewer.LaunchNavigatorContentProvider#isTypeNodeVisible()
	 */
	@Override
	protected boolean isTypeNodeVisible() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.ui.internal.viewer.LaunchNavigatorContentProvider#isEmptyTypeNodeVisible()
	 */
	@Override
	protected boolean isEmptyTypeNodeVisible() {
		return false;
	}
}
