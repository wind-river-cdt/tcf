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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.tcf.te.ui.views.internal.preferences.IPreferenceConsts;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.INavigatorContentDescriptor;
import org.eclipse.ui.navigator.INavigatorContentService;

/**
 * The dynamic contribution of content extension MRU menu list. 
 */
public class ContentMRUContribution extends CompoundContributionItem {

	/**
	 * A MRU item action to enable or disable specified content extension.
	 */
	static class ContentMRUAction extends Action {
		// The content service of the navigator.
		private INavigatorContentService contentService;
		// The content extension descriptor to be configured by this action.
		private INavigatorContentDescriptor contentDescriptor;
		// The common viewer of the navigator.
		private CommonViewer commonViewer;

		/**
		 * Constructor
		 */
		public ContentMRUAction(INavigatorContentDescriptor contentDescriptor, INavigatorContentService contentService, CommonViewer commonViewer) {
			super(contentDescriptor.getName(), AS_CHECK_BOX);
			this.contentDescriptor = contentDescriptor;
			this.contentService = contentService;
			this.commonViewer = commonViewer;
			setChecked(contentService.isActive(contentDescriptor.getId()));
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.action.Action#run()
		 */
		@Override
		public void run() {
			Set<String> activeIds = new HashSet<String>();
			String[] visibleIds = contentService.getVisibleExtensionIds();
			if (visibleIds != null) {
				for (String visibleId : visibleIds) {
					if (contentService.isActive(visibleId)) activeIds.add(visibleId);
				}
			}
			if (isChecked()) activeIds.add(contentDescriptor.getId());
			else activeIds.remove(contentDescriptor.getId());
			String[] idsToActivate = activeIds.toArray(new String[activeIds.size()]);
			UpdateActiveExtensionsOperation updateExtensions = new UpdateActiveExtensionsOperation(commonViewer, idsToActivate);
			updateExtensions.execute(null, null);
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
		List<IContributionItem> items = new ArrayList<IContributionItem>();
		List<String> extensionSet = new MRUList(IPreferenceConsts.PREF_CONTENT_MRU_LIST);
		CommonViewer commonViewer = navigator.getCommonViewer();
		for (String extensionId : extensionSet) {
			INavigatorContentDescriptor contentDescriptor = contentService.getContentDescriptorById(extensionId);
			items.add(new ActionContributionItem(new ContentMRUAction(contentDescriptor, contentService, commonViewer)));
		}
		return items.toArray(new IContributionItem[items.size()]);
	}
}
