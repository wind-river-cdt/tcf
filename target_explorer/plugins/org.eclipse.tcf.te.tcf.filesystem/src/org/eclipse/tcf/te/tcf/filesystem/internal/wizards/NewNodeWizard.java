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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.tcf.te.ui.wizards.AbstractWizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * The base wizard class to create a new file/folder in the file system of Target Explorer.
 */
public abstract class NewNodeWizard extends AbstractWizard implements INewWizard {
	// The folder in which the new node is created.
	private FSTreeNode folder;
	// The wizard page used to create the new node.
	private NewNodeWizardPage page;

	/**
	 * Create an instance.
	 */
	public NewNodeWizard() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
	 * org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// Set the window title
		setWindowTitle(getTitle());
		if (!selection.isEmpty()) {
			Object element = selection.getFirstElement();
			if (element instanceof FSTreeNode) {
				folder = (FSTreeNode) element;
				if (folder.isFile()) {
					folder = folder.parent;
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	@Override
	public void addPages() {
		if (folder != null) {
			addPage(page = createWizardPage(folder));
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		if (page != null) {
			// Save the value so that next time it is used as the default input.
			page.saveWidgetValues();
			// Get the new name and create the node.
			String name = page.getNodeName();
			FSCreate create = getCreateOp(folder, name);
			boolean doit = create.doit();
			if (!doit) {
				// The the error message generated during creation.
				page.setMessage(create.getError(), IMessageProvider.ERROR);
				return false;
			}
		}
		return true;
	}

	/**
	 * Create a wizard page to create a new node in the specified folder.
	 * 
	 * @param folder The parent folder in which the new node is created.
	 * @return The new wizard page.
	 */
	protected abstract NewNodeWizardPage createWizardPage(FSTreeNode folder);

	/**
	 * Create a Create operation instance using the specified folder and the new name.
	 * 
	 * @param folder The folder in which the new node is created.
	 * @param name The name of the new node.
	 * @return a FSCreate instance to do the creation.
	 */
	protected abstract FSCreate getCreateOp(FSTreeNode folder, String name);

	/**
	 * The wizard's title to be used.
	 * 
	 * @return The wizard's title to be used.
	 */
	protected abstract String getTitle();
}
