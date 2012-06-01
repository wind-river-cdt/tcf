/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.controls.wire.serial;

import gnu.io.CommPortIdentifier;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tcf.te.core.nodes.interfaces.wire.IWireTypeSerial;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.runtime.services.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tcf.te.ui.controls.BaseDialogPageControl;
import org.eclipse.tcf.te.ui.controls.activator.UIPlugin;
import org.eclipse.tcf.te.ui.controls.interfaces.tracing.ITraceIds;
import org.eclipse.tcf.te.ui.controls.nls.Messages;
import org.eclipse.tcf.te.ui.controls.panels.AbstractWizardConfigurationPanel;
import org.eclipse.tcf.te.ui.controls.validator.NumberValidator;
import org.eclipse.tcf.te.ui.controls.validator.RegexValidator;
import org.eclipse.tcf.te.ui.controls.validator.Validator;
import org.eclipse.tcf.te.ui.interfaces.data.IDataExchangeNode3;
import org.eclipse.tcf.te.ui.jface.interfaces.IValidatingContainer;
import org.eclipse.tcf.te.ui.swt.SWTControlUtil;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * Serial line wire type wizard configuration panel.
 */
public class SerialLinePanel extends AbstractWizardConfigurationPanel implements IDataExchangeNode3 {
	public static final String fcDefaultTTYSpeed = "9600"; //$NON-NLS-1$
	public static final String fcDefaultTTYDeviceWin32 = "COM1"; //$NON-NLS-1$
	public static final String fcDefaultTTYDeviceSolaris = "/dev/cua/a"; //$NON-NLS-1$
	public static final String fcDefaultTTYDeviceLinux = "/dev/ttyS0"; //$NON-NLS-1$
	public static final String fcDefaultTTYDatabits = "8"; //$NON-NLS-1$
	public static final String fcDefaultTTYParity = "None"; //$NON-NLS-1$
	public static final String fcDefaultTTYStopbits = "1"; //$NON-NLS-1$
	public static final String fcDefaultTTYFlowControl = "None"; //$NON-NLS-1$
	public static final String fcDefaultTTYTimeout = "5"; //$NON-NLS-1$
	public static final String fcEditableTTYOther = "Other..."; //$NON-NLS-1$

	private static final String[] fcTTYSpeedRates = { "600", //$NON-NLS-1$
		"1200", //$NON-NLS-1$
		"2400", //$NON-NLS-1$
		"4800", //$NON-NLS-1$
		"9600", //$NON-NLS-1$
		"14400", //$NON-NLS-1$
		"19200", //$NON-NLS-1$
		"38400", //$NON-NLS-1$
		"57600", //$NON-NLS-1$
		"115200" //$NON-NLS-1$
	};

	private static final String[] fcTTYDatabits = {
		"8", "7" //$NON-NLS-1$ //$NON-NLS-2$
	};

	private static final String[] fcTTYParity = {
		"None", "Odd", "Even" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	};

	private static final String[] fcTTYStopbits = {
		"1", "2" //$NON-NLS-1$ //$NON-NLS-2$
	};

	private static final String[] fcTTYFlowControl = {
		"None", "Hardware", "Software" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	};

	private final boolean editable;
	private final boolean terminalMode;
	private final boolean showAdvancedSerialOptions;

	Label hostTTYDeviceLabel;
	Combo hostTTYDeviceCombo;
	Label hostTTYSpeedLabel;
	Combo hostTTYSpeedCombo;
	Label hostTTYBitsLabel;
	Combo hostTTYBitsCombo;
	Label hostTTYParityLabel;
	Combo hostTTYParityCombo;
	Label hostTTYStopbitsLabel;
	Combo hostTTYStopbitsCombo;
	Label hostTTYFlowControlLabel;
	Combo hostTTYFlowControlCombo;
	Label hostTTYTimeoutLabel;
	Text  hostTTYTimeoutText;

	// Keep the fInputValidator protected!
	protected IInputValidator inputValidatorBaud;

	int lastSelected = -1;
	int lastSelectedBaud = -1;

	/**
	 * Constructor.
	 *
	 * @param parentPageControl The parent control this backend configuration panel is embedded in. Must not be <code>null</code>!
	 * @param terminalMode Specify <code>true</code> if the configuration panel controls are layout one per row.
	 * @param editable Specify <code>true</code> if the user should be allowed to edit the serial device name, <code>false</code> otherwise.
	 */
	public SerialLinePanel(BaseDialogPageControl parentPageControl, boolean terminalMode, boolean editable) {
		this(parentPageControl, terminalMode, editable, false);
	}

	/**
	 * Constructor.
	 *
	 * @param parentPageControl The parent control this backend configuration panel is embedded in. Must not be <code>null</code>!
	 * @param terminalMode Specify <code>true</code> if the configuration panel controls are layout one per row.
	 * @param editable Specify <code>true</code> if the user should be allowed to edit the serial device name and serial baud rate, <code>false</code> otherwise.
	 * @param showAdvancedOptions If <code>true</code>, advanced serial options are available to the user.
	 */
	public SerialLinePanel(BaseDialogPageControl parentPageControl, boolean terminalMode, boolean editable, boolean showAdvancedOptions) {
		super(parentPageControl);
		this.terminalMode = terminalMode;
		this.editable = editable;
		this.showAdvancedSerialOptions = showAdvancedOptions;
	}

	protected class CustomSerialBaudRateInputValidator implements IInputValidator {
		private final Validator validator;

		/**
		 * Constructor.
		 *
		 */
		public CustomSerialBaudRateInputValidator() {
			validator = new NumberValidator();
			validator.setMessageText(RegexValidator.ERROR_INVALID_VALUE, Messages.SerialLinePanel_error_invalidCharactesBaudRate);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.IInputValidator#isValid(java.lang.String)
		 */
		@Override
        public String isValid(String newText) {
			if (newText != null && newText.trim().length() > 0) {
				if (!validator.isValid(newText)) {
					return validator.getMessage();
				}
			} else if (newText != null) {
				// Empty string is an error without message (see interface)!
				return ""; //$NON-NLS-1$
			}
			return null;
		}
	}

	/**
	 * Returns the input validator to be used for checking the custom serial
	 * baud rate for basic plausibility.
	 */
	protected IInputValidator getCustomSerialBaudRateInputValidator() {
		if (inputValidatorBaud == null) {
			inputValidatorBaud = new CustomSerialBaudRateInputValidator();
		}
		return inputValidatorBaud;
	}

