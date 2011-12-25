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

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.filesystem.nls.Messages;
import org.eclipse.tcf.te.ui.controls.validator.Validator;

/**
 * The validator to validate the path of the parent directory in the new file/folder wizard
 * page is valid. It is only when it is not empty and it exists in the target peer.
 * 
 * @see Validator
 */
public class FolderValidator extends Validator {
	// The wizard page to create the new node.
	private NewNodeWizardPage page;
	
	/**
	 * Create a folder validator of the specified wizard page.
	 * 
	 * @param page The wizard page to create the new file/folder.
	 */
	public FolderValidator(NewNodeWizardPage page) {
	    super(ATTR_MANDATORY);
	    this.page = page;
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.controls.validator.Validator#isValid(java.lang.String)
	 */
	@Override
	public boolean isValid(String newText) {
		if (newText == null || newText.trim().length() == 0) {
			setMessage(Messages.FolderValidator_SpecifyFolder, IMessageProvider.ERROR);
			return false;
		}
		FSTreeNode folder = page.getInputDir();
		if (folder == null) {
			setMessage(NLS.bind(Messages.FolderValidator_DirNotExist, newText), IMessageProvider.ERROR);
			return false;
		}
		if (!folder.isWritable()) {
			setMessage(NLS.bind(Messages.FolderValidator_NotWritable, newText), IMessageProvider.ERROR);
			return false;
		}
		setMessage(null, IMessageProvider.NONE);
		return true;
	}
}
