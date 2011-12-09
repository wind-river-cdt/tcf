/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.trees;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;

/**
 * The base class of the tree viewer column's label provider.  
 */
public class TreeColumnLabelProvider extends LabelProvider {
	// The viewer of this tree column.
	protected TreeViewer viewer;

	/**
	 * Set the tree viewer of this column. 
	 * 
	 * @param viewer The new tree viewer.
	 */
	public void setViewer(TreeViewer viewer) {
		this.viewer = viewer;
	}
}
