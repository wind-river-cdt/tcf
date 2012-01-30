/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.ui.navigator;

import java.util.EventObject;

import org.eclipse.tcf.te.runtime.events.ChangeEvent;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.ui.views.events.AbstractEventListener;

/**
 * UI event listener updating the main view.
 */
public class EventListener extends AbstractEventListener {

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.interfaces.events.IEventListener#eventFired(java.util.EventObject)
	 */
	@Override
	public void eventFired(EventObject event) {
		if (event instanceof ChangeEvent) {
			final ChangeEvent changeEvent = (ChangeEvent)event;
			final Object source = changeEvent.getSource();

			// Property changes for individual peer model nodes refreshes the node only
			if (source instanceof IPeerModel) {
				if ("expanded".equals(changeEvent.getEventId())) { //$NON-NLS-1$
					// Expansion state of the node changed.
					boolean expanded = ((Boolean)changeEvent.getNewValue()).booleanValue();
					// Update the nodes expansion state
					getViewer().setExpandedState(source, expanded);
				} else {
					// Refresh the node
					refresh(source, false);
				}
			}
		}
	}

}
