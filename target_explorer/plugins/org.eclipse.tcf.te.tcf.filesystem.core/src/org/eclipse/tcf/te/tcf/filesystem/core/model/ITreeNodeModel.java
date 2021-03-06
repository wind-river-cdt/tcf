/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.core.model;

/**
 * The model for holding the root of the whole trees. 
 */
public interface ITreeNodeModel {
	/**
	 * Get the root node of the whole tree.
	 * 
	 * @return The root node.
	 */
	AbstractTreeNode getRoot();
}
