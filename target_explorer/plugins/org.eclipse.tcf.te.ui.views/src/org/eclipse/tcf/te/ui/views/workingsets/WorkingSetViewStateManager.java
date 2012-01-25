/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.views.workingsets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.tcf.te.ui.views.activator.UIPlugin;
import org.eclipse.tcf.te.ui.views.interfaces.workingsets.IWorkingSetIDs;
import org.eclipse.tcf.te.ui.views.interfaces.workingsets.IWorkingSetNameIDs;
import org.eclipse.tcf.te.ui.views.internal.View;
import org.eclipse.tcf.te.ui.views.nls.Messages;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.ILocalWorkingSetManager;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.navigator.IMementoAware;

/**
 * The WorkingSetViewStateManager is a state manager used to load and save persisted
 * states from external storage and provide runtime access and modification
 * methods.
 */
public final class WorkingSetViewStateManager implements IMementoAware {
	// The file used to persist the states used in this plugin.
	private static final String STATES_XML = "states.xml"; //$NON-NLS-1$

	// The memento element name id's.

	/** The root element of working set manager.*/
	private static final String WORKING_SET_MANAGER = "workingSetManager"; //$NON-NLS-1$
	/** The property name used to save the sorting flag of the working sets.*/
	private static final String SORTED_WORKINGSET = "sortedWorkingSet"; //$NON-NLS-1$
	/** The element used to save the order of the working sets specified by end users.*/
	private static final String WORKINGSET_ORDER = "order"; //$NON-NLS-1$
	/** The root element of visible working sets.*/
	private static final String WORKINGSET_VISIBLE = "visibleWorkingSets"; //$NON-NLS-1$
	/** The working set element. */
	private static final String ELEMENT_WORKINGSET = "workingset"; //$NON-NLS-1$
	/** The attribute to store name. */
	private static final String ATTR_NAME = "name"; //$NON-NLS-1$
	/** The attribute to store the option that if the default working set should be sorted on top. */
	private static final String ATTR_DEFAULT_WORKINGSET_ONTOP = "defaultWorkingSetOnTop"; //$NON-NLS-1$

	// The parent view instance.
	/* default */ View view;
	// The working set manager used.
	/* default */ ILocalWorkingSetManager localWorkingSetManager;
	// If the working set is sorted
	private boolean sortedWorkingSet;
	// If the default fixed working sets, including My Targets and Local Subnet, should be on top when sorting.
	private boolean defaultWorkingSetOnTop = true;
	// The working set comparator used to sort the working set in the order specified by end users.
	private CustomizedOrderComparator workingSetComparator;
	// The list of active (checked) working sets
	private List<IWorkingSet> visibleWorkingSets;

	// Workbench wide working set manager property change listener
	private IPropertyChangeListener workingSetManagerListener;

	/**
	 * Constructor.
	 *
	 * @param view The parent view instance. Must not be <code>null</code>.
	 */
	public WorkingSetViewStateManager(View view) {
		Assert.isNotNull(view);
		this.view = view;

		IMemento memento = readWorkingSetMemento();
		restoreState(memento);

		initializeListener();
	}

	/**
	 * Returns the parent view instance.
	 *
	 * @return The parent view instance.
	 */
	public View getParentView() {
		return view;
	}

	/**
	 * If the default working sets are on top when sorting.
	 * @return true if it is or else false.
	 */
	public boolean isDefaultWorkingSetOnTop() {
		return defaultWorkingSetOnTop;
	}

	/**
	 * Get the current local working set manager used.
	 *
	 * @return The current local working set manager.
	 */
	public ILocalWorkingSetManager getLocalWorkingSetManager() {
		return localWorkingSetManager;
	}

	/**
	 * Returns the list of all working sets.
	 *
	 * @return The list of all working sets or an empty list.
	 */
	public IWorkingSet[] getAllWorkingSets() {
		List<IWorkingSet> workingSets = new ArrayList<IWorkingSet>();

		// Add the workbench wide working sets (target type only)
		IWorkingSetManager manager = PlatformUI.getWorkbench().getWorkingSetManager();
		IWorkingSet[] candidates = manager.getAllWorkingSets();
		for (IWorkingSet candidate : candidates) {
			if (!candidate.isAggregateWorkingSet() && IWorkingSetIDs.ID_WS_TARGET.equals(candidate.getId())) {
				if (!workingSets.contains(candidate)) {
					workingSets.add(candidate);
				}
			}
		}
		// Add all local working sets
		candidates = localWorkingSetManager.getAllWorkingSets();
		for (IWorkingSet candidate : candidates) {
			if (!workingSets.contains(candidate)) {
				workingSets.add(candidate);
			}
		}

		return workingSets.toArray(new IWorkingSet[workingSets.size()]);
	}

