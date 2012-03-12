/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.tcf.te.ui.views.handler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.tcf.te.ui.views.internal.preferences.IPreferenceConsts;
import org.eclipse.tcf.te.ui.views.nls.Messages;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.INavigatorContentDescriptor;
import org.eclipse.ui.navigator.INavigatorContentService;

/**
 * Ensures that a given set of content extensions is <i>active</i> and a second
 * non-intersecting set of content extensions are not <i>active</i>.
 * 
 * <p>
 * This operation is smart enough not to force any change if each id in each set
 * is already in its desired state (<i>active</i> or <i>inactive</i>).
 * </p>
 * 
 * @since 1.0.0 Copied and adapted from org.eclipse.ui.internal.navigator.filters.UpdateActiveExtensionsOperation
 * 
 */
public class UpdateActiveExtensionsOperation extends AbstractOperation {

	private String[] contentExtensionsToActivate;

	private final CommonViewer commonViewer;

	private final INavigatorContentService contentService;

	/**
	 * Create an operation to activate extensions and refresh the viewer.
	 * 
	 * p> To use only one part of this operation (either "activate" or
	 * "deactivate", but not both), then supply <b>null</b> for the array state
	 * you are not concerned with.
	 * </p>
	 * 
	 * @param aCommonViewer
	 *            The CommonViewer instance to update
	 * @param theExtensionsToActivate
	 *            An array of ids that correspond to the extensions that should
	 *            be in the <i>active</i> state after this operation executes.
	 */
	public UpdateActiveExtensionsOperation(CommonViewer aCommonViewer, String[] theExtensionsToActivate) {
		super(Messages.UpdateActiveExtensionsOperation_OperationName);
		commonViewer = aCommonViewer;
		contentService = commonViewer.getNavigatorContentService();
		contentExtensionsToActivate = theExtensionsToActivate;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.AbstractOperation#execute(org.eclipse.core.runtime.IProgressMonitor,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */
	@Override
    public IStatus execute(IProgressMonitor monitor, IAdaptable info) {

		// we sort the array in order to use Array.binarySearch();
		Arrays.sort(contentExtensionsToActivate);
		
		IStructuredSelection ssel = null;
	
		try {
			commonViewer.getControl().setRedraw(false);
			
			ISelection selection = commonViewer.getSelection();
			if(selection instanceof IStructuredSelection)
				ssel = (IStructuredSelection) selection;

			INavigatorContentDescriptor[] visibleContentDescriptors = contentService
					.getVisibleExtensions();

			int indexofContentExtensionIdToBeActivated;
			Set<String> deltaSet = new HashSet<String>();
			/* is there a delta? */
			for (int i = 0; i < visibleContentDescriptors.length; i++) {
				indexofContentExtensionIdToBeActivated = Arrays.binarySearch(
						contentExtensionsToActivate,
						visibleContentDescriptors[i].getId());
				/*
				 * Either we have a filter that should be active that isn't XOR
				 * a filter that shouldn't be active that is currently
				 */
				if (indexofContentExtensionIdToBeActivated >= 0
						^ contentService.isActive(visibleContentDescriptors[i]
								.getId())) {
					deltaSet.add(visibleContentDescriptors[i].getId());
				}
			}

			/* If so, update */
			if (!deltaSet.isEmpty()) {
				 
				contentService.getActivationService().activateExtensions(
						contentExtensionsToActivate, true);
				contentService.getActivationService()
						.persistExtensionActivations();
				

				Object[] expandedElements = commonViewer.getExpandedElements();

				contentService.update();

				commonViewer.refresh();
				
				Object[] originalObjects = ssel.toArray(); 
				
				commonViewer.setExpandedElements(expandedElements);

				IStructuredSelection newSelection = new StructuredSelection(originalObjects);
				commonViewer.setSelection(newSelection, true);
				MRUList mru = new MRUList(IPreferenceConsts.PREF_CONTENT_MRU_LIST);
				mru.updateMRUList(deltaSet);
			}

		} finally {
			commonViewer.getControl().setRedraw(true);
		} 

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
