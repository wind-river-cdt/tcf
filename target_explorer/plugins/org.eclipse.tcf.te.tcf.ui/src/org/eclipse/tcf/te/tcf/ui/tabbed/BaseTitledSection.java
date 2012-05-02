/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.ui.tabbed;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tcf.te.core.interfaces.IViewerInput;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModelProvider;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * The base section that displays a title in a title bar.
 */
public abstract class BaseTitledSection extends AbstractPropertySection implements PropertyChangeListener {

	// The main composite used to create the section content.
	protected Composite composite;

	protected IViewerInput viewerInput;

	// The input node.
	protected IPeerModelProvider provider;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.tabbed.AbstractPropertySection#setInput(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
    public void setInput(IWorkbenchPart part, ISelection selection) {
        super.setInput(part, selection);
		if (this.viewerInput != null) {
			this.viewerInput.removePropertyChangeListener(this);
		}
        Assert.isTrue(selection instanceof IStructuredSelection);
        Object input = ((IStructuredSelection) selection).getFirstElement();
		if (input instanceof IPeerModelProvider) {
			this.provider = (IPeerModelProvider) input;
			IPeerModel peerNode = this.provider.getPeerModel();
			this.viewerInput = (IViewerInput) peerNode.getAdapter(IViewerInput.class);
			if (this.viewerInput != null) {
				this.viewerInput.addPropertyChangeListener(this);
			}
		} else {
			this.provider = null;
			this.viewerInput = null;
		}
		updateInput(provider);
    }

	/**
	 * Update the input node.
	 *
	 * @param input The input node.
	 */
	protected void updateInput(IPeerModelProvider input) {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.tabbed.AbstractPropertySection#aboutToBeHidden()
	 */
	@Override
    public void aboutToBeHidden() {
		if(this.viewerInput != null) {
			this.viewerInput.removePropertyChangeListener(this);
		}
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.tabbed.AbstractPropertySection#createControls(org.eclipse.swt.widgets.Composite, org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage)
	 */
	@Override
    public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);
		parent.setLayout(new FormLayout());

		Section section = getWidgetFactory().createSection(parent, ExpandableComposite.TITLE_BAR);
		section.setText(getText());
		FormData data = new FormData();
		data.left = new FormAttachment(0, ITabbedPropertyConstants.HMARGIN);
		data.right = new FormAttachment(100, -ITabbedPropertyConstants.HMARGIN);
		data.top = new FormAttachment(0, 2 * ITabbedPropertyConstants.VMARGIN);
		section.setLayoutData(data);

		composite = getWidgetFactory().createComposite(parent);
		FormLayout layout = new FormLayout();
		layout.spacing = ITabbedPropertyConstants.HMARGIN;
		composite.setLayout(layout);

		data = new FormData();
		data.left = new FormAttachment(0, 2 * ITabbedPropertyConstants.HMARGIN);
		data.right = new FormAttachment(100, -2 * ITabbedPropertyConstants.HMARGIN);
		data.top = new FormAttachment(section, ITabbedPropertyConstants.VSPACE);
		data.bottom = new FormAttachment(100, 0);
		composite.setLayoutData(data);
	}

	/**
	 * Create a label for the control using the specified text.
	 *
	 * @param control The control for which the label is created.
	 * @param text The label text.
	 */
	protected void createLabel(Control control, String text) {
		CLabel nameLabel = getWidgetFactory().createCLabel(composite, text);
		FormData data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(control, -ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(control, 0, SWT.CENTER);
		nameLabel.setLayoutData(data);
	}

	/**
	 * Create a text field and a label with the specified label
	 * relative to the specified control.
	 *
	 * @param control The control relative to.
	 * @param label The text of the label.
	 * @return The new text created.
	 */
	protected Text createTextField(Control control, String label) {
		Text text = createText(control);
		createLabel(text, label);
		return text;
	}

	/**
	 * Create a wrap text field and a label with the specified label
	 * relative to the specified control.
	 *
	 * @param control The control relative to.
	 * @param label The text of the label.
	 * @return The new wrap text created.
	 */
	protected Text createWrapTextField(Control control, String label) {
		Text text = createWrapText(control);
		createLabel(text, label);
		return text;
	}

	/**
	 * Create a text field relative to the specified control.
	 *
	 * @param control The control to layout the new text field.
	 * @return The new text field created.
	 */
	private Text createText(Control control) {
		Text text = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
		FormData data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		if (control == null) {
			data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
		}
		else {
			data.top = new FormAttachment(control, ITabbedPropertyConstants.VSPACE);
		}
		text.setLayoutData(data);
		text.setEditable(false);
		return text;
	}

	/**
	 * Create a wrap text field relative to the specified control.
	 *
	 * @param control The control to layout the new wrap text field.
	 * @return The new wrap text field created.
	 */
	private Text createWrapText(Control control) {
		Text text = getWidgetFactory().createText(composite, "", SWT.WRAP); //$NON-NLS-1$
		FormData data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		if (control == null) {
			data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
		}
		else {
			data.top = new FormAttachment(control, ITabbedPropertyConstants.VSPACE);
		}
		data.width = 200;
		text.setLayoutData(data);
		text.setEditable(false);
		return text;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.tabbed.AbstractPropertySection#refresh()
	 */
	@Override
    public void refresh() {
		if (composite != null) {
			composite.layout();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	@Override
    public void propertyChange(PropertyChangeEvent event) {
		if (event.getSource() == provider) {
			updateInput(provider);
			Display display = getPart().getSite().getShell().getDisplay();
			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					refresh();
				}
			});
		}
    }

	/**
	 * Get the text which is used as the title in the title bar of the section.
	 *
	 * @return A text string representing the section.
	 */
	protected abstract String getText();
}