	/**
	 * Get the visible working sets displayed in the working set viewer.
	 * @return The visible working sets in a list.
	 */
	public List<IWorkingSet> getVisibleWorkingSets() {
		return visibleWorkingSets;
	}

	/**
	 * Set the visible working sets with a new working set list.
	 * @param visibleWorkingSets The new working set list.
	 */
	public void setVisibleWorkingSets(List<IWorkingSet> visibleWorkingSets) {
		this.visibleWorkingSets = visibleWorkingSets;
	}

	/**
	 * If the current working sets are sorted.
	 * @return true if they are sorted.
	 */
	public boolean isSortedWorkingSet() {
		return sortedWorkingSet;
	}

	/**
	 * Set the flag if the current working sets are sorted.
	 * @param sortedWorkingSet The new value.
	 */
	public void setSortedWorkingSet(boolean sortedWorkingSet) {
		this.sortedWorkingSet = sortedWorkingSet;
	}

	/**
	 * Get the customized ordered comparator. This comparator is
	 * used to sort the working sets when users define the order of the working sets.
	 * @return The comparator.
	 */
	public CustomizedOrderComparator getWorkingSetComparator() {
		return workingSetComparator;
	}

	/**
	 * Set the new customized ordered working set comparator.
	 *
	 * @param workingSetComparator The new comparator.
	 */
	public void setWorkingSetComparator(CustomizedOrderComparator workingSetComparator) {
		this.workingSetComparator = workingSetComparator;
	}

	/**
	 * Dispose the working set view state manager.
	 */
	public void dispose() {
		XMLMemento memento = XMLMemento.createWriteRoot(WORKING_SET_MANAGER);
		saveState(memento);
		try {
			writeWorkingSetMemento(memento);
		}
		catch (IOException e) {
			if (Platform.inDebugMode()) e.printStackTrace();
		}
		localWorkingSetManager.dispose();
		if (workingSetManagerListener != null) {
			PlatformUI.getWorkbench().getWorkingSetManager().removePropertyChangeListener(workingSetManagerListener);
			workingSetManagerListener = null;
		}
	}

