/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.views.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.IMementoAware;

/**
 * The class used to save and restore the expanding states of
 * a common viewer in a navigator.
 */
public class ViewExpandingState implements IMementoAware {
	// The common viewer whose expanding state is to be persisted.
	private CommonViewer viewer;
	/**
	 * The constructor.
	 */
	public ViewExpandingState(CommonViewer viewer) {
		this.viewer = viewer;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.navigator.IMementoAware#restoreState(org.eclipse.ui.IMemento)
	 */
	@Override
    public void restoreState(IMemento memento) {
		RestoreJob job = new RestoreJob(memento);
		job.addJobChangeListener(new RestoreDone(viewer));
		job.schedule();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.navigator.IMementoAware#saveState(org.eclipse.ui.IMemento)
	 */
	@Override
    public void saveState(IMemento memento) {
		Object[] elements = viewer.getVisibleExpandedElements(); // Do not remember invisible expanded elements.
		Map<Object, UUID> savedElements = new HashMap<Object, UUID>();
		if(elements != null && elements.length > 0) {
			IMemento memExpand = memento.createChild("expanded-elements"); //$NON-NLS-1$
			for(Object element : elements) {
				if(element instanceof IAdaptable) {
					UUID uuid = saveElement(memExpand, (IAdaptable)element);
					if(uuid != null) {
						savedElements.put(element, uuid);
					}
				}
			}
		}
		TreePath[] paths = viewer.getExpandedTreePaths();
		if(paths != null && paths.length > 0) {
			IMemento memExpand = memento.createChild("expanded-paths"); //$NON-NLS-1$
			for(TreePath path : paths) {
				if(isPathVisible(path, savedElements)) {
					savePath(memExpand, path, savedElements);
				}
			}
		}
	}

	/**
	 * Iterate the path elements and see if all the elements are visible based
	 * on the map stored. If all the elements of the path is visible, then the
	 * path is visible, or else invisible.
	 * 
	 * @param path The path to be tested.
	 * @param visibleElements The visible elements stored as a map.
	 * @return true if the path is visible.
	 */
	private boolean isPathVisible(TreePath path, Map<Object, UUID> visibleElements) {
		int count = path.getSegmentCount();
		for(int i=0;i<count;i++){
			Object element = path.getSegment(i);
			if(visibleElements.get(element) == null)
				return false;
		}
		return true;
    }
	
	/**
	 * Save the path under the specified memento using the element map to find
	 * its element based on the referenced uuid.
	 * 
	 * @param memExpand The memento under which to save the path.
	 * @param path The path to save.
	 * @param savedElements The saved elements.
	 */
	private void savePath(IMemento memExpand, TreePath path, Map<Object, UUID> savedElements) {
		IMemento pathElement = memExpand.createChild("path"); //$NON-NLS-1$
		int count = path.getSegmentCount();
		for(int i=0;i<count;i++) {
			Object element = path.getSegment(i);
			UUID uuid = savedElements.get(element);
			pathElement.createChild("element", uuid.toString()); //$NON-NLS-1$
		}
    }

	/**
	 * Save an element to an element memento under the expanding
	 * elements memento. Assign each element with a uuid which
	 * serves as a reference id for path elements.
	 * 
	 * @param memExpand The expanding memento.
	 * @param element The element to save.
	 * @return the uuid of this element, or null if saving failed.
	 */
	private UUID saveElement(IMemento memExpand, IAdaptable element) {
		IPersistableElement persistable = (IPersistableElement) element.getAdapter(IPersistableElement.class);
		if(persistable == null) {
			persistable = (IPersistableElement) Platform.getAdapterManager().getAdapter(element, IPersistableElement.class);
		}
		if(persistable != null) {
			UUID uuid = UUID.randomUUID();
			String factoryId = persistable.getFactoryId();
			IMemento memElement = memExpand.createChild("element", uuid.toString()); //$NON-NLS-1$
			memElement.putString("factoryId", factoryId); //$NON-NLS-1$
			persistable.saveState(memElement);
			return uuid;
		}
		return null;
	}
}
