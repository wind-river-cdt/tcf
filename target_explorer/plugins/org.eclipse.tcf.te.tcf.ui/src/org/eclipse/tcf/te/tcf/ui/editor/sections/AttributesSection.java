/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.ui.editor.sections;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.runtime.properties.PropertiesContainer;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.ui.nls.Messages;
import org.eclipse.tcf.te.tcf.ui.wizards.controls.PeerAttributesTablePart;
import org.eclipse.tcf.te.ui.forms.parts.AbstractSection;
import org.eclipse.tcf.te.ui.swt.SWTControlUtil;
import org.eclipse.tcf.te.ui.views.editor.AbstractEditorPage;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * Peer custom attributes section implementation.
 */
public class AttributesSection extends AbstractSection {
	// The section sub controls
	private PeerAttributesTablePart tablePart;

	// Reference to the original data object
	/* default */ IPeerModel od;
	// Reference to a copy of the original data
	/* default */ final IPropertiesContainer odc = new PropertiesContainer();

	/*
	 * The list of banned names for the table parts. Banned names are names the user is not allowed
	 * to add to the table.
	 */
	private static final String[] BANNED_NAMES = new String[] {
																IPeer.ATTR_ID, IPeer.ATTR_AGENT_ID, IPeer.ATTR_SERVICE_MANGER_ID,
																IPeer.ATTR_NAME, IPeer.ATTR_TRANSPORT_NAME, IPeer.ATTR_IP_HOST,
																IPeer.ATTR_IP_PORT, "PipeName" //$NON-NLS-1$
															  };

	/*
	 * The list of filtered attributes. This attributes are filtered from the data given to the
	 * table part on initialization. The attributes listed here are handled by other controls of the
	 * overview page.
	 */
	private static final String[] FILTERED_NAMES = new String [] {
																	IPeer.ATTR_ID, IPeer.ATTR_NAME,
																	IPeer.ATTR_TRANSPORT_NAME, IPeer.ATTR_IP_HOST, IPeer.ATTR_IP_PORT
																 };

