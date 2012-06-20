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

import org.eclipse.jface.viewers.TreeViewer;

/**
 * This interface is used to decorate a property change event to provide
 * if it should be scheduled at certain cycle.
 * 
 * @see CommonViewerListener
 */
public interface ISchedulableEvent {
	
	/**
	 * If this event should be applicable to the specified tree viewer.
	 * Used to determine if the event should be processed over the tree viewer.
	 * 
	 * @param viewer The tree viewer to be tested over.
	 * @return true if it is applicable or else false.
	 */
	public boolean isApplicable(TreeViewer viewer);
	
	/**
	 * Called when the event is added to the event queue.
	 */
	public void eventQueued();
	
	/**
	 * Called by CommonViewerListener to determine if this event should
	 * be scheduled to be processed in this cycle.
	 * 
	 * @return true if it should be processed in this cycle.
	 */
	public boolean isSchedulable();
}
