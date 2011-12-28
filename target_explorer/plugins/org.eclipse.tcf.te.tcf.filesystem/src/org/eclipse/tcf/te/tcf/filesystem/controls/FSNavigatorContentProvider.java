/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.controls;


/**
 * File system content provider for the common navigator of Target Explorer.
 */
public class FSNavigatorContentProvider extends FSTreeContentProvider {
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.trees.TreeContentProvider#isRootNodeVisible()
	 */
	@Override
	protected boolean isRootNodeVisible() {
		return true;
	}
}