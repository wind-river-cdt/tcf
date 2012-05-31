/*******************************************************************************
 * Copyright (c) 2006, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.tcf.te.ui.views.handler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.tcf.te.ui.views.internal.preferences.IPreferenceKeys;
import org.eclipse.tcf.te.ui.views.nls.Messages;
import org.eclipse.ui.internal.navigator.NavigatorFilterService;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.ICommonFilterDescriptor;
import org.eclipse.ui.navigator.INavigatorContentService;

/**
 * Ensures that a given set of filters is <i>active</i> and the complement of
 * that set of filters are not <i>active</i>.
 *
 * <p>
 * This operation is smart enough not to force any change if each id in each set
 * is already in its desired state (<i>active</i> or <i>inactive</i>).
 * <p>
 * Copied and adapted from org.eclipse.ui.internal.navigator.filters.UpdateActiveFiltersOperation
 */
@SuppressWarnings("restriction")
public class UpdateActiveFiltersOperation extends AbstractOperation {

	private String[] filterIdsToActivate;

	private final CommonViewer commonViewer;

	private final INavigatorContentService contentService;

	/**
	 * Create an operation to activate extensions and refresh the viewer.
	 *
	 *
	 * @param aCommonViewer
	 *            The CommonViewer instance to update
	 * @param theActiveFilterIds
	 *            An array of ids that correspond to the filters that should be
	 *            in the <i>active</i> state after this operation executes. The
	 *            complement of this set will likewise be in the <i>inactive</i>
	 *            state after this operation executes.
	 */
	public UpdateActiveFiltersOperation(CommonViewer aCommonViewer, String[] theActiveFilterIds) {
		super(Messages.UpdateActiveFiltersOperation_OperationName);
		Assert.isNotNull(theActiveFilterIds);

		commonViewer = aCommonViewer;
		contentService = commonViewer.getNavigatorContentService();
		filterIdsToActivate = theActiveFilterIds;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.commands.operations.AbstractOperation#execute(org.eclipse.core.runtime.IProgressMonitor,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */
	@Override
    public IStatus execute(IProgressMonitor monitor, IAdaptable info) {
        NavigatorFilterService filterService = (NavigatorFilterService) contentService.getFilterService();
		ICommonFilterDescriptor[] filterDescriptors = filterService.getVisibleFilterDescriptorsForUI();

		// Compute delta list.
		List<String> deltaList = new ArrayList<String>();
		Set<String> current = new HashSet<String>();
		for(ICommonFilterDescriptor filterDescriptor : filterDescriptors) {
			String filterId = filterDescriptor.getId();
			if(filterService.isActive(filterId)) {
				current.add(filterId);
				deltaList.add(filterId);
			}
		}

		for(String filterId : filterIdsToActivate) {
			if (current.contains(filterId)) deltaList.remove(filterId);
			else deltaList.add(filterId);
		}

		filterService.activateFilterIdsAndUpdateViewer(filterIdsToActivate);
		MRUList mru = new MRUList(IPreferenceKeys.PREF_FILTER_MRU_LIST);
		mru.updateMRUList(deltaList);
		return Status.OK_STATUS;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.commands.operations.AbstractOperation#redo(org.eclipse.core.runtime.IProgressMonitor,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */
	@Override
    public IStatus redo(IProgressMonitor monitor, IAdaptable info) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.commands.operations.AbstractOperation#undo(org.eclipse.core.runtime.IProgressMonitor,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */
	@Override
    public IStatus undo(IProgressMonitor monitor, IAdaptable info) {
		return null;
	}
}
