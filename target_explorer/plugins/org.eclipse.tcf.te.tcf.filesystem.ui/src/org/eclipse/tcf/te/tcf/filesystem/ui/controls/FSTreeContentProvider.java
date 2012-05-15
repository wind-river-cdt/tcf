/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.ui.controls;

/**
 * File system tree content provider implementation.
 */
public class FSTreeContentProvider extends FSNavigatorContentProvider {
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.controls.FSNavigatorContentProvider#isRootNodeVisible()
	 */
	@Override
    protected boolean isRootNodeVisible() {
	    return false;
    }
}