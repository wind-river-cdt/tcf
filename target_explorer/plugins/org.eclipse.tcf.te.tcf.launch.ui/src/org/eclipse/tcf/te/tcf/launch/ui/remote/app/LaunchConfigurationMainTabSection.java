/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.launch.ui.remote.app;

import org.eclipse.core.runtime.Assert;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tcf.te.launch.core.persistence.DefaultPersistenceDelegate;
import org.eclipse.tcf.te.launch.core.persistence.launchcontext.LaunchContextsPersistenceDelegate;
import org.eclipse.tcf.te.launch.ui.interfaces.ILaunchConfigurationTabFormPart;
import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.filesystem.ui.dialogs.FSOpenFileDialog;
import org.eclipse.tcf.te.tcf.launch.core.interfaces.IRemoteAppLaunchAttributes;
import org.eclipse.tcf.te.tcf.launch.ui.nls.Messages;
import org.eclipse.tcf.te.ui.controls.BaseEditBrowseTextControl;
import org.eclipse.tcf.te.ui.forms.parts.AbstractSection;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * Remote application launch configuration main tab section implementation.
 */
public class LaunchConfigurationMainTabSection extends AbstractSection implements ILaunchConfigurationTabFormPart {

	BaseEditBrowseTextControl processImage;
	BaseEditBrowseTextControl processArguments;
	IModelNode firstSelection = null;

	/**
	 * Constructor.
	 *
	 * @param form The parent managed form. Must not be <code>null</code>.
	 * @param parent The parent composite. Must not be <code>null</code>.
	 */
	public LaunchConfigurationMainTabSection(IManagedForm form, Composite parent) {
		super(form, parent, ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED);
		getSection().setBackground(parent.getBackground());
		createClient(getSection(), form.getToolkit());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.forms.parts.AbstractSection#createClient(org.eclipse.ui.forms.widgets.Section, org.eclipse.ui.forms.widgets.FormToolkit)
	 */
	@Override
	protected void createClient(final Section section, FormToolkit toolkit) {
		Assert.isNotNull(section);
		Assert.isNotNull(toolkit);

		// Configure the section
		section.setText(Messages.LaunchConfigurationMainTabSection_title);
		section.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL, SWT.CENTER, true, false));

		// Create the section client
		Composite client = createClientContainer(section, 3, toolkit);
		Assert.isNotNull(client);
		section.setClient(client);
		client.setBackground(section.getBackground());

		// Create a toolbar for the section
		createSectionToolbar(section, toolkit);

		// Create the section sub controls
		processImage = new BaseEditBrowseTextControl(null) {
			@Override
			protected void onButtonControlSelected() {
				if (firstSelection != null) {
					FSOpenFileDialog dialog = new FSOpenFileDialog(section.getShell());
					dialog.setInput(firstSelection);
					if (dialog.open() == Window.OK) {
						Object candidate = dialog.getFirstResult();
						if (candidate instanceof FSTreeNode) {
							String absPath = ((FSTreeNode) candidate).getLocation();
							if (absPath != null) {
								processImage.setEditFieldControlText(absPath);
							}
						}
					}
				}
			}
			@Override
			public void modifyText(ModifyEvent e) {
				super.modifyText(e);
				getManagedForm().dirtyStateChanged();
			}
		};
		processImage.setEditFieldLabel(Messages.ProcessImageSelectorControl_label);
		processImage.setIsGroup(false);
		processImage.setHideBrowseButton(false);
		processImage.setAdjustBackgroundColor(true);
		processImage.setParentControlIsInnerPanel(true);
		processImage.setFormToolkit(toolkit);
		processImage.setupPanel(client);
		processImage.doCreateControlDecoration(processImage.getEditFieldControl());

		processArguments = new BaseEditBrowseTextControl(null) {
			@Override
			public void modifyText(ModifyEvent e) {
				super.modifyText(e);
				getManagedForm().dirtyStateChanged();
			}
		};
		processArguments.setEditFieldLabel(Messages.LaunchConfigurationMainTabSection_processArguments_label);
		processArguments.setIsGroup(false);
		processArguments.setHideBrowseButton(true);
		processArguments.setAdjustBackgroundColor(true);
		processArguments.setParentControlIsInnerPanel(true);
		processArguments.setFormToolkit(toolkit);
		processArguments.setupPanel(client);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.ui.interfaces.ILaunchConfigurationTabFormPart#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		Assert.isNotNull(configuration);

		if (processImage != null) {
			String image = DefaultPersistenceDelegate.getAttribute(configuration, IRemoteAppLaunchAttributes.ATTR_PROCESS_IMAGE, ""); //$NON-NLS-1$
			processImage.setEditFieldControlText(image);
		}

		if (processArguments != null) {
			String arguments = DefaultPersistenceDelegate.getAttribute(configuration, IRemoteAppLaunchAttributes.ATTR_PROCESS_ARGUMENTS, ""); //$NON-NLS-1$
			processArguments.setEditFieldControlText(arguments);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.ui.interfaces.ILaunchConfigurationTabFormPart#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		Assert.isNotNull(configuration);

		if (processImage != null) {
			String image = processImage.getEditFieldControlText();

			if (image != null && image.trim().length() > 0) {
				DefaultPersistenceDelegate.setAttribute(configuration, IRemoteAppLaunchAttributes.ATTR_PROCESS_IMAGE, image);
			} else {
				DefaultPersistenceDelegate.setAttribute(configuration, IRemoteAppLaunchAttributes.ATTR_PROCESS_IMAGE, (String)null);
			}
		} else {
			DefaultPersistenceDelegate.setAttribute(configuration, IRemoteAppLaunchAttributes.ATTR_PROCESS_IMAGE, (String)null);
		}

		if (processArguments != null) {
			String arguments = processArguments.getEditFieldControlText();

			if (arguments != null && arguments.trim().length() > 0) {
				DefaultPersistenceDelegate.setAttribute(configuration, IRemoteAppLaunchAttributes.ATTR_PROCESS_ARGUMENTS, arguments);
			} else {
				DefaultPersistenceDelegate.setAttribute(configuration, IRemoteAppLaunchAttributes.ATTR_PROCESS_ARGUMENTS, (String)null);
			}
		} else {
			DefaultPersistenceDelegate.setAttribute(configuration, IRemoteAppLaunchAttributes.ATTR_PROCESS_ARGUMENTS, (String)null);
		}
	}

	@Override
	public boolean isValid(ILaunchConfiguration configuration) {
		firstSelection = null;
		IModelNode[] contexts = LaunchContextsPersistenceDelegate.getLaunchContexts(configuration);
		if (contexts != null && contexts.length > 0) {
			firstSelection = contexts[0];
		}
		processImage.getButtonControl().setEnabled(firstSelection != null);

		if (processImage.getEditFieldControlText().trim().length() > 0) {
			setMessage(null, IMessageProvider.NONE);
		}
		else {
			setMessage(Messages.ProcessImageSelectorControl_error_missingProcessImage, IMessageProvider.ERROR);
		}
		processImage.updateControlDecoration(getMessage(), getMessageType());

		return processImage.getEditFieldControlText().trim().length() > 0;
	}
}