	/**
	 * Returns if or if not to adjust the background color of the panels.
	 *
	 * @return <code>True</code> to adjust the background color.
	 */
	protected boolean isAdjustBackgroundColor() {
		return getParentControl().getParentPage() != null || terminalMode;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.controls.interfaces.IWizardConfigurationPanel#setupPanel(org.eclipse.swt.widgets.Composite, org.eclipse.ui.forms.widgets.FormToolkit)
	 */
	@Override
    public void setupPanel(Composite parent, FormToolkit toolkit) {
		Assert.isNotNull(parent);
		Assert.isNotNull(toolkit);

		boolean adjustBackgroundColor = isAdjustBackgroundColor();

		Composite panel = toolkit.createComposite(parent);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0; layout.marginWidth = 0;
		panel.setLayout(layout);
		panel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		if (adjustBackgroundColor) panel.setBackground(parent.getBackground());

		setControl(panel);

		// Create the wire type section
		Composite section;
		if (!terminalMode) {
			section = toolkit.createSection(panel, ExpandableComposite.TITLE_BAR);
			Assert.isNotNull(section);
			((Section)section).setText(Messages.SerialLinePanel_section);
			section.setLayout(new GridLayout());
			section.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		} else {
			// No section -> Use the panel directly
			section = panel;
		}
		if (adjustBackgroundColor) section.setBackground(panel.getBackground());

		final Composite client = toolkit.createComposite(section);
		Assert.isNotNull(client);
		client.setLayout(new GridLayout(terminalMode ? 2 : 4, false));
		client.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		if (adjustBackgroundColor) client.setBackground(section.getBackground());
		if (section instanceof Section) ((Section)section).setClient(client);

		// Host TTY settings
		hostTTYDeviceLabel = new Label(client, SWT.NONE);
		hostTTYDeviceLabel.setText(terminalMode ? Messages.SerialLinePanel_hostTTYDevice_label_terminalMode : Messages.SerialLinePanel_hostTTYDevice_label);

		hostTTYDeviceCombo = new Combo(client, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
		hostTTYDeviceCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		hostTTYDeviceCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// if the user selected the special editable device, show a dialog asking for the device name
				if (fcEditableTTYOther.equals(SWTControlUtil.getText(hostTTYDeviceCombo))) {
					List<String> tty = new ArrayList<String>();
					List<String> tcp = new ArrayList<String>();
					String selected = SWTControlUtil.getItem(hostTTYDeviceCombo, lastSelected);
					for (String device : SWTControlUtil.getItems(hostTTYDeviceCombo)) {
						if (!device.equalsIgnoreCase(fcEditableTTYOther)) {
							if (device.toUpperCase().startsWith("TCP:")) { //$NON-NLS-1$
								tcp.add(device);
							}
							else {
								tty.add(device);
							}
						}
					}
					SerialPortAddressDialog dialog = new SerialPortAddressDialog(client.getShell(), selected, tty, tcp);
					if (dialog.open() == Window.OK) {
						// retrieve the custom serial device name and set it to the combobox drop
						String device = dialog.getData();
						if (device != null && device.trim().length() > 0) {
							SWTControlUtil.add(hostTTYDeviceCombo, device.trim());
							SWTControlUtil.setText(hostTTYDeviceCombo, device.trim());
						} else if (lastSelected != -1) {
							SWTControlUtil.setText(hostTTYDeviceCombo, SWTControlUtil.getItem(hostTTYDeviceCombo, lastSelected));
						}
					} else if (lastSelected != -1){
						SWTControlUtil.setText(hostTTYDeviceCombo, SWTControlUtil.getItem(hostTTYDeviceCombo, lastSelected));
					}
				}
				lastSelected = SWTControlUtil.getSelectionIndex(hostTTYDeviceCombo);

				IValidatingContainer validatingContainer = SerialLinePanel.this.getParentControl().getValidatingContainer();
				if (validatingContainer != null) validatingContainer.validate();
			}
		});

		hostTTYSpeedLabel = new Label(client, SWT.NONE);
		hostTTYSpeedLabel.setText(terminalMode ? Messages.SerialLinePanel_hostTTYSpeed_label_terminalMode : Messages.SerialLinePanel_hostTTYSpeed_label);

