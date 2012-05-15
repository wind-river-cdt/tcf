/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.ui.internal.testers;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;

/**
 * The property tester of a ColumnViewerEditorActivationEvent. 
 * The properties include "isEditorActivation" which calculates
 * if the event will trigger a cell editing action.
 */
public class EditorActivationEventPropertyTester extends PropertyTester {

	/**
	 * Create an instance.
	 */
	public EditorActivationEventPropertyTester() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		Assert.isTrue(receiver instanceof ColumnViewerEditorActivationEvent);
		ColumnViewerEditorActivationEvent event = (ColumnViewerEditorActivationEvent) receiver;
		if (property.equals("isEditorActivation")) { //$NON-NLS-1$
			return event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
		}
		return false;
	}
}
