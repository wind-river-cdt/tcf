/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.internal.handlers;

import org.eclipse.tcf.te.tcf.filesystem.internal.wizards.NewFileWizard;
import org.eclipse.ui.IWorkbenchWizard;

/**
 * The handler to create a new file node in the file system of Target Explorer.
 */
public class NewFileHandler extends NewNodeHandler {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.internal.handlers.NewNodeHandler#createWizard()
	 */
	@Override
	protected IWorkbenchWizard createWizard() {
		return new NewFileWizard();
	}
}
