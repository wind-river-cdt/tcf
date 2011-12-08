/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.controls.wire.serial;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tcf.te.ui.controls.BaseEditBrowseTextControl;
import org.eclipse.tcf.te.ui.controls.interfaces.help.IContextHelpIds;
import org.eclipse.tcf.te.ui.controls.net.RemoteHostAddressControl;
import org.eclipse.tcf.te.ui.controls.nls.Messages;
import org.eclipse.tcf.te.ui.controls.validator.NameOrIPValidator;
import org.eclipse.tcf.te.ui.controls.validator.NumberValidator;
import org.eclipse.tcf.te.ui.controls.validator.PortNumberValidator;
import org.eclipse.tcf.te.ui.controls.validator.PortNumberVerifyListener;
import org.eclipse.tcf.te.ui.controls.validator.RegexValidator;
import org.eclipse.tcf.te.ui.controls.validator.Validator;
import org.eclipse.tcf.te.ui.jface.dialogs.CustomTitleAreaDialog;

/**
 * Serial line port or address dialog.
 */
public class SerialPortAddressDialog extends CustomTitleAreaDialog {

	BaseEditBrowseTextControl ttyControl;
	RemoteHostAddressControl addressControl;
	BaseEditBrowseTextControl portControl;

	Validator ttyValidator;
	Validator portValidator;

	List<String> ttyHistory;
	List<String> tcpHistory;

	String data = null;

