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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tcf.te.launch.core.persistence.ContextSelectorPersistenceDelegate;
import org.eclipse.tcf.te.launch.ui.activator.UIPlugin;
import org.eclipse.tcf.te.launch.ui.interfaces.ILaunchConfigurationTabFormPart;
import org.eclipse.tcf.te.launch.ui.internal.ImageConsts;
import org.eclipse.tcf.te.launch.ui.nls.Messages;
import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;
import org.eclipse.tcf.te.ui.forms.parts.AbstractSection;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * Context selector section implementation.
 */
public class LaunchContextSelectorSection extends AbstractSection implements ILaunchConfigurationTabFormPart {
	// Reference to the section sub controls
	/* default */ LaunchContextSelectorControl selector;

	/**
	 * Context selector control refresh action implementation.
	 */
	protected class RefreshAction extends Action {

		/**
		 * Constructor.
		 */
		public RefreshAction() {
			super(null, IAction.AS_PUSH_BUTTON);
			setImageDescriptor(UIPlugin.getImageDescriptor(ImageConsts.ACTION_Refresh_Enabled));
			setToolTipText(Messages.ContextSelectorControl_toolbar_refresh_tooltip);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.action.Action#run()
		 */
		@Override
		public void run() {
			if (selector != null && selector.getViewer() != null) {
				selector.getViewer().refresh();
			}
		}
	}

	/**
	 * Constructor.
	 *
	 * @param form The parent managed form. Must not be <code>null</code>.
	 * @param parent The parent composite. Must not be <code>null</code>.
	 */
	public LaunchContextSelectorSection(IManagedForm form, Composite parent) {
		super(form, parent, ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED);
		getSection().setBackground(parent.getBackground());
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
		section.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		// Create the section client
		Composite client = createClientContainer(section, 1, toolkit);
		Assert.isNotNull(client);
		section.setClient(client);
		client.setBackground(section.getBackground());

		// Create a toolbar for the section
		createSectionToolbar(section, toolkit);

		// Create the section sub controls
		selector = new LaunchContextSelectorControl(null) {
			/* (non-Javadoc)
			 * @see org.eclipse.tcf.te.launch.ui.tabs.selector.LaunchContextSelectorControl#onModelNodeCheckStateChanged(org.eclipse.tcf.te.runtime.model.interfaces.IModelNode, boolean)
			 */
			@Override
			protected void onModelNodeCheckStateChanged(IModelNode node, boolean checked) {
				getManagedForm().dirtyStateChanged();
			}
		};
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

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.forms.parts.AbstractSection#createSectionToolbarItems(org.eclipse.ui.forms.widgets.Section, org.eclipse.ui.forms.widgets.FormToolkit, org.eclipse.jface.action.ToolBarManager)
	 */
	@Override
	protected void createSectionToolbarItems(Section section, FormToolkit toolkit, ToolBarManager tlbMgr) {
		super.createSectionToolbarItems(section, toolkit, tlbMgr);
		tlbMgr.add(new RefreshAction());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.ui.interfaces.ILaunchConfigurationTabFormPart#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		Assert.isNotNull(configuration);

		if (selector != null) {
			IModelNode[] contexts = ContextSelectorPersistenceDelegate.getLaunchContexts(configuration);
			if (contexts != null && contexts.length > 0) {
				// Loop the contexts and create a list of nodes
				List<IModelNode> nodes = new ArrayList<IModelNode>();
				for (IModelNode node : contexts) {
					if (node != null && !nodes.contains(node)) {
						nodes.add(node);
					}
				}
				if (!nodes.isEmpty()) {
					selector.setCheckedModelContexts(nodes.toArray(new IModelNode[nodes.size()]));
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.ui.interfaces.ILaunchConfigurationTabFormPart#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		Assert.isNotNull(configuration);

		if (selector != null) {
			IModelNode[] nodes = selector.getCheckedModelContexts();

			// Write the selected contexts to the launch configuration
			if (nodes != null && nodes.length > 0) {
				ContextSelectorPersistenceDelegate.setLaunchContexts(configuration, nodes);
			} else {
				ContextSelectorPersistenceDelegate.setLaunchContexts(configuration, null);
			}
		} else {
			ContextSelectorPersistenceDelegate.setLaunchContexts(configuration, null);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.ui.interfaces.ILaunchConfigurationTabFormPart#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public boolean isValid(ILaunchConfiguration configuration) {
		boolean valid = super.isValid();

		if (valid) {
			valid = selector.isValid();
			if (!valid) {
				setMessage(selector.getMessage(), selector.getMessageType());
			}
		}

		return valid;
	}
}
