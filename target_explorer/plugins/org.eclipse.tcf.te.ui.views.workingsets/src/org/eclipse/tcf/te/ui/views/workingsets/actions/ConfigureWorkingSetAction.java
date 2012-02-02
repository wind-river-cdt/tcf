/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.views.workingsets.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.tcf.te.ui.views.interfaces.workingsets.IWorkingSetIDs;
import org.eclipse.tcf.te.ui.views.workingsets.CustomizedOrderComparator;
import org.eclipse.tcf.te.ui.views.workingsets.WorkingSetViewStateManager;
import org.eclipse.tcf.te.ui.views.workingsets.dialogs.WorkingSetConfigurationDialog;
import org.eclipse.tcf.te.ui.views.workingsets.nls.Messages;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonViewer;

/**
 * Configure working sets action implementation.
 */
public class ConfigureWorkingSetAction extends Action {
	// The parent viewer instance.
	private StructuredViewer viewer;

	/**
	 * Constructor.
	 *
	 * @param view The parent viewer instance. Must not be <code>null</code>.
	 */
	public ConfigureWorkingSetAction(StructuredViewer viewer) {
		super(Messages.ConfigureWorkingSetAction_text);
		setToolTipText(Messages.ConfigureWorkingSetAction_toolTip);

		Assert.isNotNull(viewer);
		this.viewer = viewer;
	}

	/**
	 * Duplicate the specified working sets and return a working set list for editing.
	 *
	 * @param workingSets The working sets
	 * @return A duplication list of the working sets.
	 */
	private List<IWorkingSet> duplicate(IWorkingSet[] workingSets) {
		IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
		List<IWorkingSet> result = new ArrayList<IWorkingSet>();
		for (IWorkingSet workingSet : workingSets) {
			// Local automatic working sets are not editable
			// -> no need to create a duplicate
			if (isLocalWorkingSet(workingSet)) {
				result.add(workingSet);
			} else {
				IWorkingSet newWorkingSet = workingSetManager.createWorkingSet(workingSet.getName(), workingSet.getElements());
				newWorkingSet.setId(workingSet.getId());
				result.add(newWorkingSet);
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		// Get the view working set state manager
		WorkingSetViewStateManager manager = (WorkingSetViewStateManager)Platform.getAdapterManager().getAdapter(((CommonViewer)viewer).getCommonNavigator(), WorkingSetViewStateManager.class);
		// Get the workbench wide working set manager
		IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();

		// Get all working sets (both local and workbench wide, target explorer type only)
		IWorkingSet[] allWorkingSets = manager.getAllWorkingSets();
		// Create working copies of the (sorted) working sets
		List<IWorkingSet> workingSets = duplicate(allWorkingSets);

		// Determine the active (checked) working sets
		List<IWorkingSet> visibleWorkingSets = manager.getVisibleWorkingSets();
		IWorkingSet[] activeWorkingSets = visibleWorkingSets.toArray(new IWorkingSet[visibleWorkingSets.size()]);

		// Sort the working sets if necessary
		boolean sortedWorkingSets = manager.isSortedWorkingSet();
		if (!sortedWorkingSets) {
			Comparator<IWorkingSet> workingSetComparator = manager.getWorkingSetComparator();
			if (workingSetComparator != null) Collections.sort(workingSets, workingSetComparator);
		}

		// Create the working set configuration dialog
		WorkingSetConfigurationDialog dialog = new WorkingSetConfigurationDialog(workingSetManager,
																				 viewer.getControl().getShell(),
																				 workingSets.toArray(new IWorkingSet[workingSets.size()]),
																				 sortedWorkingSets);
		// Set the active working sets
		dialog.setSelection(activeWorkingSets);
		// And open the dialog
		if (dialog.open() == IDialogConstants.OK_ID) {
			// Remember the current list of recently used working sets
			IWorkingSet[] mruWorkingSets = workingSetManager.getRecentWorkingSets();
			// Remove the old working set instances (non-local only)
			for (IWorkingSet workingSet : allWorkingSets) {
				if (!isLocalWorkingSet(workingSet)) {
					workingSetManager.removeWorkingSet(workingSet);
				}
			}
			// Get the new working set list from the dialog
			allWorkingSets = dialog.getAllWorkingSets();
			// And add the working sets back to the manager (non-local only)
			for (IWorkingSet workingSet : allWorkingSets) {
				if (!isLocalWorkingSet(workingSet)) {
					workingSetManager.addWorkingSet(workingSet);
				}
				// If the working set existed in the recently used working set list,
				// restore it
				if (exists(workingSet.getName(), mruWorkingSets)) {
					workingSetManager.addRecentWorkingSet(workingSet);
				}
			}

			// Update the sorted state
			sortedWorkingSets = dialog.isSortingEnabled();
			manager.setSortedWorkingSet(sortedWorkingSets);
			if (!sortedWorkingSets) {
				CustomizedOrderComparator comparator = new CustomizedOrderComparator(allWorkingSets);
				manager.setWorkingSetComparator(comparator);
			}

			// Update the active (checked) working set list
			IWorkingSet[] selection = dialog.getSelection();
			List<IWorkingSet> list = new ArrayList<IWorkingSet>(Arrays.asList(selection));
			manager.setVisibleWorkingSets(list);

			// Trigger a viewer refresh to reflect the changes
			viewer.refresh();
		}
	}

	/**
	 * Determine if the given working set is a local working set and therefore
	 * managed by the local working set manager.
	 *
	 * @param workingSet The working set. Must not be <code>null</code>.
	 * @return <code>True</code> if the working set is a local working set, <code>false</code> otherwise.
	 */
	private boolean isLocalWorkingSet(IWorkingSet workingSet) {
		Assert.isNotNull(workingSet);

		boolean isLocal = IWorkingSetIDs.ID_WS_OTHERS.equals(workingSet.getId());

		return isLocal;
	}

	/**
	 * Judge if the working set with the specified name exists in the working set array.
	 *
	 * @param wsname The name of the working set.
	 * @param workingSets The working set array to be searched.
	 * @return true if a working set with the specified name exists.
	 */
	private boolean exists(String wsname, IWorkingSet[] workingSets) {
		for (IWorkingSet workingSet : workingSets) {
			if (wsname.equals(workingSet)) {
				return true;
			}
		}
		return false;
	}
}
