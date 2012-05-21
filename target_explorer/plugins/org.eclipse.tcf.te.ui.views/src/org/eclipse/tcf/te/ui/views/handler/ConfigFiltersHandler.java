/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.views.handler;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tcf.te.ui.dialogs.FilteredCheckedListDialog;
import org.eclipse.tcf.te.ui.views.nls.Messages;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.navigator.NavigatorFilterService;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.ICommonFilterDescriptor;
import org.eclipse.ui.navigator.IDescriptionProvider;
import org.eclipse.ui.navigator.INavigatorContentService;

/**
 * The handler to configure filters in Target Explorer.
 */
@SuppressWarnings("restriction")
public class ConfigFiltersHandler extends AbstractHandler {

	/**
	 * The label provider for common filter descriptors to provide labels, images, and descriptions for
	 * a common filter descriptor.
	 */
	class CommonFilterDescriptorLabelProvider extends LabelProvider implements IDescriptionProvider {

		/* (non-Javadoc)
		 * @see org.eclipse.ui.navigator.IDescriptionProvider#getDescription(java.lang.Object)
		 */
		@Override
        public String getDescription(Object element) {
			if (element instanceof ICommonFilterDescriptor) {
				return ((ICommonFilterDescriptor) element).getDescription();
			}
			return null;
        }

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
		 */
		@Override
        public String getText(Object element) {
			if (element instanceof ICommonFilterDescriptor) {
				return ((ICommonFilterDescriptor) element).getName();
			}
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = HandlerUtil.getActiveShellChecked(event);
		CommonNavigator navigator = (CommonNavigator) HandlerUtil.getActivePartChecked(event);
		FilteredCheckedListDialog filterDialog = new FilteredCheckedListDialog(shell);
		filterDialog.setTitle(Messages.ConfigFiltersHandler_DialogTitle);
		filterDialog.setFilterText(Messages.ConfigFiltersHandler_InitialFilter);
		filterDialog.setMessage(Messages.ConfigFiltersHandler_PromptMessage);
		filterDialog.setStatusLineAboveButtons(true);
		filterDialog.setLabelProvider(new CommonFilterDescriptorLabelProvider());
		INavigatorContentService contentService = navigator.getNavigatorContentService();
		if (contentService != null) {
			NavigatorFilterService filterService = (NavigatorFilterService) contentService.getFilterService();
			ICommonFilterDescriptor[] visibleFilters = filterService.getVisibleFilterDescriptorsForUI();
			if (visibleFilters != null && visibleFilters.length > 0) {
				filterDialog.setElements(visibleFilters);
				List<ICommonFilterDescriptor> activeFilters = new ArrayList<ICommonFilterDescriptor>();
				for (ICommonFilterDescriptor filter : visibleFilters) {
					if (filterService.isActive(filter.getId())) {
						activeFilters.add(filter);
					}
				}
				filterDialog.setInitialElementSelections(activeFilters);
				if (filterDialog.open() == Window.OK) {
					Object[] result = filterDialog.getResult();
					if (result != null) {
						activateFilters(navigator.getCommonViewer(), result);
					}
				}
			}
		}
		return null;
	}

	/**
	 * Activate the specified common filters specified in the element array.
	 *
	 * @param commonViewer The common viewer to be updated with the viewer filters.
	 * @param elements The common filters to be activated.
	 */
	private void activateFilters(CommonViewer commonViewer, Object[] elements) {
		String[] filterIdsToActivate = new String[elements.length];
		for (int i=0;i<elements.length;i++) {
			ICommonFilterDescriptor descriptor = (ICommonFilterDescriptor) elements[i];
			filterIdsToActivate[i] = descriptor.getId();
		}
		UpdateActiveFiltersOperation updateFilters = new UpdateActiveFiltersOperation(commonViewer, filterIdsToActivate);
		updateFilters.execute(null, null);
    }
}
