/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.ui.views.monitor.events;

import java.util.EventObject;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.te.tcf.log.core.events.MonitorEvent;
import org.eclipse.tcf.te.tcf.ui.views.monitor.console.Console;
import org.eclipse.tcf.te.tcf.ui.views.monitor.console.Factory;
import org.eclipse.tcf.te.ui.events.AbstractEventListener;

/**
 * Communication monitor console event listener
 */
public class EventListener extends AbstractEventListener {

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.interfaces.events.IEventListener#eventFired(java.util.EventObject)
	 */
	@Override
	public void eventFired(EventObject event) {
		if (event instanceof MonitorEvent) {
			MonitorEvent monitorEvent = (MonitorEvent)event;

			// Get the event type and message
			MonitorEvent.Type type = monitorEvent.getType();
			MonitorEvent.Message message = monitorEvent.getMessage();

			switch (type) {
				case CLOSE:
					// Channel close messages are logged only if there is an error
					if (message != null && message.text != null && !message.text.contains("(error=null)")) { //$NON-NLS-1$
						// Get the console
						Console console = Factory.getConsole((IPeer)monitorEvent.getSource(), true);
						Assert.isNotNull(console);
						// Message type 'R' is an unknown type and will lead to print the
						// message text using the error color.
						console.appendMessage('R', message.text);
					}
					break;
				case ACTIVITY:
					if (message != null) {
						// Get the console
						Console console = Factory.getConsole((IPeer)monitorEvent.getSource(), true);
						Assert.isNotNull(console);
						console.appendMessage(message.type, message.text);
					}
					break;
				case OPEN:
				default:
					break;
			}
		}
	}

}
