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
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.processes.ui.model.ProcessModel;
import org.eclipse.ui.IEditorInput;

/**
 * The property tester to test the editor input of an editor.
 */
public class EditorInputPropertyTester extends PropertyTester {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (receiver instanceof IEditorInput) {
			IEditorInput editorInput = (IEditorInput) receiver;
			IPeerModel peerModel = (IPeerModel) editorInput.getAdapter(IPeerModel.class);
			if (peerModel != null && property.equals("isRefreshStopped")) { //$NON-NLS-1$
				ProcessModel processModel = ProcessModel.getProcessModel(peerModel);
				return processModel.isRefreshStopped();
			}
		}
		return false;
	}
}
