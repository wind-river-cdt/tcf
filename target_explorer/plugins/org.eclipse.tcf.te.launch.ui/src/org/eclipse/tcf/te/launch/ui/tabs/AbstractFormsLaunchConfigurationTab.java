/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.ui.tabs;

import org.eclipse.core.runtime.Assert;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tcf.te.launch.ui.interfaces.ILaunchConfigurationTabFormPart;
import org.eclipse.tcf.te.ui.forms.CustomFormToolkit;
import org.eclipse.tcf.te.ui.forms.FormLayoutFactory;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

/**
 * Abstract form based launch configuration tab implementation.
 */
public abstract class AbstractFormsLaunchConfigurationTab extends AbstractLaunchConfigurationTab {
	// The forms toolkit instance
	private CustomFormToolkit toolkit = null;
	// The managed form reference
	/* default */ ManagedForm mform = null;

	/**
	 * Internal managed form implementation to link the managed form with the parent
	 * launch configuration tab.
	 */
	private static class TabForm extends ManagedForm {

		/**
		 * Constructor.
		 *
		 * @param parentTab The parent launch configuration tab. Must not be <code>null</code>.
		 * @param form The scrolled form. Must not be <code>null</code>.
		 */
		public TabForm(AbstractFormsLaunchConfigurationTab parentTab, ScrolledForm form) {
			super(parentTab.getFormToolkit().getFormToolkit(), form);
			setContainer(parentTab);
		}

		/**
		 * Returns the parent launch configuration tab.
		 *
		 * @return The parent launch configuration tab.
		 */
		public AbstractFormsLaunchConfigurationTab getParentTab() {
			return (AbstractFormsLaunchConfigurationTab)getContainer();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.forms.ManagedForm#dirtyStateChanged()
		 */
		@SuppressWarnings("synthetic-access")
		@Override
		public void dirtyStateChanged() {
			getParentTab().updateLaunchConfigurationDialog();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.forms.ManagedForm#staleStateChanged()
		 */
		@SuppressWarnings("synthetic-access")
		@Override
		public void staleStateChanged() {
			getParentTab().updateLaunchConfigurationDialog();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#dispose()
	 */
	@Override
	public void dispose() {
		if (mform != null) {mform.dispose(); mform = null; }
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
	 * If {@link #createControl(Composite)} hasn't been called yet, or
	 * {@link #dispose()} has been called, the method will return
	 * <code>null</code>.
	 *
	 * @return The forms toolkit instance or <code>null</code>.
	 */
	public final CustomFormToolkit getFormToolkit() {
		return toolkit;
	}

	/**
	 * Returns the managed form hosted by this launch configuration tab.
	 *
	 * @return The managed form or <code>null</code> if the form hasn't been created yet.
	 */
	public IManagedForm getManagedForm() {
		return mform;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public final void createControl(Composite parent) {
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
		mform = new TabForm(this, form);

		// Create the form content
		BusyIndicator.showWhile(parent.getDisplay(), new Runnable() {
			@Override
			public void run() {
				createFormContent(mform);
			}
		});
	}

	/**
	 * Subclasses should override this method to create content in the form
	 * hosted in this launch configuration tab.
	 *
	 * @param managedForm The managed form hosted in this tab. Must not be <code>null</code>.
	 */
	public void createFormContent(IManagedForm managedForm) {
		Assert.isNotNull(managedForm);

		// Configure the managed form
		configureManagedForm(managedForm);

		// Do create the content of the form now
		doCreateFormContent(managedForm.getForm().getBody(), getFormToolkit());

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
	 * Returns the context help id to associate with the page form.
	 *
	 * @return The context help id.
	 */
	protected String getContextHelpId() {
		return null;
	}

	/**
	 * Do create the managed form content.
	 *
	 * @param parent The parent composite. Must not be <code>null</code>
	 * @param toolkit The {@link CustomFormToolkit} instance. Must not be <code>null</code>.
	 */
	protected abstract void doCreateFormContent(Composite parent, CustomFormToolkit toolkit);

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		if (mform != null) {
			// Set the launch configuration as input element to make
			// it accessible to the form parts outside the life cycle methods.
			mform.setInput(configuration);

			// Get all registered form parts
			IFormPart[] parts = mform.getParts();
			for (IFormPart part : parts) {
				if (part instanceof ILaunchConfigurationTabFormPart) {
					((ILaunchConfigurationTabFormPart)part).initializeFrom(configuration);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		if (mform != null) {
			// Get all registered form parts
			IFormPart[] parts = mform.getParts();
			for (IFormPart part : parts) {
				if (part instanceof ILaunchConfigurationTabFormPart) {
					((ILaunchConfigurationTabFormPart)part).performApply(configuration);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public boolean isValid(ILaunchConfiguration configuration) {
		boolean valid = super.isValid(configuration);

		String errorMessage = null;

		if (mform != null) {
			// Get all registered form parts
			IFormPart[] parts = mform.getParts();
			for (IFormPart part : parts) {
				if (part instanceof ILaunchConfigurationTabFormPart) {
					valid &= ((ILaunchConfigurationTabFormPart)part).isValid(configuration);
					if (!valid) {
						if (part instanceof IMessageProvider && errorMessage == null) {
							errorMessage = ((IMessageProvider)part).getMessage();
						}
					}
				}
			}
		}

		setErrorMessage(errorMessage);

		return valid;
	}
}
