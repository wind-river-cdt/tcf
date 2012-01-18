/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.processes.ui.internal.testers;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.tcf.te.tcf.processes.ui.model.ProcessTreeNode;

/**
 * The property tester for a process tree node.
 */
public class ProcessPropertyTester extends PropertyTester {
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if(receiver instanceof ProcessTreeNode) {
			ProcessTreeNode node = (ProcessTreeNode) receiver;
			if(property.equals("isSystemRoot")) { //$NON-NLS-1$
				return "ProcRootNode".equals(node.type); //$NON-NLS-1$
			}
		}
		return false;
	}
}
