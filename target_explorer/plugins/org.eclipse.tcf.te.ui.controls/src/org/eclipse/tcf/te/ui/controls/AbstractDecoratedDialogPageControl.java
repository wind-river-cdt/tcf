/*
 * AbstractDecoratedDialogPageControl.java
 * Created on 05.03.2012
 *
 * Copyright 2012 Wind River Systems Inc. All rights reserved.
 */
package org.eclipse.tcf.te.ui.controls;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;

/**
 * AbstractDecoratedDialogPageControl
 * @author tobias.schwarz@windriver.com
 */
public abstract class AbstractDecoratedDialogPageControl extends BaseDialogPageControl {

	private ControlDecoration controlDecoration;

	/**
	 * Constructor.
	 */
	public AbstractDecoratedDialogPageControl() {
	}

	/**
	 * Constructor.
	 * @param parentPage
	 */
	public AbstractDecoratedDialogPageControl(IDialogPage parentPage) {
		super(parentPage);
	}

	/**
	 * Creates a new instance of a {@link ControlDecoration} object associated with
	 * the given control. The method is called after the control has been created.
	 *
	 * @param control The control. Must not be <code>null</code>.
	 * @return The control decoration object instance.
	 */
	public ControlDecoration doCreateControlDecoration(Control control) {
		Assert.isNotNull(control);
		controlDecoration = new ControlDecoration(control, doGetControlDecorationPosition());
		return controlDecoration;
	}

	/**
	 * Returns the control decoration position. The default is
	 * {@link SWT#TOP} | {@link SWT#LEFT}.
	 *
	 * @return The control position.
	 */
	protected int doGetControlDecorationPosition() {
		return SWT.TOP | SWT.LEFT;
	}

	/**
	 * Configure the given control decoration.
	 *
	 * @param decoration The control decoration. Must not be <code>null</code>.
	 */
	protected void configureControlDecoration(ControlDecoration decoration) {
		Assert.isNotNull(decoration);
		decoration.setShowOnlyOnFocus(false);
	}

	/**
	 * Updates the control decoration to represent the given message and message type.
	 * If the message is <code>null</code> or the message type is IMessageProvider.NONE,
	 * no decoration will be shown.
	 *
	 * @param message The message.
	 * @param messageType The message type.
	 */
	public void updateControlDecoration(String message, int messageType) {
		if (getControlDecoration() != null) {
			// The description is the same as the message
			getControlDecoration().setDescriptionText(message);

			// The icon depends on the message type
			FieldDecorationRegistry registry = FieldDecorationRegistry.getDefault();

			// Determine the id of the decoration to show
			String decorationId = FieldDecorationRegistry.DEC_INFORMATION;
			if (messageType == IMessageProvider.ERROR) {
				decorationId = FieldDecorationRegistry.DEC_ERROR;
			} else if (messageType == IMessageProvider.WARNING) {
				decorationId = FieldDecorationRegistry.DEC_WARNING;
			}

			// Get the field decoration
			FieldDecoration fieldDeco = registry.getFieldDecoration(decorationId);
			if (fieldDeco != null) {
				getControlDecoration().setImage(fieldDeco.getImage());
			}

			if (message == null || messageType == IMessageProvider.NONE) {
				getControlDecoration().hide();
			}
			else {
				getControlDecoration().show();
			}
		}
	}

	/**
	 * Returns the control decoration.
	 *
	 * @return The control decoration instance or <code>null</code> if not yet created.
	 */
	public final ControlDecoration getControlDecoration() {
		return controlDecoration;
	}
}