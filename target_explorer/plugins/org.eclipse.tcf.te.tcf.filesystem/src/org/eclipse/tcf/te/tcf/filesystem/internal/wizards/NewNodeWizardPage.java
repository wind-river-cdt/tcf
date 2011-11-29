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

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tcf.te.tcf.filesystem.internal.help.IContextHelpIds;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.tcf.te.ui.controls.BaseEditBrowseTextControl;
import org.eclipse.tcf.te.ui.forms.FormLayoutFactory;
import org.eclipse.tcf.te.ui.wizards.pages.AbstractValidatableWizardPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * The base wizard page class to create a new file/folder in the file system of Target Explorer.
 */
public abstract class NewNodeWizardPage extends AbstractValidatableWizardPage {
	// The form toolkit to create the content of the wizard page.
	private FormToolkit toolkit;
	// The name control for the user to enter the new name.
	private BaseEditBrowseTextControl nameControl;
	// The folder in which the new node is created.
	protected FSTreeNode folder;

	/**
	 * Create an instance page with the specified page name.
	 * 
	 * @param pageName The page name.
	 */
	public NewNodeWizardPage(String pageName) {
		super(pageName);
	}

	/**
	 * Create an instance page with the specified page name and a folder, in which the new node is
	 * created.
	 * 
	 * @param pageName The page name.
	 * @param folder the folder in which the new node is created.
	 */
	public NewNodeWizardPage(String pageName, FSTreeNode folder) {
		this(pageName);
		this.folder = folder;
	}

	/**
	 * Get the page's title.
	 * 
	 * @return The page's title.
	 */
	protected abstract String getPageTitle();

	/**
	 * Get the page's description.
	 * 
	 * @return The page's description.
	 */
	protected abstract String getPageDescription();

	/**
	 * Get the label of the text field to enter the new name.
	 * 
	 * @return The label of the text field to enter the new name.
	 */
	protected abstract String getNameFieldLabel();

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		// Setup title and description
		setTitle(getPageTitle());
		setDescription(getPageDescription());

		// Create the forms toolkit
		toolkit = new FormToolkit(parent.getDisplay());

		// Create the main panel
		Composite mainPanel = toolkit.createComposite(parent);
		mainPanel.setLayout(FormLayoutFactory.createClearGridLayout(false, 1));
		mainPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		mainPanel.setBackground(parent.getBackground());

		setControl(mainPanel);

		// Setup the help
		PlatformUI.getWorkbench().getHelpSystem()
		                .setHelp(mainPanel, IContextHelpIds.FS_NEW_FILE_WIZARD_PAGE);

		// Do not validate the page while creating the controls
		boolean changed = setValidationInProgress(true);
		// Create the main panel sub controls
		createMainPanelControls(mainPanel);
		// Reset the validation in progress state
		if (changed) setValidationInProgress(false);

		// Adjust the font
		Dialog.applyDialogFont(mainPanel);

		// Validate the page for the first time
		validatePage();
	}

	/**
	 * Create the main panel of this wizard page.
	 * 
	 * @param parent The parent composite in which the page is created.
	 */
	private void createMainPanelControls(Composite parent) {
		Assert.isNotNull(parent);

		// Create the client composite
		Composite client = toolkit.createComposite(parent);
		client.setLayout(FormLayoutFactory.createSectionClientGridLayout(false, 2));
		client.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		client.setBackground(parent.getBackground());

		nameControl = new BaseEditBrowseTextControl(this);
		nameControl.setIsGroup(false);
		nameControl.setHasHistory(false);
		nameControl.setHideBrowseButton(true);
		nameControl.setEditFieldLabel(getNameFieldLabel());
		nameControl.setAdjustBackgroundColor(true);
		nameControl.setFormToolkit(toolkit);
		nameControl.setParentControlIsInnerPanel(true);
		nameControl.setupPanel(client);
		nameControl.setEditFieldValidator(new NameValidator(folder));
		nameControl.getEditFieldControl().setFocus();

		// restore the widget values from the history
		restoreWidgetValues();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.tcf.te.ui.controls.wizards.pages.AbstractValidatableWizardPage#validatePage()
	 */
	@Override
	public void validatePage() {
		super.validatePage();
		if (!isPageComplete()) return;

		if (isValidationInProgress()) return;
		setValidationInProgress(true);

		boolean valid = nameControl.isValid();
		setMessage(nameControl.getMessage(), nameControl.getMessageType());
		setPageComplete(valid);
		setValidationInProgress(false);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#dispose()
	 */
	@Override
	public void dispose() {
		if (nameControl != null) {
			nameControl.dispose();
			nameControl = null;
		}
		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.wizards.pages.AbstractWizardPage#saveWidgetValues()
	 */
	@Override
	public void saveWidgetValues() {
		IDialogSettings settings = getDialogSettings();
		if (settings != null) {
			if (nameControl != null) nameControl.saveWidgetValues(settings, null);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.wizards.pages.AbstractWizardPage#restoreWidgetValues()
	 */
	@Override
	public void restoreWidgetValues() {
		IDialogSettings settings = getDialogSettings();
		if (settings != null) {
			if (nameControl != null) {
				nameControl.restoreWidgetValues(settings, null);
				Control control = nameControl.getEditFieldControl();
				if (control instanceof Text) {
					Text text = (Text) control;
					text.selectAll();
				}
			}
		}
	}

	/**
	 * Get the entered name of this node.
	 * 
	 * @return The entered name of this node.
	 */
	public String getNodeName() {
		return nameControl.getEditFieldControlTextForValidation();
	}
}
