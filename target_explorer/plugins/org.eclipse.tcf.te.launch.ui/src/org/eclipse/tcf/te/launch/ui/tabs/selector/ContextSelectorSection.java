/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.ui.tabs.selector;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tcf.te.launch.ui.nls.Messages;
import org.eclipse.tcf.te.ui.forms.parts.AbstractSection;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * Context selector section implementation.
 */
public class ContextSelectorSection extends AbstractSection {
	// Reference to the section sub controls
	private ContextSelectorControl selector;

	/**
	 * Constructor.
	 *
	 * @param form The parent managed form. Must not be <code>null</code>.
	 * @param parent The parent composite. Must not be <code>null</code>.
	 */
	public ContextSelectorSection(IManagedForm form, Composite parent) {
		super(form, parent, Section.DESCRIPTION | ExpandableComposite.CLIENT_INDENT);
		createClient(getSection(), form.getToolkit());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.forms.parts.AbstractSection#createClient(org.eclipse.ui.forms.widgets.Section, org.eclipse.ui.forms.widgets.FormToolkit)
	 */
	@Override
	protected void createClient(Section section, FormToolkit toolkit) {
		Assert.isNotNull(section);
		Assert.isNotNull(toolkit);

		// Configure the section
		section.setText(Messages.ContextSelectorSection_title);
		section.setDescription(Messages.ContextSelectorSection_description);

		section.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		// Create the section client
		Composite client = createClientContainer(section, 1, toolkit);
		Assert.isNotNull(client);
		section.setClient(client);

		selector = new ContextSelectorControl(null);
		selector.setFormToolkit(toolkit);
		selector.setupPanel(client);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.AbstractFormPart#dispose()
	 */
	@Override
	public void dispose() {
		if (selector != null) { selector.dispose(); selector = null; }
	    super.dispose();
	}
}
