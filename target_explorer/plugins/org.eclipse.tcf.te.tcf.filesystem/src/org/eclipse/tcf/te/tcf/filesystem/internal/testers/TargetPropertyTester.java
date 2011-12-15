/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.internal.testers;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;

/**
 * The property tester to test if the target OS is a Windows OS.
 */
public class TargetPropertyTester extends PropertyTester {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if(receiver instanceof IPeerModel) {
			final IPeerModel peerModel = (IPeerModel) receiver;
			if(property.equals("isWindows")) { //$NON-NLS-1$
				final String[] osName = new String[1];
				if(Protocol.isDispatchThread()) {
					osName[0] = peerModel.getStringProperty("OSName"); //$NON-NLS-1$
				} else {
					Protocol.invokeAndWait(new Runnable(){
						@Override
                        public void run() {
							osName[0] = peerModel.getStringProperty("OSName"); //$NON-NLS-1$
                        }});
				}
				return osName[0] == null ? false : (osName[0].startsWith("Windows")); //$NON-NLS-1$
			}
		}
		return false;
	}
}
