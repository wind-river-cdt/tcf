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
/**
 * This interface is used to decorate a property change event to provide
 * if it should be scheduled at certain cycle.
 * 
 * @see CommonViewerListener
 */
public interface ISchedulable {
	
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
