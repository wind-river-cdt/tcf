/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.wizards.pages;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tcf.te.ui.forms.CustomFormToolkit;
import org.eclipse.tcf.te.ui.forms.FormLayoutFactory;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

/**
 * Abstract wizard page using forms.
 */
public abstract class AbstractFormsWizardPage extends AbstractValidatingWizardPage {
	// The forms toolkit instance
	private CustomFormToolkit toolkit = null;
	// The managed form reference
	/* default */ ManagedForm mform = null;

	/**
	 * Internal managed form implementation to link the managed form with the parent
	 * wizard page.
	 */
	private static class WizardPageForm extends ManagedForm {

		/**
		 * Constructor.
		 *
		 * @param parentPage The parent wizard page. Must not be <code>null</code>.
		 * @param form The scrolled form. Must not be <code>null</code>.
		 */
		public WizardPageForm(AbstractFormsWizardPage parentPage, ScrolledForm form) {
			super(parentPage.getFormToolkit().getFormToolkit(), form);
			setContainer(parentPage);
		}

		/**
		 * Returns the parent wizard page.
		 *
		 * @return The parent wizard page.
		 */
		public AbstractFormsWizardPage getParentPage() {
			return (AbstractFormsWizardPage)getContainer();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.forms.ManagedForm#dirtyStateChanged()
		 */
		@Override
		public void dirtyStateChanged() {
			getParentPage().validate();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.forms.ManagedForm#staleStateChanged()
		 */
		@Override
		public void staleStateChanged() {
			getParentPage().validate();
		}
	}

	/**
	 * Constructor.
	 *
	 * @param pageName The page name. Must not be <code>null</code>.
	 */
	public AbstractFormsWizardPage(String pageName) {
		super(pageName);
	}

	/**
	 * Constructor.
	 *
	 * @param pageName The page name. Must not be <code>null</code>.
	 * @param title The wizard page title or <code>null</code>.
	 * @param titleImage The wizard page title image or <code>null</code>.
	 */
	public AbstractFormsWizardPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#dispose()
	 */
	@Override
	public void dispose() {
		if (mform != null) { mform.dispose(); mform = null; }
		if (toolkit != null) { toolkit.dispose(); toolkit = null; }
		super.dispose();
	}

	/**
	 * Creates the forms toolkit to use.
	 *
	 * @param display The display. Must not be <code>null</code>.
	 * @return The forms toolkit instance. Must never be <code>null</code>.
	 */
	protected CustomFormToolkit createFormToolkit(Display display) {
		Assert.isNotNull(display);
		return new CustomFormToolkit(new FormToolkit(display));
	}

	/**
	 * Returns the forms toolkit to use.
	 * <p>
	 * If {@link #createControl(Composite)} hasn't been called yet, or {@link #dispose()} has been
	 * called, the method will return <code>null</code>.
	 *
	 * @return The forms toolkit instance or <code>null</code>.
	 */
	public final CustomFormToolkit getFormToolkit() {
		return toolkit;
	}

	/**
	 * Returns the managed form hosted by this wizard page.
	 *
	 * @return The managed form or <code>null</code> if the form hasn't been created yet.
	 */
	public final IManagedForm getManagedForm() {
		return mform;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		Assert.isNotNull(parent);

		// Create the form toolkit
		toolkit = createFormToolkit(parent.getDisplay());
		Assert.isNotNull(toolkit);

		// Create the scrolled form which will hold the launch configuration tab controls
		ScrolledForm form = toolkit.createScrolledForm(parent, null, true);
		Assert.isNotNull(form);
		// The scrolled form is the main control for this tab
		setControl(form);

		// Create the managed form instance
		mform = new WizardPageForm(this, form);

		// Do not validate the page while creating the form content
		boolean changed = setValidationInProgress(true);
		// Create the form content
		createFormContent(mform);
		// Reset the validation in progress state
		if (changed) setValidationInProgress(false);

		// Adjust the font
		Dialog.applyDialogFont(form);

		// Validate the page for the first time
		validate();
	}

	/**
	 * Subclasses should override this method to create content in the form
	 * hosted in this launch configuration tab.
	 *
	 * @param managedForm The managed form hosted in this tab. Must not be <code>null</code>.
	 */
	protected void createFormContent(IManagedForm managedForm) {
		Assert.isNotNull(managedForm);

		// Configure the managed form
		configureManagedForm(managedForm);

		// Do create the content of the form now
		doCreateFormContent(managedForm.getForm().getBody(), toolkit);

		// Re-arrange the controls
		managedForm.reflow(true);
	}

	/**
	 * Configure the managed form to be ready for usage.
	 *
	 * @param managedForm The managed form. Must not be <code>null</code>.
	 */
	protected void configureManagedForm(IManagedForm managedForm) {
		Assert.isNotNull(managedForm);

		// Configure main layout
		Composite body = managedForm.getForm().getBody();
		body.setLayout(FormLayoutFactory.createFormGridLayout(false, 1));

		// Set context help id
		if (getContextHelpId() != null) {
			PlatformUI.getWorkbench().getHelpSystem().setHelp(managedForm.getForm(), getContextHelpId());
		}
	}

	/**
	 * Do create the managed form content.
	 *
	 * @param parent The parent composite. Must not be <code>null</code>
	 * @param toolkit The {@link CustomFormToolkit} instance. Must not be <code>null</code>.
	 */
	protected abstract void doCreateFormContent(Composite parent, CustomFormToolkit toolkit);
}