	/**
	 * Constructor.
	 *
	 * @param form The parent managed form. Must not be <code>null</code>.
	 * @param parent The parent composite. Must not be <code>null</code>.
	 */
	public AttributesSection(IManagedForm form, Composite parent) {
		super(form, parent, Section.DESCRIPTION);
		createClient(getSection(), form.getToolkit());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.AbstractFormPart#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		if (tablePart != null) { tablePart.dispose(); tablePart = null; }
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.forms.parts.AbstractSection#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter(Class adapter) {
		if (PeerAttributesTablePart.class.equals(adapter)) {
			return tablePart;
		}
	    return super.getAdapter(adapter);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.forms.parts.AbstractSection#createClient(org.eclipse.ui.forms.widgets.Section, org.eclipse.ui.forms.widgets.FormToolkit)
	 */
	@Override
	protected void createClient(Section section, FormToolkit toolkit) {
		Assert.isNotNull(section);
		Assert.isNotNull(toolkit);

		// Configure the section
		section.setText(Messages.AttributesSection_title);
		section.setDescription(Messages.AttributesSection_description);

		section.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		// Create the section client
		Composite client = createClientContainer(section, 2, toolkit);
		Assert.isNotNull(client);
		section.setClient(client);

		tablePart = new PeerAttributesTablePart();
		tablePart.setMinSize(SWTControlUtil.convertWidthInCharsToPixels(client, 20), SWTControlUtil.convertHeightInCharsToPixels(client, 6));
		tablePart.setBannedNames(BANNED_NAMES);
		tablePart.createControl(client, SWT.SINGLE | SWT.FULL_SELECTION, 2, toolkit);

		// Adjust the control enablement
		updateEnablement();
	}

	/**
	 * Indicates whether the sections parent page has become the active in the editor.
	 *
	 * @param active <code>True</code> if the parent page should be visible, <code>false</code> otherwise.
	 */
	public void setActive(boolean active) {
		// If the parent page has become the active and it does not contain
		// unsaved data, than fill in the data from the selected node
		if (active) {
			// Leave everything unchanged if the page is in dirty state
			if (getManagedForm().getContainer() instanceof AbstractEditorPage
					&& !((AbstractEditorPage)getManagedForm().getContainer()).isDirty()) {
				Object node = ((AbstractEditorPage)getManagedForm().getContainer()).getEditorInputNode();
				if (node instanceof IPeerModel) {
					setupData((IPeerModel)node);
				}
			}
		}
	}

	/**
	 * Initialize the page widgets based of the data from the given peer node.
	 * <p>
	 * This method may called multiple times during the lifetime of the page and
	 * the given configuration node might be even <code>null</code>.
	 *
	 * @param node The peer node or <code>null</code>.
	 */
	public void setupData(final IPeerModel node) {
		// Store a reference to the original data
		od = node;
		// Clean the original data copy
		odc.clearProperties();

		// If no data is available, we are done
		if (node == null) return;

		// Thread access to the model is limited to the executors thread.
		// Copy the data over to the working copy to ease the access.
		Protocol.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				Map<String, String> properties = node.getPeer().getAttributes();
				for (String key : properties.keySet()) {
					odc.setProperty(key, properties.get(key));
				}
			}
		});

		// From here on, work with the working copy only!

		// Remove the filtered attributes from the original data and working copy in
		// order to get an exact base to determine the dirty state of the section.
		for (String filteredName : FILTERED_NAMES) {
			odc.setProperty(filteredName, null);
		}

		// Remove all "*.transient" properties
		for (String name : odc.getProperties().keySet()) {
			if (name.endsWith(".transient")) odc.setProperty(name, null); //$NON-NLS-1$
		}

		// Make a <String, String> map out of the remaining properties
		Map<String, String> attributes = new HashMap<String, String>();
		Map<String, Object> properties = odc.getProperties();
		for (String key : properties.keySet()) {
			attributes.put(key, properties.get(key).toString());
		}

		// Pass on to the table part
		if (tablePart != null) tablePart.setAttributes(attributes);

		// Adjust the control enablement
		updateEnablement();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.AbstractFormPart#commit(boolean)
	 */
	@Override
	public void commit(boolean onSave) {
		// Remember the current dirty state
		boolean needsSaving = isDirty();
		// Call the super implementation (resets the dirty state)
	    super.commit(onSave);

		// Nothing to do if not on save or saving is not needed
		if (!onSave || !needsSaving) return;
	}

	/**
	 * Called to signal that the data associated has been changed.
	 *
	 * @param e The event which triggered the invocation or <code>null</code>.
	 */
	public void dataChanged(TypedEvent e) {
		boolean isDirty = false;

		if (tablePart != null) {
			// Get the attributes from the table part
			Map<String, String> attributes = tablePart.getAttributes();
			Map<String, Object> properties = odc.getProperties();
			if (attributes.size() != properties.size()) {
				isDirty = true;
			} else {
				for (String key : attributes.keySet()) {
					if (!properties.containsKey(key) || !properties.get(key).equals(attributes.get(key))) {
						isDirty = true;
						break;
					}
				}
			}
		}

		// If dirty, mark the form part dirty.
		// Otherwise call refresh() to reset the dirty (and stale) flag
		markDirty(isDirty);
	}

	/**
	 * Updates the control enablement.
	 */
	protected void updateEnablement() {
		// Determine the input
		final Object input = getManagedForm().getInput();

		// Determine if the peer is a static peer
		final AtomicBoolean isStatic = new AtomicBoolean();
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				if (input instanceof IPeerModel) {
					String value = ((IPeerModel)input).getPeer().getAttributes().get("static.transient"); //$NON-NLS-1$
					isStatic.set(value != null && Boolean.parseBoolean(value.trim()));
				}
			}
		};
		if (Protocol.isDispatchThread()) runnable.run();
		else Protocol.invokeAndWait(runnable);

		if (tablePart != null) tablePart.setReadOnly(!isStatic.get());
	}
}
