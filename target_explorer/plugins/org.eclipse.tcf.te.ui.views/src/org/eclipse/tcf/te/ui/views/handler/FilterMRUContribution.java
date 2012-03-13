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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.tcf.te.ui.views.internal.preferences.IPreferenceConsts;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.internal.navigator.NavigatorFilterService;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.ICommonFilterDescriptor;
import org.eclipse.ui.navigator.INavigatorContentService;

/**
 * The dynamic contribution of common filter MRU menu list. 
 */
@SuppressWarnings("restriction")
public class FilterMRUContribution extends CompoundContributionItem {
	
	/**
	 * A MRU item action to enable or disable specified common filter.
	 */
	static class FilterMRUAction extends Action {
		// The filter service used to enable or disable this filter.
		private NavigatorFilterService filterService;
		// This filter's descriptor
		private ICommonFilterDescriptor filterDescriptor;
		// The common viewer of the navigator.
		private CommonViewer commonViewer;
		
		/**
		 * Constructor
		 */
		public FilterMRUAction(ICommonFilterDescriptor filterDescriptor, NavigatorFilterService filterService, CommonViewer commonViewer) {
			super(filterDescriptor.getName(), AS_CHECK_BOX);
			this.filterDescriptor = filterDescriptor;
			this.filterService = filterService;
			this.commonViewer = commonViewer;
			setChecked(filterService.isActive(filterDescriptor.getId()));
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.action.Action#run()
		 */
		@Override
        public void run() {
			ICommonFilterDescriptor[] visibleFilters = filterService.getVisibleFilterDescriptorsForUI();
			Set<String> activeIds = new HashSet<String>();
			if (visibleFilters != null && visibleFilters.length > 0) {
				for (ICommonFilterDescriptor filter : visibleFilters) {
					if (filterService.isActive(filter.getId())) activeIds.add(filter.getId());
				}
			}
			if (isChecked()) activeIds.add(filterDescriptor.getId());
			else activeIds.remove(filterDescriptor.getId());
			UpdateActiveFiltersOperation updateFilters = new UpdateActiveFiltersOperation(commonViewer, activeIds.toArray(new String[activeIds.size()]));
			updateFilters.execute(null, null);
        }
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.actions.CompoundContributionItem#getContributionItems()
	 */
	@Override
    protected IContributionItem[] getContributionItems() {
		CommonNavigator navigator = (CommonNavigator) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
		INavigatorContentService contentService = navigator.getNavigatorContentService();
        NavigatorFilterService filterService = (NavigatorFilterService) contentService.getFilterService();
		Map<String, ICommonFilterDescriptor> fdMap = new HashMap<String, ICommonFilterDescriptor>();
		ICommonFilterDescriptor[] visibleFilterDescriptors = filterService.getVisibleFilterDescriptorsForUI();
		for(ICommonFilterDescriptor filterDescriptor : visibleFilterDescriptors) {
			fdMap.put(filterDescriptor.getId(), filterDescriptor);
		}
		List<IContributionItem> items = new ArrayList<IContributionItem>();
		List<String> filterSet = new MRUList(IPreferenceConsts.PREF_FILTER_MRU_LIST);
		CommonViewer commonViewer = navigator.getCommonViewer();
	    for(String filterId : filterSet) {
	    	ICommonFilterDescriptor filterDescriptor = fdMap.get(filterId);
	    	items.add(new ActionContributionItem(new FilterMRUAction(filterDescriptor, filterService, commonViewer)));
	    }
	    return items.toArray(new IContributionItem[items.size()]);
    }
}