		hostTTYSpeedCombo = new Combo(client, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
		hostTTYSpeedCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		hostTTYSpeedCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// if the user selected the special editable baud rate, show a dialog asking for the baud rate
				if (fcEditableTTYOther.equals(SWTControlUtil.getText(hostTTYSpeedCombo))) {
					InputDialog dialog = new InputDialog(getControl().getShell(),
					                                     Messages.SerialLinePanel_customSerialBaudRate_title,
					                                     Messages.SerialLinePanel_customSerialBaudRate_message,
					                                     "", //$NON-NLS-1$
					                                     getCustomSerialBaudRateInputValidator());
					if (dialog.open() == Window.OK) {
						// retrieve the custom serial device name and set it to the combobox drop
						String device = dialog.getValue();
						if (device != null && device.trim().length() > 0) {
							int index = SWTControlUtil.indexOf(hostTTYSpeedCombo, fcEditableTTYOther);
							if (index != -1 && index == SWTControlUtil.getItemCount(hostTTYSpeedCombo) - 1) {
								SWTControlUtil.add(hostTTYSpeedCombo, device.trim());
							} else if (index != -1) {
								SWTControlUtil.setItem(hostTTYSpeedCombo, index + 1, device.trim());
							}
							SWTControlUtil.setText(hostTTYSpeedCombo, device.trim());
						} else if (lastSelectedBaud != -1) {
							SWTControlUtil.setText(hostTTYSpeedCombo, SWTControlUtil.getItem(hostTTYSpeedCombo, lastSelectedBaud));
						}
					} else if (lastSelectedBaud != -1){
						SWTControlUtil.setText(hostTTYSpeedCombo, SWTControlUtil.getItem(hostTTYSpeedCombo, lastSelectedBaud));
					}
				}
				lastSelectedBaud = SWTControlUtil.getSelectionIndex(hostTTYSpeedCombo);

				IValidatingContainer validatingContainer = SerialLinePanel.this.getParentControl().getValidatingContainer();
				if (validatingContainer != null) validatingContainer.validate();
			}
		});

		// Query the list of available serial port interfaces.
		UIPlugin.getTraceHandler().trace("SerialLinePanel: Start quering the available comm ports.", ITraceIds.TRACE_SERIAL_LINE_PANEL, this); //$NON-NLS-1$

		// Query the serial devices now. If we are in the wizard, we can show a progress
		// bar in the bottom of the wizard. Otherwise, show at least a busy indicator.
		if (getParentControl().getRunnableContext() instanceof WizardDialog) {
			IRunnableContext context = getParentControl().getRunnableContext();

			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				@Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask(Messages.SerialLinePanel_task_queryAvailableSerialDevices, IProgressMonitor.UNKNOWN);
					queryAvailableSerialDevices();
					monitor.done();
				}
			};
			try {
				context.run(true, false, runnable);
			} catch (InvocationTargetException e) {
				/* ignored on purpose. The runnable is directly declared here. */
			} catch (InterruptedException e) {
				/* ignored on purpose. The runnable is not cancelable */
			}
		} else {
			BusyIndicator.showWhile(getControl().getDisplay(), new Runnable() {
				@Override
                public void run() {
					queryAvailableSerialDevices();
				}
			});
		}

		// add a special device which is being the editable one if requested at the end of the list
		if (editable) {
			SWTControlUtil.add(hostTTYDeviceCombo, fcEditableTTYOther);
		}

		if (SWTControlUtil.indexOf(hostTTYDeviceCombo, getDefaultHostTTYDevice()) != -1) {
			SWTControlUtil.setText(hostTTYDeviceCombo, getDefaultHostTTYDevice());
		} else {
			if ("".equals(SWTControlUtil.getText(hostTTYDeviceCombo)) && SWTControlUtil.getItemCount(hostTTYDeviceCombo) > 0) { //$NON-NLS-1$
				// USI: For SWT-GTK we need the special empty entry as well. Otherwise we will have problems
				// getting the selection changed event!
				if (SWTControlUtil.getItemCount(hostTTYDeviceCombo) == 1
					&& fcEditableTTYOther.equals(SWTControlUtil.getItem(hostTTYDeviceCombo, 0))) {
					SWTControlUtil.add(hostTTYDeviceCombo, "", 0, true); //$NON-NLS-1$
				}
				SWTControlUtil.setText(hostTTYDeviceCombo, SWTControlUtil.getItem(hostTTYDeviceCombo, 0));
			}
		}

		if (SWTControlUtil.getItemCount(hostTTYDeviceCombo) > 0) {
			SWTControlUtil.setEnabled(hostTTYDeviceCombo, true);
		} else {
			SWTControlUtil.setEnabled(hostTTYDeviceCombo, false);
		}
		lastSelected = SWTControlUtil.getSelectionIndex(hostTTYDeviceCombo);

		for (String fcTTYSpeedRate : fcTTYSpeedRates) {
			SWTControlUtil.add(hostTTYSpeedCombo, fcTTYSpeedRate);
		}
		if (editable) {
			SWTControlUtil.add(hostTTYSpeedCombo, fcEditableTTYOther);
		}

		SWTControlUtil.setText(hostTTYSpeedCombo, fcDefaultTTYSpeed);
		lastSelectedBaud = SWTControlUtil.getSelectionIndex(hostTTYSpeedCombo);

		// add the advanced serial options if configured
		if (showAdvancedSerialOptions) {
			Composite bitsPanel = terminalMode ? client : new Composite(client, SWT.NONE);
			if (!terminalMode) {
				layout = new GridLayout();
				layout.marginHeight = 0; layout.marginWidth = 0;
				layout.numColumns = 3;
				bitsPanel.setLayout(layout);
				GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
				layoutData.horizontalSpan = 4;
				bitsPanel.setLayoutData(layoutData);
			}

			Composite panel2 = terminalMode ? client : new Composite(bitsPanel, SWT.NONE);
			if (!terminalMode) {
				layout = new GridLayout(2, false);
				layout.marginHeight = 0;
				layout.marginWidth = 0;
				panel.setLayout(layout);
				panel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			}

			hostTTYBitsLabel = new Label(panel2, SWT.NONE);
			hostTTYBitsLabel.setText(Messages.SerialLinePanel_hostTTYDatabits_label);
			hostTTYBitsCombo = new Combo(panel2, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
			hostTTYBitsCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			hostTTYBitsCombo.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					IValidatingContainer validatingContainer = SerialLinePanel.this.getParentControl().getValidatingContainer();
					if (validatingContainer != null) validatingContainer.validate();
				}
			});

			for (String fcTTYDatabit : fcTTYDatabits) {
				SWTControlUtil.add(hostTTYBitsCombo, fcTTYDatabit);
			}
			SWTControlUtil.setText(hostTTYBitsCombo, fcDefaultTTYDatabits);

			hostTTYParityLabel = new Label(panel2, SWT.NONE);
			hostTTYParityLabel.setText(Messages.SerialLinePanel_hostTTYParity_label);
			hostTTYParityCombo = new Combo(panel2, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
			hostTTYParityCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			hostTTYParityCombo.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					IValidatingContainer validatingContainer = SerialLinePanel.this.getParentControl().getValidatingContainer();
					if (validatingContainer != null) validatingContainer.validate();
				}
			});

			for (String element : fcTTYParity) {
				SWTControlUtil.add(hostTTYParityCombo, element);
			}
			SWTControlUtil.setText(hostTTYParityCombo, fcDefaultTTYParity);

			hostTTYStopbitsLabel = new Label(panel2, SWT.NONE);
			hostTTYStopbitsLabel.setText(Messages.SerialLinePanel_hostTTYStopbits_label);
			hostTTYStopbitsCombo = new Combo(panel2, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
			hostTTYStopbitsCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			hostTTYStopbitsCombo.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					IValidatingContainer validatingContainer = SerialLinePanel.this.getParentControl().getValidatingContainer();
					if (validatingContainer != null) validatingContainer.validate();
				}
			});

			for (String fcTTYStopbit : fcTTYStopbits) {
				SWTControlUtil.add(hostTTYStopbitsCombo, fcTTYStopbit);
			}
			SWTControlUtil.setText(hostTTYStopbitsCombo, fcDefaultTTYStopbits);

			hostTTYFlowControlLabel = new Label(panel2, SWT.NONE);
			hostTTYFlowControlLabel.setText(Messages.SerialLinePanel_hostTTYFlowControl_label);
			hostTTYFlowControlCombo = new Combo(panel2, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
			hostTTYFlowControlCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			hostTTYFlowControlCombo.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					IValidatingContainer validatingContainer = SerialLinePanel.this.getParentControl().getValidatingContainer();
					if (validatingContainer != null) validatingContainer.validate();
				}
			});

			for (String element : fcTTYFlowControl) {
				SWTControlUtil.add(hostTTYFlowControlCombo, element);
			}
			SWTControlUtil.setText(hostTTYFlowControlCombo, fcDefaultTTYFlowControl);

			if (terminalMode) {
				hostTTYTimeoutLabel = new Label(panel2, SWT.NONE);
				hostTTYTimeoutLabel.setText(Messages.SerialLinePanel_hostTTYTimeout_label);
				hostTTYTimeoutText = new Text(panel2, SWT.SINGLE | SWT.BORDER);
				hostTTYTimeoutText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				hostTTYTimeoutText.addModifyListener(new ModifyListener() {
					@Override
					public void modifyText(ModifyEvent e) {
						IValidatingContainer validatingContainer = SerialLinePanel.this.getParentControl().getValidatingContainer();
						if (validatingContainer != null) validatingContainer.validate();
					}
				});
				SWTControlUtil.setText(hostTTYTimeoutText, fcDefaultTTYTimeout);
			}
		}
	}

	/**
	 * Query the list of serial devices via RXTX.
	 */
	protected void queryAvailableSerialDevices() {
		// Avoid printing the library version output to stdout if the platform
		// is not in debug mode.
		String prop = System.getProperty("gnu.io.rxtx.NoVersionOutput"); //$NON-NLS-1$
		if (prop == null && !Platform.inDebugMode()) {
			System.setProperty("gnu.io.rxtx.NoVersionOutput", "true"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		// java.lang.UnsatisfiedLinkError: ../plugins/gnu.io.rxtx.solaris.sparc_2.1.7.200702281917/os/solaris/sparc/librxtxSerial.so:
		//       Can't load Sparc 32-bit .so on a Sparc 32-bit platform
		// May happen in CommPortIdentifier static constructor!
		try {
            Enumeration<CommPortIdentifier> ttyPortIds = CommPortIdentifier.getPortIdentifiers();
			if (!ttyPortIds.hasMoreElements()) {
				UIPlugin.getTraceHandler().trace("SerialLinePanel: NO comm ports available at all!", ITraceIds.TRACE_SERIAL_LINE_PANEL, this); //$NON-NLS-1$
			}
			final List<String> ports = new ArrayList<String>();
			while (ttyPortIds.hasMoreElements()) {
				CommPortIdentifier port = ttyPortIds.nextElement();
				String type = "unknown"; //$NON-NLS-1$
				if (port.getPortType() == CommPortIdentifier.PORT_PARALLEL) {
					type = "parallel"; //$NON-NLS-1$
				}
				if (port.getPortType() == CommPortIdentifier.PORT_SERIAL) {
					type = "serial"; //$NON-NLS-1$
				}
				UIPlugin.getTraceHandler().trace("SerialLinePanel: Found comm port: name='" + port.getName() + "', type='" + type, ITraceIds.TRACE_SERIAL_LINE_PANEL, this); //$NON-NLS-1$ //$NON-NLS-2$
				// only add serial ports
				if (port.getPortType() == CommPortIdentifier.PORT_SERIAL) {
					UIPlugin.getTraceHandler().trace("SerialLinePanel: Adding found serial comm port to combo!", ITraceIds.TRACE_SERIAL_LINE_PANEL, this); //$NON-NLS-1$
					if (!ports.contains(port.getName())) {
						ports.add(port.getName());
					}
				}
			}
			if (!ports.isEmpty()) {
				Collections.sort(ports);
				// This method may executed in a separate thread. We must spawn back
				// into the UI thread to execute the adding of the ports to the control.
				PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
					@Override
                    public void run() {
						for (String port : ports) {
							SWTControlUtil.add(hostTTYDeviceCombo, port);
						}
					}
				});
			}
		} catch (UnsatisfiedLinkError e) {
			IStatus status = new Status(IStatus.WARNING, UIPlugin.getUniqueIdentifier(),
										Messages.SerialLinePanel_warning_FailedToLoadSerialPorts, e);
			UIPlugin.getDefault().getLog().log(status);
		} catch (NoClassDefFoundError e) {
			// The NoClassDefFoundError happens the second time if the load of the library
			// failed once! We do ignore this error completely!
		}
	}

	/**
	 * Enables or disables the configuration panels controls.
	 *
	 * @param enabled Specify <code>true</code> to enable the controls, <code>false</code> otherwise.
	 */
	@Override
    public void setEnabled(boolean enabled) {
		SWTControlUtil.setEnabled(hostTTYDeviceLabel, enabled);
		SWTControlUtil.setEnabled(hostTTYDeviceCombo, enabled);
		SWTControlUtil.setEnabled(hostTTYSpeedLabel, enabled);
		SWTControlUtil.setEnabled(hostTTYSpeedCombo, enabled);
		SWTControlUtil.setEnabled(hostTTYBitsLabel, enabled);
		SWTControlUtil.setEnabled(hostTTYBitsCombo, enabled);
		SWTControlUtil.setEnabled(hostTTYParityLabel, enabled);
		SWTControlUtil.setEnabled(hostTTYParityCombo, enabled);
		SWTControlUtil.setEnabled(hostTTYStopbitsLabel, enabled);
		SWTControlUtil.setEnabled(hostTTYStopbitsCombo, enabled);
		SWTControlUtil.setEnabled(hostTTYFlowControlLabel, enabled);
		SWTControlUtil.setEnabled(hostTTYFlowControlCombo, enabled);
	}

	/**
	 * The name of the serial ports differ between the host platforms, so we have to
	 * detect the default host TTY device based on the host platform.
	 */
	public String getDefaultHostTTYDevice() {
		String osName = System.getProperty("os.name"); //$NON-NLS-1$
		// Linux ?
		if (osName.equalsIgnoreCase("Linux")) { //$NON-NLS-1$
			return fcDefaultTTYDeviceLinux;
		}
		// Solaris ?
		if (osName.equalsIgnoreCase("SunOS")) { //$NON-NLS-1$
			return fcDefaultTTYDeviceSolaris;
		}
		// Windows ?
		if (osName.toLowerCase().startsWith("windows")) { //$NON-NLS-1$
			return fcDefaultTTYDeviceWin32;
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * Returns the default value for the serial port speed setting in bit/s
	 */
	public String getDefaultHostTTYSpeed() {
		return fcDefaultTTYSpeed;
	}

	/**
	 * Returns the default value for the serial port data bits setting
	 */
	public String getDefaultHostTTYDatabits() {
		return fcDefaultTTYDatabits;
	}

	/**
	 * Returns the default value for the serial port parity setting
	 */
	public String getDefaultHostTTYParity() {
		return fcDefaultTTYParity;
	}

	/**
	 * Returns the default value for the serial port stop bits setting
	 */
	public String getDefaultHostTTYStopbits() {
		return fcDefaultTTYStopbits;
	}

	/**
	 * Returns the default value for the serial port flow control setting
	 */
	public String getDefaultHostTTYFlowControl() {
		return fcDefaultTTYFlowControl;
	}

	/**
	 * Returns the default value for the serial port timeout setting.
	 */
	public String getDefaultHostTTYTimeout() {
		return fcDefaultTTYTimeout;
	}

	/**
	 * Set the text to the combo if available as selectable option.
	 *
	 * @param combo The combo box control. Must not be <code>null</code>.
	 * @param value The value to set. Must not be <code>null</code>.
	 */
	protected void doSetTextInCombo(Combo combo, String value) {
		Assert.isNotNull(combo);
		Assert.isNotNull(value);
		if (SWTControlUtil.indexOf(combo, value) != 1) {
			SWTControlUtil.setText(combo, value);
		}
	}

	/**
	 * Select the given tty device if available.
	 *
	 * @param value The tty device to select. Must not be <code>null</code>.
	 */
	public void setSelectedTTYDevice(String value) {
		doSetTextInCombo(hostTTYDeviceCombo, value);
	}

	/**
	 * Select the given tty device if available. The method
	 * will do nothing if the specified index is invalid.
	 *
	 * @param index The index of the tty device to select.
	 */
	public void setSelectedTTYDevice(int index) {
		if (index >= 0 && index < SWTControlUtil.getItemCount(hostTTYDeviceCombo)) {
			SWTControlUtil.setText(hostTTYDeviceCombo, SWTControlUtil.getItem(hostTTYDeviceCombo, index));
		}
	}

	/**
	 * Select the given tty device speed if available.
	 *
	 * @param value The tty device speed to select. Must not be <code>null</code>.
	 */
	public void setSelectedTTYSpeed(String value) {
		doSetTextInCombo(hostTTYSpeedCombo, value);
	}

	/**
	 * Select the given tty device data bit configuration if available.
	 *
	 * @param value The tty device data bit configuration to select. Must not be <code>null</code>.
	 */
	public void setSelectedTTYDatabits(String value) {
		doSetTextInCombo(hostTTYBitsCombo, value);
	}

	/**
	 * Select the given tty device parity configuration if available.
	 *
	 * @param value The tty device parity configuration to select. Must not be <code>null</code>.
	 */
	public void setSelectedTTYParity(String value) {
		doSetTextInCombo(hostTTYParityCombo, value);
	}

	/**
	 * Select the given tty device stop bit configuration if available.
	 *
	 * @param value The tty device stop bit configuration to select. Must not be <code>null</code>.
	 */
	public void setSelectedTTYStopbits(String value) {
		doSetTextInCombo(hostTTYStopbitsCombo, value);
	}

	/**
	 * Select the given tty device flow control configuration if available.
	 *
	 * @param value The tty device flow control configuration to select. Must not be <code>null</code>.
	 */
	public void setSelectedTTYFlowControl(String value) {
		doSetTextInCombo(hostTTYFlowControlCombo, value);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.controls.panels.AbstractWizardConfigurationPanel#isValid()
	 */
	@Override
	public boolean isValid() {
		String selectedTTYDevice = SWTControlUtil.getText(hostTTYDeviceCombo);
		if (selectedTTYDevice == null || selectedTTYDevice.trim().length() == 0) {
			setMessage(Messages.SerialLinePanel_error_emptyHostTTYDevice, IMessageProvider.ERROR);
			return false;
		}

		if (fcEditableTTYOther.equals(selectedTTYDevice)) {
			setMessage(Messages.SerialLinePanel_info_editableTTYDeviceSelected, IMessageProvider.INFORMATION);
			return false;
		}

		String selectedTTYSpeedRate = SWTControlUtil.getText(hostTTYSpeedCombo);
		if (selectedTTYSpeedRate == null || selectedTTYSpeedRate.trim().length() == 0) {
			setMessage(Messages.SerialLinePanel_error_emptyHostTTYSpeedRate, IMessageProvider.ERROR);
			return false;
		}

		if (fcEditableTTYOther.equals(selectedTTYSpeedRate)) {
			setMessage(Messages.SerialLinePanel_info_editableTTYBaudRateSelected, IMessageProvider.INFORMATION);
			return false;
		}

		if (showAdvancedSerialOptions) {
			String option = SWTControlUtil.getText(hostTTYBitsCombo);
			if (option == null || option.trim().length() == 0) {
				setMessage(Messages.SerialLinePanel_error_emptyHostTTYDatabits, IMessageProvider.ERROR);
				return false;
			}

			option = SWTControlUtil.getText(hostTTYParityCombo);
			if (option == null || option.trim().length() == 0) {
				setMessage(Messages.SerialLinePanel_error_emptyHostTTYParity, IMessageProvider.ERROR);
				return false;
			}

			option = SWTControlUtil.getText(hostTTYStopbitsCombo);
			if (option == null || option.trim().length() == 0) {
				setMessage(Messages.SerialLinePanel_error_emptyHostTTYStopbits, IMessageProvider.ERROR);
				return false;
			}

			option = SWTControlUtil.getText(hostTTYFlowControlCombo);
			if (option == null || option.trim().length() == 0) {
				setMessage(Messages.SerialLinePanel_error_emptyHostTTYFlowControl, IMessageProvider.ERROR);
				return false;
			}

			if (terminalMode) {
				option = SWTControlUtil.getText(hostTTYTimeoutText);
				if (option == null || option.trim().length() == 0) {
					setMessage(Messages.SerialLinePanel_error_emptyHostTTYFlowControl, IMessageProvider.ERROR);
					return false;
				}
			}
		}

		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.controls.interfaces.IWizardConfigurationPanel#dataChanged(org.eclipse.tcf.te.runtime.interfaces.nodes.IPropertiesContainer, org.eclipse.swt.events.TypedEvent)
	 */
	@Override
    public boolean dataChanged(IPropertiesContainer data, TypedEvent e) {
		Assert.isNotNull(data);

		boolean isDirty = false;

		if (!terminalMode) {
			Map<String, Object> container = (Map<String, Object>)data.getProperty(IWireTypeSerial.PROPERTY_CONTAINER_NAME);
			if (container == null) container = new HashMap<String, Object>();

			String value = SWTControlUtil.getText(hostTTYDeviceCombo);
			if (value != null) isDirty |= !value.equals(container.get(IWireTypeSerial.PROPERTY_SERIAL_DEVICE) != null ? container.get(IWireTypeSerial.PROPERTY_SERIAL_DEVICE) : ""); //$NON-NLS-1$

			value = SWTControlUtil.getText(hostTTYSpeedCombo);
			if (value != null) isDirty |= !value.equals(container.get(IWireTypeSerial.PROPERTY_SERIAL_BAUD_RATE) != null ? container.get(IWireTypeSerial.PROPERTY_SERIAL_BAUD_RATE) : ""); //$NON-NLS-1$

			if (showAdvancedSerialOptions) {
				value = SWTControlUtil.getText(hostTTYBitsCombo);
				if (value != null) isDirty |= !value.equals(container.get(IWireTypeSerial.PROPERTY_SERIAL_DATA_BITS) != null ? container.get(IWireTypeSerial.PROPERTY_SERIAL_DATA_BITS) : ""); //$NON-NLS-1$

				value = SWTControlUtil.getText(hostTTYParityCombo);
				if (value != null) isDirty |= !value.equals(container.get(IWireTypeSerial.PROPERTY_SERIAL_PARITY) != null ? container.get(IWireTypeSerial.PROPERTY_SERIAL_PARITY) : ""); //$NON-NLS-1$

				value = SWTControlUtil.getText(hostTTYStopbitsCombo);
				if (value != null) isDirty |= !value.equals(container.get(IWireTypeSerial.PROPERTY_SERIAL_STOP_BITS) != null ? container.get(IWireTypeSerial.PROPERTY_SERIAL_STOP_BITS) : ""); //$NON-NLS-1$

				value = SWTControlUtil.getText(hostTTYFlowControlCombo);
				if (value != null) isDirty |= !value.equals(container.get(IWireTypeSerial.PROPERTY_SERIAL_FLOW_CONTROL) != null ? container.get(IWireTypeSerial.PROPERTY_SERIAL_FLOW_CONTROL) : ""); //$NON-NLS-1$
			}
		} else {
			String value = SWTControlUtil.getText(hostTTYDeviceCombo);
			if (value != null) isDirty |= !value.equals(data.getStringProperty(ITerminalsConnectorConstants.PROP_SERIAL_DEVICE) != null ? data.getStringProperty(ITerminalsConnectorConstants.PROP_SERIAL_DEVICE) : ""); //$NON-NLS-1$

			value = SWTControlUtil.getText(hostTTYSpeedCombo);
			if (value != null) isDirty |= !value.equals(data.getStringProperty(ITerminalsConnectorConstants.PROP_SERIAL_BAUD_RATE) != null ? data.getStringProperty(ITerminalsConnectorConstants.PROP_SERIAL_BAUD_RATE) : ""); //$NON-NLS-1$

			if (showAdvancedSerialOptions) {
				value = SWTControlUtil.getText(hostTTYBitsCombo);
				if (value != null) isDirty |= !value.equals(data.getStringProperty(ITerminalsConnectorConstants.PROP_SERIAL_DATA_BITS) != null ? data.getStringProperty(ITerminalsConnectorConstants.PROP_SERIAL_DATA_BITS) : ""); //$NON-NLS-1$

				value = SWTControlUtil.getText(hostTTYParityCombo);
				if (value != null) isDirty |= !value.equals(data.getStringProperty(ITerminalsConnectorConstants.PROP_SERIAL_PARITY) != null ? data.getStringProperty(ITerminalsConnectorConstants.PROP_SERIAL_PARITY) : ""); //$NON-NLS-1$

				value = SWTControlUtil.getText(hostTTYStopbitsCombo);
				if (value != null) isDirty |= !value.equals(data.getStringProperty(ITerminalsConnectorConstants.PROP_SERIAL_STOP_BITS) != null ? data.getStringProperty(ITerminalsConnectorConstants.PROP_SERIAL_STOP_BITS) : ""); //$NON-NLS-1$

				value = SWTControlUtil.getText(hostTTYFlowControlCombo);
				if (value != null) isDirty |= !value.equals(data.getStringProperty(ITerminalsConnectorConstants.PROP_SERIAL_FLOW_CONTROL) != null ? data.getStringProperty(ITerminalsConnectorConstants.PROP_SERIAL_FLOW_CONTROL) : ""); //$NON-NLS-1$

				value = SWTControlUtil.getText(hostTTYTimeoutText);
				if (value != null) isDirty |= !value.equals(data.getStringProperty(ITerminalsConnectorConstants.PROP_TIMEOUT) != null ? data.getStringProperty(ITerminalsConnectorConstants.PROP_TIMEOUT) : ""); //$NON-NLS-1$
			}
		}

		return isDirty;
	}

	private final String fcSelectedTTYDeviceSlotId = "SerialLinePanel.selectedTTYDevice." + System.getProperty("os.name"); //$NON-NLS-1$ //$NON-NLS-2$
	private final String fcSelectedTTYSpeedRateSlotId = "SerialLinePanel.selectedTTYSpeedRate." + System.getProperty("os.name"); //$NON-NLS-1$ //$NON-NLS-2$
	private final String fcSelectedTTYDatabitsSlotId = "SerialLinePanel.selectedTTYDatabits." + System.getProperty("os.name"); //$NON-NLS-1$ //$NON-NLS-2$
	private final String fcSelectedTTYParitySlotId = "SerialLinePanel.selectedTTYParity." + System.getProperty("os.name"); //$NON-NLS-1$ //$NON-NLS-2$
	private final String fcSelectedTTYStopbitsSlotId = "SerialLinePanel.selectedTTYStopbits." + System.getProperty("os.name"); //$NON-NLS-1$ //$NON-NLS-2$
	private final String fcSelectedTTYFlowControlSlotId = "SerialLinePanel.selectedTTYFlowControl." + System.getProperty("os.name"); //$NON-NLS-1$ //$NON-NLS-2$
	private final String fcSelectedTTYTimeoutSlotId = "SerialLinePanel.selectedTTYTimeout." + System.getProperty("os.name"); //$NON-NLS-1$ //$NON-NLS-2$

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.controls.panels.AbstractWizardConfigurationPanel#doRestoreWidgetValues(org.eclipse.jface.dialogs.IDialogSettings, java.lang.String)
	 */
	@Override
	public void doRestoreWidgetValues(IDialogSettings settings, String idPrefix) {
		Assert.isNotNull(settings);

		String selectedTTYDevice = settings.get(getParentControl().prefixDialogSettingsSlotId(fcSelectedTTYDeviceSlotId, idPrefix));
		if (selectedTTYDevice != null && selectedTTYDevice.trim().length() > 0) {
			if (SWTControlUtil.indexOf(hostTTYDeviceCombo, selectedTTYDevice) != -1) {
				SWTControlUtil.setText(hostTTYDeviceCombo, selectedTTYDevice);
			}
		}

		String selectedTTYSpeedRate = settings.get(getParentControl().prefixDialogSettingsSlotId(fcSelectedTTYSpeedRateSlotId, idPrefix));
		if (selectedTTYSpeedRate != null && selectedTTYSpeedRate.trim().length() > 0) {
			if (SWTControlUtil.indexOf(hostTTYSpeedCombo, selectedTTYSpeedRate) != -1) {
				SWTControlUtil.setText(hostTTYSpeedCombo, selectedTTYSpeedRate);
			}
		}

		if (showAdvancedSerialOptions) {
			String option = settings.get(getParentControl().prefixDialogSettingsSlotId(fcSelectedTTYDatabitsSlotId, idPrefix));
			if (option != null && option.trim().length() > 0 && SWTControlUtil.indexOf(hostTTYBitsCombo, option) != -1) {
				SWTControlUtil.setText(hostTTYBitsCombo, option);
			}

			option = settings.get(getParentControl().prefixDialogSettingsSlotId(fcSelectedTTYParitySlotId, idPrefix));
			if (option != null && option.trim().length() > 0 && SWTControlUtil.indexOf(hostTTYParityCombo, option) != -1) {
				SWTControlUtil.setText(hostTTYParityCombo, option);
			}

			option = settings.get(getParentControl().prefixDialogSettingsSlotId(fcSelectedTTYStopbitsSlotId, idPrefix));
			if (option != null && option.trim().length() > 0 && SWTControlUtil.indexOf(hostTTYStopbitsCombo, option) != -1) {
				SWTControlUtil.setText(hostTTYStopbitsCombo, option);
			}

			option = settings.get(getParentControl().prefixDialogSettingsSlotId(fcSelectedTTYFlowControlSlotId, idPrefix));
			if (option != null && option.trim().length() > 0 && SWTControlUtil.indexOf(hostTTYFlowControlCombo, option) != -1) {
				SWTControlUtil.setText(hostTTYFlowControlCombo, option);
			}

			if (terminalMode) {
				option = settings.get(getParentControl().prefixDialogSettingsSlotId(fcSelectedTTYTimeoutSlotId, idPrefix));
				if (option != null && option.trim().length() > 0 && SWTControlUtil.indexOf(hostTTYTimeoutText, option) != -1) {
					SWTControlUtil.setText(hostTTYTimeoutText, option);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.controls.panels.AbstractWizardConfigurationPanel#doSaveWidgetValues(org.eclipse.jface.dialogs.IDialogSettings, java.lang.String)
	 */
	@Override
	public void doSaveWidgetValues(IDialogSettings settings, String idPrefix) {
		Assert.isNotNull(settings);

		settings.put(getParentControl().prefixDialogSettingsSlotId(fcSelectedTTYDeviceSlotId, idPrefix), SWTControlUtil.getText(hostTTYDeviceCombo));
		settings.put(getParentControl().prefixDialogSettingsSlotId(fcSelectedTTYSpeedRateSlotId, idPrefix), SWTControlUtil.getText(hostTTYSpeedCombo));

		if (showAdvancedSerialOptions) {
			settings.put(getParentControl().prefixDialogSettingsSlotId(fcSelectedTTYDatabitsSlotId, idPrefix), SWTControlUtil.getText(hostTTYBitsCombo));
			settings.put(getParentControl().prefixDialogSettingsSlotId(fcSelectedTTYParitySlotId, idPrefix), SWTControlUtil.getText(hostTTYParityCombo));
			settings.put(getParentControl().prefixDialogSettingsSlotId(fcSelectedTTYStopbitsSlotId, idPrefix), SWTControlUtil.getText(hostTTYStopbitsCombo));
			settings.put(getParentControl().prefixDialogSettingsSlotId(fcSelectedTTYFlowControlSlotId, idPrefix), SWTControlUtil.getText(hostTTYFlowControlCombo));

			if (terminalMode) {
				settings.put(getParentControl().prefixDialogSettingsSlotId(fcSelectedTTYTimeoutSlotId, idPrefix), SWTControlUtil.getText(hostTTYTimeoutText));
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.wizards.interfaces.ISharedDataExchangeNode#setupData(org.eclipse.tcf.te.runtime.interfaces.nodes.IPropertiesContainer)
	 */
	@Override
    public void setupData(IPropertiesContainer data) {
		if (data == null) return;

		if (!terminalMode) {
			Map<String, Object> container = (Map<String, Object>)data.getProperty(IWireTypeSerial.PROPERTY_CONTAINER_NAME);
			if (container == null) container = new HashMap<String, Object>();

			SWTControlUtil.setText(hostTTYDeviceCombo, (String)container.get(IWireTypeSerial.PROPERTY_SERIAL_DEVICE));
			SWTControlUtil.setText(hostTTYSpeedCombo, (String)container.get(IWireTypeSerial.PROPERTY_SERIAL_BAUD_RATE));

			if (showAdvancedSerialOptions) {
				SWTControlUtil.setText(hostTTYBitsCombo, (String)container.get(IWireTypeSerial.PROPERTY_SERIAL_DATA_BITS));
				SWTControlUtil.setText(hostTTYParityCombo, (String)container.get(IWireTypeSerial.PROPERTY_SERIAL_PARITY));
				SWTControlUtil.setText(hostTTYStopbitsCombo, (String)container.get(IWireTypeSerial.PROPERTY_SERIAL_STOP_BITS));
				SWTControlUtil.setText(hostTTYFlowControlCombo, (String)container.get(IWireTypeSerial.PROPERTY_SERIAL_FLOW_CONTROL));
			}
		} else {
			// In terminal mode, read the properties directly from the given properties container
			// and use the constants from ITerminalConnectorConstants!
			SWTControlUtil.setText(hostTTYDeviceCombo, data.getStringProperty(ITerminalsConnectorConstants.PROP_SERIAL_DEVICE));
			SWTControlUtil.setText(hostTTYSpeedCombo, data.getStringProperty(ITerminalsConnectorConstants.PROP_SERIAL_BAUD_RATE));

			if (showAdvancedSerialOptions) {
				SWTControlUtil.setText(hostTTYBitsCombo, data.getStringProperty(ITerminalsConnectorConstants.PROP_SERIAL_DATA_BITS));
				SWTControlUtil.setText(hostTTYParityCombo, data.getStringProperty(ITerminalsConnectorConstants.PROP_SERIAL_PARITY));
				SWTControlUtil.setText(hostTTYStopbitsCombo, data.getStringProperty(ITerminalsConnectorConstants.PROP_SERIAL_STOP_BITS));
				SWTControlUtil.setText(hostTTYFlowControlCombo, data.getStringProperty(ITerminalsConnectorConstants.PROP_SERIAL_FLOW_CONTROL));
				SWTControlUtil.setText(hostTTYTimeoutText, data.getStringProperty(ITerminalsConnectorConstants.PROP_TIMEOUT));
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.wizards.interfaces.ISharedDataExchangeNode#extractData(org.eclipse.tcf.te.runtime.interfaces.nodes.IPropertiesContainer)
	 */
	@Override
    public void extractData(IPropertiesContainer data) {
		if (data == null) return;

		if (!terminalMode) {
			Map<String, Object> container = (Map<String, Object>)data.getProperty(IWireTypeSerial.PROPERTY_CONTAINER_NAME);
			if (container == null) container = new HashMap<String, Object>();

			container.put(IWireTypeSerial.PROPERTY_SERIAL_DEVICE, SWTControlUtil.getText(hostTTYDeviceCombo));
			container.put(IWireTypeSerial.PROPERTY_SERIAL_BAUD_RATE, SWTControlUtil.getText(hostTTYSpeedCombo));

			container.put(IWireTypeSerial.PROPERTY_SERIAL_DATA_BITS, showAdvancedSerialOptions ? SWTControlUtil.getText(hostTTYBitsCombo) : null);
			container.put(IWireTypeSerial.PROPERTY_SERIAL_PARITY, showAdvancedSerialOptions ? SWTControlUtil.getText(hostTTYParityCombo) : null);
			container.put(IWireTypeSerial.PROPERTY_SERIAL_STOP_BITS, showAdvancedSerialOptions ? SWTControlUtil.getText(hostTTYStopbitsCombo) : null);
			container.put(IWireTypeSerial.PROPERTY_SERIAL_FLOW_CONTROL, showAdvancedSerialOptions ? SWTControlUtil.getText(hostTTYFlowControlCombo) : null);

			data.setProperty(IWireTypeSerial.PROPERTY_CONTAINER_NAME, !container.isEmpty() ? container : null);
		} else {
			// In terminal mode, write the properties directly to the given properties container
			// and use the constants from ITerminalConnectorConstants!
			data.setProperty(ITerminalsConnectorConstants.PROP_SERIAL_DEVICE, SWTControlUtil.getText(hostTTYDeviceCombo));
			data.setProperty(ITerminalsConnectorConstants.PROP_SERIAL_BAUD_RATE, SWTControlUtil.getText(hostTTYSpeedCombo));

			data.setProperty(ITerminalsConnectorConstants.PROP_SERIAL_DATA_BITS, showAdvancedSerialOptions ? SWTControlUtil.getText(hostTTYBitsCombo) : null);
			data.setProperty(ITerminalsConnectorConstants.PROP_SERIAL_PARITY, showAdvancedSerialOptions ? SWTControlUtil.getText(hostTTYParityCombo) : null);
			data.setProperty(ITerminalsConnectorConstants.PROP_SERIAL_STOP_BITS, showAdvancedSerialOptions ? SWTControlUtil.getText(hostTTYStopbitsCombo) : null);
			data.setProperty(ITerminalsConnectorConstants.PROP_SERIAL_FLOW_CONTROL, showAdvancedSerialOptions ? SWTControlUtil.getText(hostTTYFlowControlCombo) : null);

			data.setProperty(ITerminalsConnectorConstants.PROP_TIMEOUT, showAdvancedSerialOptions ? SWTControlUtil.getText(hostTTYTimeoutText) : null);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.interfaces.data.IDataExchangeNode2#initializeData(org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer)
	 */
	@Override
    public void initializeData(IPropertiesContainer data) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.interfaces.data.IDataExchangeNode3#removeData(org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer)
	 */
	@Override
    public void removeData(IPropertiesContainer data) {
		if (data == null) return;
		data.setProperty(IWireTypeSerial.PROPERTY_CONTAINER_NAME, null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.interfaces.data.IDataExchangeNode3#copyData(org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer)
	 */
	@Override
	public void copyData(IPropertiesContainer src, IPropertiesContainer dst) {
		Assert.isNotNull(src);
		Assert.isNotNull(dst);

        Map<String, Object> srcContainer = (Map<String, Object>)src.getProperty(IWireTypeSerial.PROPERTY_CONTAINER_NAME);
        Map<String, Object> dstContainer = null;

        if (srcContainer != null) {
        	dstContainer = new HashMap<String, Object>(srcContainer);
        }

        dst.setProperty(IWireTypeSerial.PROPERTY_CONTAINER_NAME, dstContainer);
	}
}
