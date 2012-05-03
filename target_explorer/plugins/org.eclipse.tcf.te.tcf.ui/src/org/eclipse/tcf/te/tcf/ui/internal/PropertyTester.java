/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.ui.internal;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.tcf.te.tcf.ui.handler.DeleteHandler;
import org.eclipse.tcf.te.tcf.ui.handler.OfflineCommandHandler;



/**
 * Property tester implementation.
 */
public class PropertyTester extends org.eclipse.core.expressions.PropertyTester {
	// Reference to the peer model delete handler (to determine "canDelete")
	private final DeleteHandler deleteHandler = new DeleteHandler();
	// Reference to the peer model offline handler (to determine "canMakeAvailableOffline")
	private final OfflineCommandHandler offlineHandler = new OfflineCommandHandler();

	/* (non-Javadoc)
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (receiver instanceof IStructuredSelection) {
			// Analyze the selection
			return testSelection((IStructuredSelection)receiver, property, args, expectedValue);
		}
		return false;
	}

	/**
	 * Test the specific selection properties.
	 *
	 * @param selection The selection. Must not be <code>null</code>.
	 * @param property The property to test.
	 * @param args The property arguments.
	 * @param expectedValue The expected value.
	 *
	 * @return <code>True</code> if the property to test has the expected value, <code>false</code>
	 *         otherwise.
	 */
    protected boolean testSelection(IStructuredSelection selection, String property, Object[] args, Object expectedValue) {
		Assert.isNotNull(selection);

		if ("canDelete".equals(property)) { //$NON-NLS-1$
			return deleteHandler.canDelete(selection);
		}

		if ("canMakeAvailableOffline".equals(property)) { //$NON-NLS-1$
			return offlineHandler.canMakeAvailableOffline(selection);
		}

		return false;
    }
}