	/**
	 * Initialize the working set view state manager listeners.
	 */
	private void initializeListener() {
		// Listen to changes to the workbench wide working set manager
		// and trigger view updates on changes.
		workingSetManagerListener = new IPropertyChangeListener() {
            @Override
	        public void propertyChange(PropertyChangeEvent event) {
				if (IWorkingSetManager.CHANGE_WORKING_SET_ADD.equals(event.getProperty())) {
					// A new working set got added to the manager. Newly added
					// working sets are active (checked) by default.
					IWorkingSet workingSet = (IWorkingSet)event.getNewValue();
					if (workingSet != null) {
						List<IWorkingSet> visible = getVisibleWorkingSets();
						if (!visible.contains(workingSet)) {
							visible.add(workingSet);
						}
					}
				}
				else if (IWorkingSetManager.CHANGE_WORKING_SET_REMOVE.equals(event.getProperty())) {
					// A working set got removed. Remove from the list
					// of visible working sets
					IWorkingSet workingSet = (IWorkingSet)event.getOldValue();
					if (workingSet != null) {
						List<IWorkingSet> visible = getVisibleWorkingSets();
						if (visible.contains(workingSet)) {
							visible.remove(workingSet);
						}
					}
				}

				if (view != null && view.getCommonViewer() != null) {
					view.getCommonViewer().refresh();
				}
			}
		};
		PlatformUI.getWorkbench().getWorkingSetManager().addPropertyChangeListener(workingSetManagerListener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.IMementoAware#restoreState(org.eclipse.ui.IMemento)
	 */
	@Override
    public void restoreState(IMemento memento) {
		Boolean bool = memento.getBoolean(SORTED_WORKINGSET);
		sortedWorkingSet = bool != null && bool.booleanValue();
		if (!sortedWorkingSet) {
			restoreWorkingSetOrder(memento);
		}
		bool = memento.getBoolean(ATTR_DEFAULT_WORKINGSET_ONTOP);
		if (bool != null) {
			defaultWorkingSetOnTop = bool.booleanValue();
		}

		// The local working set manager contains only automatic generated
		// working sets. Do not restore anything from the memento
		localWorkingSetManager = PlatformUI.getWorkbench().createLocalWorkingSetManager();
		// Restore the local automatic working sets
		restoreLocalWorkingSets();

		// Restore the active (checked) working sets
		restoreVisibleWorkingSet(memento);
	}

	/**
	 * Restore the local automatic working sets.
	 */
	private void restoreLocalWorkingSets() {
		Assert.isNotNull(localWorkingSetManager);
		// Create the "Others" working set if not restored from the memento
		IWorkingSet others = localWorkingSetManager.getWorkingSet(Messages.ViewStateManager_others_name);
		if (others == null) {
			others = localWorkingSetManager.createWorkingSet(Messages.ViewStateManager_others_name, new IAdaptable[0]);
			others.setId(IWorkingSetIDs.ID_WS_OTHERS);
			localWorkingSetManager.addWorkingSet(others);
		} else {
			others.setId(IWorkingSetIDs.ID_WS_OTHERS);
		}
	}

	/**
	 * Restore the visible working sets from the specified memento element.
	 * @param memento The element.
	 */
	private void restoreVisibleWorkingSet(IMemento memento) {
		IMemento vwsMemento = memento.getChild(WORKINGSET_VISIBLE);
		visibleWorkingSets = new ArrayList<IWorkingSet>();
		if (vwsMemento != null) {
			IMemento[] children = vwsMemento.getChildren(ELEMENT_WORKINGSET);
			if (children != null && children.length > 0) {
				IWorkingSet[] allWorkingSets = getAllWorkingSets();
				for (IMemento child : children) {
					String wsName = child.getString(ATTR_NAME);
					for (IWorkingSet workingSet : allWorkingSets) {
						if (wsName.equals(workingSet.getName())) {
							visibleWorkingSets.add(workingSet);
						}
					}
				}
			}
		} else {
			IWorkingSet[] workingSets = getAllWorkingSets();
			for (IWorkingSet workingSet : workingSets) {
				visibleWorkingSets.add(workingSet);
			}
		}
	}

	/**
	 * Restore the order of working sets specified in the memento element.
	 * @param memento The element.
	 */
	private void restoreWorkingSetOrder(IMemento memento) {
		IMemento child = memento.getChild(WORKINGSET_ORDER);
		if (child != null) {
			String factoryId = child.getString(IWorkingSetNameIDs.FACTORY_ID);
			IElementFactory factory = PlatformUI.getWorkbench().getElementFactory(factoryId);
			IAdaptable adaptable = factory.createElement(child);
			IPersistableElement element = (IPersistableElement) adaptable.getAdapter(IPersistableElement.class);
			if (element instanceof CustomizedOrderComparator) {
				workingSetComparator = (CustomizedOrderComparator) element;
			}
		}
	}

	/**
	 * Save the states to the specified memento element.
	 */
	@Override
    public void saveState(IMemento memento) {
		memento.putBoolean(SORTED_WORKINGSET, sortedWorkingSet);
		if (!sortedWorkingSet) {
			if (workingSetComparator != null) {
				IMemento child = memento.createChild(WORKINGSET_ORDER);
				child.putString(IWorkingSetNameIDs.FACTORY_ID, workingSetComparator.getFactoryId());
				workingSetComparator.saveState(child);
			}
		}
		memento.putBoolean(ATTR_DEFAULT_WORKINGSET_ONTOP, defaultWorkingSetOnTop);
		IMemento vwsMemento = memento.createChild(WORKINGSET_VISIBLE);
		for (IWorkingSet workingSet : visibleWorkingSets) {
			IMemento child = vwsMemento.createChild(ELEMENT_WORKINGSET);
			child.putString(ATTR_NAME, workingSet.getName());
		}
	}

	/**
	 * Returns the file used as the persistence store, or <code>null</code> if
	 * there is no available file.
	 *
	 * @return the file used as the persistence store, or <code>null</code>
	 */
	private File getStateFile() {
        try {
        	IPath path = UIPlugin.getDefault().getStateLocation();
    		path = path.append(STATES_XML);
    		return path.toFile();
        }catch (IllegalStateException e) {
            // An RCP workspace-less environment (-data @none)
        	File root = new File(System.getProperty("user.home"), ".tcf"); //$NON-NLS-1$ //$NON-NLS-2$
        	File ws = new File(root, "ws"); //$NON-NLS-1$
        	return new File(ws, STATES_XML);
        }
	}

	/**
	 * Read the working set memento element from the state file.
	 * @return The working set memento.
	 */
	private IMemento readWorkingSetMemento() {
		File stateFile = getStateFile();
		if (stateFile != null && stateFile.exists()) {
			BufferedReader reader = null;
			try {
				FileInputStream input = new FileInputStream(stateFile);
				reader = new BufferedReader(new InputStreamReader(input, "utf-8")); //$NON-NLS-1$
				IMemento memento = XMLMemento.createReadRoot(reader);
				return memento;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (Exception e) {
					}
				}
			}
		}
		return XMLMemento.createWriteRoot(WORKING_SET_MANAGER);
	}

	/**
	 * Write working set memento to the state file.
	 * @param memento The working set memento.
	 * @throws IOException thrown when writing fails.
	 */
	private void writeWorkingSetMemento(XMLMemento memento) throws IOException {
		File stateFile = getStateFile();
		FileOutputStream stream = new FileOutputStream(stateFile);
		OutputStreamWriter writer = new OutputStreamWriter(stream, "utf-8"); //$NON-NLS-1$
		memento.save(writer);
		writer.close();
	}

}
