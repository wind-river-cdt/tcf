/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.ui.internal.wizards;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.Operation;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.filesystem.ui.internal.celleditor.FSCellValidator;
import org.eclipse.tcf.te.tcf.filesystem.ui.nls.Messages;
import org.eclipse.tcf.te.ui.controls.validator.Validator;

/**
 * The validator to validate the name of a file/folder in the file system of Target Explorer.
 * 
 * @see Validator
 */
public class NameValidator extends Validator {
	// The folder in which the new file/folder is to be created.
	NewNodeWizardPage wizard;

	/**
	 * Create a NameValidator with the folder in which the file/folder is created.
	 * 
	 * @param wizard The parent folder in which the file/folder is created.
	 */
	public NameValidator(NewNodeWizardPage wizard) {
		super(ATTR_MANDATORY);
		this.wizard = wizard;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.controls.validator.Validator#isValid(java.lang.String)
	 */
	@Override
	public boolean isValid(String newText) {
		FSTreeNode folder = wizard.getInputDir();
		if(folder == null) {
			setMessage(Messages.NameValidator_SpecifyFolder, IMessageProvider.INFORMATION);
			return false;
		}
		if (newText == null || newText.trim().length() == 0) {
			setMessage(Messages.FSRenamingAssistant_SpecifyNonEmptyName, IMessageProvider.ERROR);
			return false;
		}
		String text = newText.trim();
		if (hasChild(text)) {
			setMessage(Messages.FSRenamingAssistant_NameAlreadyExists, IMessageProvider.ERROR);
			return false;
		}
		String formatRegex = folder.isWindowsNode() ? FSCellValidator.WIN_FILENAME_REGEX : FSCellValidator.UNIX_FILENAME_REGEX;
		if (!text.matches(formatRegex)) {
			setMessage(folder.isWindowsNode() ? Messages.FSRenamingAssistant_WinIllegalCharacters : Messages.FSRenamingAssistant_UnixIllegalCharacters, IMessageProvider.ERROR);
			return false;
		}
		setMessage(null, IMessageProvider.NONE);
		return true;
	}

	/**
	 * To test if the folder has a child with the specified name.
	 * 
	 * @param name The name.
	 * @return true if it has a child with the name.
	 */
	private boolean hasChild(String name) {
		List<FSTreeNode> nodes = getChildren();
		for (FSTreeNode node : nodes) {
			if (node.isWindowsNode()) {
				if (node.name.equalsIgnoreCase(name)) return true;
			}
			else if (node.name.equals(name)) return true;
		}
		return false;
	}

	/**
	 * Get the folder's current children. If the children has not yet been loaded, then load it.
	 * 
	 * @return The current children of the folder.
	 */
	private List<FSTreeNode> getChildren() {
		final FSTreeNode folder = wizard.getInputDir();
		if (folder.childrenQueried) {
			return folder.unsafeGetChildren();
		}
		final List<FSTreeNode> result = new ArrayList<FSTreeNode>();
		SafeRunner.run(new SafeRunnable() {
			@Override
            public void handleException(Throwable e) {
				// Ignore exception
            }
			@Override
			public void run() throws Exception {
				result.addAll(new Operation().getChildren(folder));
			}
		});
		return result;
	}
}
