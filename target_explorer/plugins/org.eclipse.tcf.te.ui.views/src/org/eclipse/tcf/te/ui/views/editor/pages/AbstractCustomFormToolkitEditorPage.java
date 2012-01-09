/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.views.editor.pages;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tcf.te.ui.forms.CustomFormToolkit;
import org.eclipse.tcf.te.ui.forms.FormLayoutFactory;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.menus.IMenuService;

/**
 * Abstract details editor page implementation managing
 *                  an custom form toolkit instance.
 */
public abstract class AbstractCustomFormToolkitEditorPage extends AbstractEditorPage {
	// Reference to the form toolkit instance
	private CustomFormToolkit toolkit = null;

	/**
	 * Returns the custom form toolkit instance.
	 *
	 * @return The custom form toolkit instance or <code>null</code>.
	 */
	protected final CustomFormToolkit getFormToolkit() {
		return toolkit;
	}

	/**
	 * Sets the custom form toolkit instance.
	 *
	 * @param toolkit The custom form toolkit instance or <code>null</code>.
	 */
	protected final void setFormToolkit(CustomFormToolkit toolkit) {
		this.toolkit = toolkit;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.editor.FormPage#dispose()
	 */
	@Override
	public void dispose() {
		if (toolkit != null) { toolkit.dispose(); toolkit = null; }
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.editor.FormPage#createFormContent(org.eclipse.ui.forms.IManagedForm)
	 */
	@Override
	protected void createFormContent(IManagedForm managedForm) {
		super.createFormContent(managedForm);

		Assert.isNotNull(managedForm);

		// Create the toolkit instance
		toolkit = new CustomFormToolkit(managedForm.getToolkit());

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

		// Decorate the form header
		getFormToolkit().getFormToolkit().decorateFormHeading(managedForm.getForm().getForm());
		// And set the header text and image
		if (getFormTitle() != null) managedForm.getForm().getForm().setText(getFormTitle());
		managedForm.getForm().getForm().setImage(getFormImage());

		// Add the toolbar items which will appear in the form header
		IToolBarManager manager = managedForm.getForm().getForm().getToolBarManager();
		// Create fixed toolbar contribution items
		createToolbarContributionItems(manager);
		// Add the default "additions" separator
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		// Get the menu service and populate contributed toolbar actions
		IMenuService service = (IMenuService) getSite().getService(IMenuService.class);
		if (service != null) {
			service.populateContributionManager((ToolBarManager)manager, "toolbar:" + getId()); //$NON-NLS-1$
		}
		// Trigger an update of the toolbar widget
		manager.update(true);
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
	 * Returns the form title to set to the top form header.
	 *
	 * @return The form title.
	 */
	protected String getFormTitle() {
		return null;
	}

	/**
	 * Returns the image to be set to the top form header.
	 *
	 * @return The image or <code>null</code> to use no image.
	 */
	protected Image getFormImage() {
		return null;
	}

	/**
	 * Create the toolbar contribution items.
	 *
	 * @param manager The toolbar manager. Must not be <code>null</code>.
	 */
	protected void createToolbarContributionItems(IToolBarManager manager) {
		Assert.isNotNull(manager);
	}

	/**
	 * Do create the managed form content.
	 *
	 * @param parent The parent composite. Must not be <code>null</code>
	 * @param toolkit The {@link CustomFormToolkit} instance. Must not be <code>null</code>.
	 */
	protected abstract void doCreateFormContent(Composite parent, CustomFormToolkit toolkit);
}
