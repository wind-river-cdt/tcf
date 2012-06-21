/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.launch.ui.launchcontext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tcf.te.launch.core.bindings.LaunchConfigTypeBindingsManager;
import org.eclipse.tcf.te.launch.core.selection.RemoteSelectionContext;
import org.eclipse.tcf.te.launch.ui.tabs.launchcontext.AbstractContextSelectorControl;
import org.eclipse.tcf.te.launch.ui.tabs.launchcontext.AbstractContextSelectorTab;
import org.eclipse.tcf.te.tcf.launch.ui.launchcontext.ContextSelectorSection.ContextSelectorSectionSelectorControl;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.ui.forms.IManagedForm;

/**
 * Launch context selection main launch tab implementation.
 */
public abstract class AbstractLaunchContextMainTab extends AbstractContextSelectorTab {
	/* default */ ILaunchConfiguration configuration = null;

	/**
	 * Context selector control filter filtering remote contexts which are not
	 * enabled for the launch configuration type.
	 */
	protected class MainTabContextSelectorViewerFilter extends ViewerFilter {

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if (element instanceof IPeerModel) {
				String typeId = null;
				if (configuration != null) {
					try {
						typeId = configuration.getType().getIdentifier();
					}
					catch (CoreException e) { /* ignored on purpose */ }
				}

				String mode = getLaunchConfigurationDialog().getMode();

				if (typeId != null && mode != null) {
					return LaunchConfigTypeBindingsManager.getInstance().isValidLaunchConfigType(typeId, mode, new RemoteSelectionContext((IPeerModel)element, true));
				}
			}
			return true;
		}
	}

	/**
	 * Launch configuration main tab context selector control implementation.
	 */
	protected class MainTabContextSelectorControl extends ContextSelectorSectionSelectorControl {

		/**
		 * Constructor.
		 *
		 * @param section The parent context selector section. Must not be <code>null</code>.
		 * @param parentPage The parent target connection page this control is embedded in. Might be
		 *            <code>null</code> if the control is not associated with a page.
		 */
		public MainTabContextSelectorControl(ContextSelectorSection section, IDialogPage parentPage) {
			super(section, parentPage);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.tcf.te.launch.ui.tabs.launchcontext.AbstractContextSelectorControl#doCreateViewerFilters()
		 */
		@Override
		protected ViewerFilter[] doCreateViewerFilters() {
			List<ViewerFilter> filters = new ArrayList<ViewerFilter>(Arrays.asList(super.doCreateViewerFilters()));
			filters.add(new MainTabContextSelectorViewerFilter());
			return filters.toArray(new ViewerFilter[filters.size()]);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.ui.tabs.AbstractFormsLaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		super.initializeFrom(configuration);
		this.configuration = configuration;

		if (getContextSelectorSection() != null) {
			AbstractContextSelectorControl control = (AbstractContextSelectorControl)getContextSelectorSection().getAdapter(AbstractContextSelectorControl.class);
			if (control != null && control.getViewer() != null) {
				control.getViewer().refresh();
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.ui.tabs.launchcontext.AbstractContextSelectorTab#doCreateContextSelectorSection(org.eclipse.ui.forms.IManagedForm, org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected ContextSelectorSection doCreateContextSelectorSection(IManagedForm form, Composite panel) {
		return new ContextSelectorSection(form, panel) {

			/* (non-Javadoc)
			 * @see org.eclipse.tcf.te.tcf.launch.ui.launchcontext.ContextSelectorSection#doCreateContextSelector()
			 */
			@Override
			protected AbstractContextSelectorControl doCreateContextSelector() {
				AbstractContextSelectorControl control = new MainTabContextSelectorControl(this, null);
				doConfigureContextSelectorControl(control);
				return control;
			}
		};
	}

	/**
	 * Configure the context selector control.
	 * @param control The context selector control.
	 */
	protected void doConfigureContextSelectorControl(AbstractContextSelectorControl control) {
	}
}
