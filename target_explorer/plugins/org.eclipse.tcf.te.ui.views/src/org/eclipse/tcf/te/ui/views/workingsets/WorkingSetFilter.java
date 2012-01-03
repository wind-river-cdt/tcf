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

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.tcf.te.runtime.interfaces.workingsets.IWorkingSetElement;
import org.eclipse.ui.IContainmentAdapter;
import org.eclipse.ui.IWorkingSet;

/**
 * The working set filter filters elements from a view that are neither a parent nor
 * children of a working set element.
 */
public class WorkingSetFilter extends ViewerFilter {
	private boolean active = false;

	/**
	 * Sets the working set filter active or inactive.
	 *
	 * @param active <code>True</code> to set the filter active, <code>false</code> to set the filter inactive.
	 */
	public final void setActive(boolean active) {
		this.active = active;
	}

    /**
     * Determines if an element should be filtered out.
     *
     * @see ViewerFilter#select(Viewer, Object, Object)
     */
    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
    	// If the filter is not active, all elements pass
    	if (!active) return true;

    	// If the parent element is a tree path, assume the last element as parent element
    	if (parentElement instanceof TreePath) {
    		parentElement = ((TreePath)parentElement).getLastSegment();
    	}

    	if (parentElement instanceof IWorkingSet) {
    		if (((IWorkingSet)parentElement).isEmpty()) {
    			return true;
    		}
    		if (element != null) {
    			return isEnclosed((IWorkingSet)parentElement, element);
    		}
    	} else if (parentElement instanceof WorkingSetViewStateManager && element instanceof IWorkingSet) {
			List<IWorkingSet> workingSets = Arrays.asList(((WorkingSetViewStateManager)parentElement).getAllWorkingSets());
			return workingSets.contains(element);
    	}
        return true;
    }

    /**
     * Returns if the given element is enclosed by a working set element.
     * The IContainmentAdapter of each working set element is used for the
     * containment test. If there is no IContainmentAdapter for a working
     * set element, a simple resource based test is used.
     *
     * @param element The element to test for enclosure by a working set element
     * @return true if element is enclosed by a working set element and false otherwise.
     */
    private boolean isEnclosed(IWorkingSet workingSet, Object element) {
        IAdaptable[] workingSetElements = workingSet.getElements();

        for (int i = 0; i < workingSetElements.length; i++) {
            IAdaptable workingSetElement = workingSetElements[i];
            IContainmentAdapter containmentAdapter = (IContainmentAdapter) workingSetElement.getAdapter(IContainmentAdapter.class);

            // if there is no IContainmentAdapter defined for the working
            // set element type fall back to using resource based
            // containment check
            if (containmentAdapter != null) {
                if (containmentAdapter.contains(workingSetElement, element,
                        IContainmentAdapter.CHECK_CONTEXT
                                | IContainmentAdapter.CHECK_IF_CHILD
                                | IContainmentAdapter.CHECK_IF_ANCESTOR
                                | IContainmentAdapter.CHECK_IF_DESCENDANT)) {
					return true;
				}
            } else if (isEnclosedElement(element, workingSetElement)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns if the given element is enclosed by a working set element.
     * A element is enclosed if it is either a parent of a working set
     * element, a child of a working set element or a working set element
     * itself.
     *
     * @param element The element to test for enclosure by a working set element
     * @param workingSetElement The working set element. Must be not <code>null</code>.
     *
     * @return true if element is enclosed by a working set element and false otherwise.
     */
    private boolean isEnclosedElement(Object element, IAdaptable workingSetElement) {
    	Assert.isNotNull(workingSetElement);

        if (workingSetElement.equals(element)) {
			return true;
		}

        if (element instanceof IWorkingSetElement) {
        	IWorkingSetElement wsElement = (IWorkingSetElement) element;
        	WorkingSetElementHolder holder = (WorkingSetElementHolder) workingSetElement.getAdapter(WorkingSetElementHolder.class);
        	if (holder != null) {
        		if (wsElement.equals(holder.getElement())) {
        			return true;
        		}
        		if (wsElement.getElementId().equals(holder.getElementId())) {
        			holder.setElement(wsElement);
        			return true;
        		}
        	}
        }

        return false;
    }
}
