/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.internal.wizards;

import org.eclipse.tcf.te.tcf.filesystem.internal.nls.Messages;
import org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSCreate;
import org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSCreateFolder;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;

/**
 * The wizard to create a new folder in the file system of Target Explorer.
 */
public class NewFolderWizard extends NewNodeWizard {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.internal.wizards.NewNodeWizard#createWizardPage()
	 */
	@Override
	protected NewNodeWizardPage createWizardPage() {
		return new NewFolderWizardPage();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.internal.wizards.NewNodeWizard#getCreateOp(org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode, java.lang.String)
	 */
	@Override
	protected FSCreate getCreateOp(FSTreeNode folder, String name) {
		return new FSCreateFolder(folder, name);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.internal.wizards.NewNodeWizard#getTitle()
	 */
	@Override
	protected String getTitle() {
		return Messages.NewFolderWizard_NewFolderWizardTitle;
	}
}