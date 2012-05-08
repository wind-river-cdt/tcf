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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.tcf.te.ui.views.nls.Messages;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PlatformUI;

/**
 * The job to restore the expanding state of a tree viewer using the input memento.
 */
public class RestoreJob extends Job {
	// The memento where the expanding state of the tree is stored.
	private IMemento memento;

	/**
	 * Create an job to restore the expanding state of the specified tree viewer.
	 * 
	 * @param memento The memento to restore the expanding state.
	 */
	public RestoreJob(IMemento memento) {
		super(Messages.RestoreJob_JobName);
		this.memento = memento;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
    protected IStatus run(IProgressMonitor monitor) {
		int work = getTotalWork();
		if (work > 0) {
			monitor.beginTask(Messages.RestoreJob_MainTask, work);
			IMemento memExpand = memento.getChild("expanded-elements"); //$NON-NLS-1$
			if (memExpand != null) {
				monitor.subTask(Messages.RestoreJob_Task1Name);				
				IMemento[] memElements = memExpand.getChildren("element"); //$NON-NLS-1$
				Map<UUID, Object> elements = new HashMap<UUID, Object>();
				for (IMemento memElement : memElements) {
					restoreElement(memElement, elements);
					monitor.worked(1);
				}
				IMemento pathsElement = memento.getChild("expanded-paths"); //$NON-NLS-1$
				if (pathsElement != null) {
					monitor.subTask(Messages.RestoreJob_Task2Name);
					IMemento[] pathElements = pathsElement.getChildren("path"); //$NON-NLS-1$
					List<TreePath> paths = new ArrayList<TreePath>();
					for (IMemento pathElement : pathElements) {
						TreePath path = restorePath(pathElement, elements);
						if (path != null) {
							paths.add(path);
						}
						monitor.worked(1);
					}
					monitor.done();
					return new RestoreStatus(paths);
				}
			}
		}
		monitor.done();
		return Status.CANCEL_STATUS;
    }
	
	/**
	 * Get the total work amount of this job.
	 * 
	 * @return the toal work amount.
	 */
	private int getTotalWork() {
		int work = 0;
		IMemento memExpand = memento.getChild("expanded-elements"); //$NON-NLS-1$
		if(memExpand != null) {
			IMemento[] memElements = memExpand.getChildren("element"); //$NON-NLS-1$
			work += memElements != null ? memElements.length : 0;
			IMemento pathsElement = memento.getChild("expanded-paths"); //$NON-NLS-1$
			if(pathsElement != null) {
				IMemento[] pathElements = pathsElement.getChildren("path"); //$NON-NLS-1$
				work += pathElements != null ? pathElements.length : 0;
			}
		}
		return work;
	}
	
	/**
	 * Restore the path from the path element of the memento, using the
	 * restored elements stored in the map.
	 * 
	 * @param pathElement The path element to restore the path from.
	 * @param elements The element map restored.
	 * @return a tree path restored.
	 */
	private TreePath restorePath(IMemento pathElement, Map<UUID, Object> elements) {
		List<Object> list = new ArrayList<Object>();
		IMemento[] children = pathElement.getChildren("element"); //$NON-NLS-1$
		for (IMemento child : children) {
			String id = child.getID();
			try {
				UUID uuid = UUID.fromString(id);
				Object element = elements.get(uuid);
				if (element != null) {
					list.add(element);
				}
			}
			catch (IllegalArgumentException e) {
				e.printStackTrace();
				return null;
			}
		}
	    return new TreePath(list.toArray());
    }

	/**
	 * Restore an element from the element memento, and save the restored
	 * element to the map with its uuid as its key.
	 * 
	 * @param memElement The element memento.
	 * @param elements The map to store the retored element and its uuid.
	 */
	private void restoreElement(IMemento memElement, Map<UUID, Object> elements) {
		String factoryId = memElement.getString("factoryId"); //$NON-NLS-1$
		if(factoryId != null) {
			IElementFactory eFactory = PlatformUI.getWorkbench().getElementFactory(factoryId);
			if(eFactory != null) {
				Object element = eFactory.createElement(memElement);
				if(element != null) {
					String id = memElement.getID();
					try {
						UUID uuid = UUID.fromString(id);
						elements.put(uuid, element);
					}catch(IllegalArgumentException e){
						e.printStackTrace();
					}
				}
			}
		}
	}
}