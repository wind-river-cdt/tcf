/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.controls.wire;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tcf.te.core.nodes.interfaces.wire.IWireTypeNetwork;
import org.eclipse.tcf.te.core.nodes.interfaces.wire.IWireTypeSerial;
import org.eclipse.tcf.te.ui.controls.BaseEditBrowseTextControl;
import org.eclipse.tcf.te.ui.controls.nls.Messages;
import org.eclipse.tcf.te.ui.swt.SWTControlUtil;

/**
 * Wire type control implementation.
 */
public class WireTypeControl extends BaseEditBrowseTextControl {

	private final static String[] WIRE_TYPES = new String[] {
										IWireTypeNetwork.PROPERTY_CONTAINER_NAME,
										IWireTypeSerial.PROPERTY_CONTAINER_NAME
									};

	/**
	 * Constructor.
	 *
	 * @param parentPage The parent dialog page this control is embedded in.
	 *                   Might be <code>null</code> if the control is not associated with a page.
	 */
	public WireTypeControl(IDialogPage parentPage) {
		super(parentPage);
		setIsGroup(false);
		setReadOnly(true);
		setHideBrowseButton(true);
		setEditFieldLabel(Messages.WireTypeControl_label);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.controls.BaseEditBrowseTextControl#setupPanel(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void setupPanel(Composite parent) {
		super.setupPanel(parent);

		List<String> wireTypeLabels = new ArrayList<String>();
		for (String wireType : WIRE_TYPES) {
			String label = getWireTypeLabel(wireType);
			if (label != null) wireTypeLabels.add(label);
		}

		setEditFieldControlHistory(wireTypeLabels.toArray(new String[wireTypeLabels.size()]));
		SWTControlUtil.select(getEditFieldControl(), 0);
	}

	/**
	 * Returns the list of supported wire types.
	 *
	 * @return The list of supported wire types.
	 */
	public static final String[] getSupportedWireTypes() {
		return Arrays.copyOf(WIRE_TYPES, WIRE_TYPES.length);
	}

	/**
	 * Returns the label of the given wire type.
	 *
	 * @param wireType The wire type. Must not be <code>null</code>.
	 * @return The corresponding label or <code>null</code> if the wire type is unknown.
	 */
	protected String getWireTypeLabel(String wireType) {
		Assert.isNotNull(wireType);

		if (IWireTypeNetwork.PROPERTY_CONTAINER_NAME.equals(wireType)) return Messages.WireTypeControl_networkType_label;
		if (IWireTypeSerial.PROPERTY_CONTAINER_NAME.equals(wireType)) return Messages.WireTypeControl_serialType_label;

		return null;
	}

	/**
	 * Returns the currently selected wire type.
	 *
	 * @return The currently selected wire type.
	 */
	public String getSelectedWireType() {
		String type = getEditFieldControlText();

		if (Messages.WireTypeControl_networkType_label.equals(type)) type = IWireTypeNetwork.PROPERTY_CONTAINER_NAME;
		else if (Messages.WireTypeControl_serialType_label.equals(type)) type = IWireTypeSerial.PROPERTY_CONTAINER_NAME;

		return type;
	}

	/**
	 * Sets the selected wire type to the specified one.
	 *
	 * @param wireType The wire type. Must not be <code>null</code>.
	 */
	public void setSelectedWireType(String wireType) {
		Assert.isNotNull(wireType);

		// Get the wire type label for given wire type
		String label = getWireTypeLabel(wireType);
		int index = SWTControlUtil.indexOf(getEditFieldControl(), label);
		if (index != -1) SWTControlUtil.select(getEditFieldControl(), index);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.controls.BaseEditBrowseTextControl#doRestoreWidgetValues(org.eclipse.jface.dialogs.IDialogSettings, java.lang.String)
	 */
	@Override
	public void doRestoreWidgetValues(IDialogSettings settings, String idPrefix) {
		// The widget is not user editable and the history is used
		// for presenting the available wire types. Neither save
		// or restore the history actively.
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.controls.BaseEditBrowseTextControl#doSaveWidgetValues(org.eclipse.jface.dialogs.IDialogSettings, java.lang.String)
	 */
	@Override
	public void doSaveWidgetValues(IDialogSettings settings, String idPrefix) {
		// The widget is not user editable and the history is used
		// for presenting the available wire types. Neither save
		// or restore the history actively.
	}
}