	/**
	 * Constructor.
	 * @param parentShell
	 */
	public SerialPortAddressDialog(Shell parentShell, String selected, List<String> ttyHistory, List<String> tcpHistory) {
		super(parentShell, IContextHelpIds.SERIAL_PORT_ADDRESS_DIALOG);
		this.ttyHistory = ttyHistory;
		this.tcpHistory = tcpHistory;
		this.data = selected;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#isResizable()
	 */
	@Override
	protected boolean isResizable() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.dialogs.CustomTitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		setDialogTitle(Messages.SerialLinePanel_customSerialDevice_title);

		//set margins of dialog and apply dialog font
		Composite container = (Composite) super.createDialogArea(parent);

		Composite ttyComp = new Composite(container, SWT.NONE);
		GridLayout gl = new GridLayout();
		ttyComp.setLayout(gl);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = 250;
		ttyComp.setLayoutData(gd);

		ttyControl = new BaseEditBrowseTextControl(null);
		ttyControl.setLabelIsButton(true);
		parent.getDisplay().asyncExec(new Runnable() {
			@Override
            public void run() {
				boolean enable = ttyHistory != null && ttyHistory.contains(data);
				setTTYControlEnabled(enable);
				setTCPControlEnabled(!enable);
				onModify();
			}
		});
		ttyControl.setIsGroup(false);
		ttyControl.setEditFieldLabel(Messages.SerialLinePanel_hostTTYDevice_label);
		ttyControl.setHideBrowseButton(true);
		ttyControl.setupPanel(ttyComp);
		((Button)ttyControl.getLabelControl()).addSelectionListener(new SelectionListener(){
			@Override
            public void widgetSelected(SelectionEvent e) {
				boolean selected = ((Button)ttyControl.getLabelControl()).getSelection();
				setTTYControlEnabled(selected);
				setTCPControlEnabled(!selected);
				onModify();
			}
			@Override
            public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		((Combo)ttyControl.getEditFieldControl()).addModifyListener(new ModifyListener(){
			@Override
            public void modifyText(ModifyEvent e) {
				onModify();
			}
		});

		Composite tcpComp = new Composite(container, SWT.NONE);
		gl = new GridLayout(4, true);
		tcpComp.setLayout(gl);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		tcpComp.setLayoutData(gd);

		Composite tcpAddrComp = new Composite(tcpComp, SWT.NONE);
		gl = new GridLayout();
		gl.marginWidth = 0;
		gl.marginHeight = 0;
		tcpAddrComp.setLayout(gl);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		tcpAddrComp.setLayoutData(gd);

		addressControl = new RemoteHostAddressControl(null) {

			/* (non-Javadoc)
			 * @see org.eclipse.tcf.te.ui.controls.BaseEditBrowseTextControl#modifyText(org.eclipse.swt.events.ModifyEvent)
			 */
			@Override
			public void modifyText(ModifyEvent e) {
				super.modifyText(e);
				onModify();
			}

			/* (non-Javadoc)
			 * @see org.eclipse.tcf.te.ui.controls.BaseEditBrowseTextControl#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean selected = ((Button)addressControl.getLabelControl()).getSelection();
				setTTYControlEnabled(!selected);
				setTCPControlEnabled(selected);
				onModify();
			}

			/* (non-Javadoc)
			 * @see org.eclipse.tcf.te.ui.controls.net.RemoteHostAddressControl#setCheckResultMessage(int, java.lang.String)
			 */
			@Override
			protected void setCheckResultMessage(int severity, String message) {
				SerialPortAddressDialog.this.setMessage(message, severity);
			}
		};
		addressControl.setLabelIsButton(true);
		addressControl.setIsGroup(false);
		addressControl.setEditFieldLabel(org.eclipse.tcf.te.ui.controls.nls.Messages.RemoteHostAddressControl_label);
		addressControl.setButtonLabel(org.eclipse.tcf.te.ui.controls.nls.Messages.RemoteHostAddressControl_button_label);
		addressControl.setupPanel(tcpAddrComp);

		Composite tcpPortComp = new Composite(tcpComp, SWT.NONE);
		gl = new GridLayout();
		gl.marginWidth = 0;
		gl.marginHeight = 0;
		tcpPortComp.setLayout(gl);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		tcpPortComp.setLayoutData(gd);

		portControl = new BaseEditBrowseTextControl(null);
		portControl.setIsGroup(false);
		portControl.setEditFieldLabel(Messages.SerialPortAddressDialog_port);
		portControl.setHideBrowseButton(true);
		portControl.setupPanel(tcpPortComp);
		((Combo)portControl.getEditFieldControl()).addVerifyListener(new PortNumberVerifyListener(PortNumberVerifyListener.ATTR_DECIMAL | PortNumberVerifyListener.ATTR_HEX));
		((Combo)portControl.getEditFieldControl()).addModifyListener(new ModifyListener(){
			@Override
            public void modifyText(ModifyEvent e) {
				onModify();
			}
		});

		// Trigger the runnable after having created all controls!
		parent.getDisplay().asyncExec(new Runnable() {
			@Override
            public void run() {
				boolean enable = tcpHistory != null && tcpHistory.contains(data);
				setTTYControlEnabled(!enable);
				setTCPControlEnabled(enable);
				onModify();
			}
		});

		ttyValidator = new Validator(Validator.ATTR_MANDATORY) {
			private final Pattern fValidCharacters = System.getProperty("os.name","").toLowerCase().startsWith("windows") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
															? Pattern.compile("[\\w]+") : Pattern.compile("[\\w/]+"); //$NON-NLS-1$ //$NON-NLS-2$

				@Override
				public boolean isValid(String newText) {
					setMessage(null);
					setMessageType(INFORMATION);
					if (newText != null && newText.trim().length() > 0) {
						Matcher matcher = fValidCharacters.matcher(newText);
						if (!matcher.matches()) {
							setMessage(Messages.SerialLinePanel_error_invalidCharactes, ERROR);
						}
					}
					else if (newText != null) {
						setMessage(Messages.SerialLinePanel_error_emptyHostTTYDevice, INFORMATION);
					}
					return getMessageType() != ERROR;
				}
		};
		ttyControl.setEditFieldValidator(ttyValidator);

		portValidator = new PortNumberValidator(Validator.ATTR_MANDATORY | PortNumberValidator.ATTR_DECIMAL | PortNumberValidator.ATTR_HEX);
		portValidator.setMessageText(RegexValidator.INFO_MISSING_VALUE,
		                              Messages.SerialPortAddressDialog_Information_MissingPort);
		portValidator.setMessageText(RegexValidator.ERROR_INVALID_VALUE,
		                              Messages.SerialPortAddressDialog_Error_InvalidPort);
		portValidator.setMessageText(NumberValidator.ERROR_INVALID_RANGE,
		                              Messages.SerialPortAddressDialog_Error_InvalidPortRange);
		portControl.setEditFieldValidator(portValidator);

		applyDialogFont(container);

		setupData();

		return container;
	}

	private void setupData() {
		setTTYControlEnabled(true);
		setTCPControlEnabled(false);
		if (ttyHistory != null && !ttyHistory.isEmpty()) {
			for (String tty : ttyHistory) {
				ttyControl.addToEditFieldControlHistory(tty.trim());
				if (tty.equals(data)) {
					ttyControl.setEditFieldControlText(tty.trim());
				}
			}
		}
		if (tcpHistory != null && !tcpHistory.isEmpty()) {
			for (String tcp : tcpHistory) {
				String[] data = tcp.split(":"); //$NON-NLS-1$
				if (data.length > 1) {
					addressControl.addToEditFieldControlHistory(data[1]);
				}
				if (data.length > 2) {
					portControl.addToEditFieldControlHistory(data[2]);
				}
				if (tcp.equals(this.data)) {
					setTTYControlEnabled(false);
					setTCPControlEnabled(true);
					if (data.length > 1) {
						addressControl.setEditFieldControlText(data[1]);
					}
					if (data.length > 2) {
						portControl.setEditFieldControlText(data[2]);
					}
				}
			}
		}
		onModify();
	}

	void setTTYControlEnabled(boolean enable) {
		ttyControl.setLabelControlSelection(enable);
		ttyControl.getEditFieldControl().setEnabled(enable);
	}

	void setTCPControlEnabled(boolean enable) {
		addressControl.setLabelControlSelection(enable);
		addressControl.getEditFieldControl().setEnabled(enable);
		addressControl.getButtonControl().setEnabled(enable);
		portControl.setEnabled(enable);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TrayDialog#createButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createButtonBar(Composite parent) {
		Control control =  super.createButtonBar(parent);
		setButtonEnabled(OK, false);
		return control;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.dialogs.CustomTitleAreaDialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		if (((Button)ttyControl.getLabelControl()).getSelection()) {
			data = ttyControl.getEditFieldControlText();
		}
		else {
			data = "tcp:" + addressControl.getEditFieldControlText() + ":" + portControl.getEditFieldControlText(); //$NON-NLS-1$ //$NON-NLS-2$
		}
		super.okPressed();
	}

	void onModify() {
		setMessage(null);

		boolean ttySelected = ((Button)ttyControl.getLabelControl()).getSelection();

		boolean isTTYValid = ttyControl.isValid();
		if (ttySelected && ttyControl.getMessageType() > getMessageType()) {
			setMessage(ttyControl.getMessage(), ttyControl.getMessageType());
		}

		boolean isTCPValid = addressControl.isValid();
		if (!ttySelected && addressControl.getMessageType() > getMessageType()) {
			setMessage(addressControl.getMessage(), addressControl.getMessageType());
		}

		isTCPValid &= portControl.isValid();
		if (!ttySelected && portControl.getMessageType() > getMessageType()) {
			setMessage(portControl.getMessage(), portControl.getMessageType());
		}

		if (getMessage() == null) {
			setDefaultMessage(Messages.SerialLinePanel_customSerialDevice_message, IMessageProvider.INFORMATION);
		}

		addressControl.getButtonControl().setEnabled(!ttySelected && addressControl.isValid() &&
			(addressControl.getEditFieldValidator() instanceof NameOrIPValidator) &&
			((NameOrIPValidator)addressControl.getEditFieldValidator()).isName());
		setButtonEnabled(OK, ttySelected ? isTTYValid : isTCPValid);
	}

	/**
	 * Return the new name after OK was pressed.
	 * Unless OK was pressed, the old name is returned.
	 */
	public String getData() {
		return data;
	}
}
