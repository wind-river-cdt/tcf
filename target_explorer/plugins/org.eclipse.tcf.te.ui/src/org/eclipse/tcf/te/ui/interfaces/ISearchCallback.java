/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.interfaces;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.TreePath;

public interface ISearchCallback {
	/**
	 * The callback invoked when the searching job is done, to process
	 * the path found.
	 * 
	 * @param status The searching resulting status.
	 * @param path The tree path found or null if no appropriate node is found.
	 */
	public void searchDone(IStatus status, TreePath treePath);
}
