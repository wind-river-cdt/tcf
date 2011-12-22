/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.processes.ui.interfaces;

import org.eclipse.tcf.te.tcf.processes.ui.model.ProcessTreeNode;

/**
 * An INodeStateListener is a listener interface. Classes that implement this 
 * interface serve as a listener processing the event that a node state has changed.
 *
 */
public interface INodeStateListener {
	/**
	 * Fired when the state of the specified ProcessTreeNode has changed.
	 *  
	 * @param node The ProcessTreeNode whose state has changed.
	 */
	void stateChanged(ProcessTreeNode node);
}
