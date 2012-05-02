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
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.PlatformUI;
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
		IMemento memExpand = memento.getChild("expanded-elements"); //$NON-NLS-1$
		if(memExpand != null) {
			IMemento[] memElements = memExpand.getChildren("element"); //$NON-NLS-1$
			List<Object> elements = new ArrayList<Object>();
			for(IMemento memElement : memElements) {
				Object element = restoreElement(memElement);
				if(element != null) {
					elements.add(element);
				}
			}
			IElementComparer comparer = viewer.getComparer();
			if(comparer instanceof ViewViewerComparer) {
				((ViewViewerComparer)comparer).setByDefault(false);
			}
			viewer.setExpandedElements(elements.toArray());
			if(comparer instanceof ViewViewerComparer) {
				((ViewViewerComparer)comparer).setByDefault(true);
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.navigator.IMementoAware#saveState(org.eclipse.ui.IMemento)
	 */
	@Override
    public void saveState(IMemento memento) {
		Object[] elements = viewer.getVisibleExpandedElements(); // Do not remember invisible expanded elements.
		if(elements != null && elements.length > 0) {
			IMemento memExpand = memento.createChild("expanded-elements"); //$NON-NLS-1$
			for(Object element : elements) {
				if(element instanceof IAdaptable) {
					saveElement(memExpand, (IAdaptable)element);
				}
			}
		}
	}
	
	/**
	 * Restore an element from the element memento.
	 * 
	 * @param memElement The element memento.
	 * @return an element if it succeeds.
	 */
	private IAdaptable restoreElement(IMemento memElement) {
		String factoryId = memElement.getString("factoryId"); //$NON-NLS-1$
		if(factoryId != null) {
			IElementFactory eFactory = PlatformUI.getWorkbench().getElementFactory(factoryId);
			if(eFactory != null) {
				return eFactory.createElement(memElement);
			}
		}
		return null;
	}
	
	/**
	 * Save an element to an element memento under the expanding
	 * elements memento.
	 * 
	 * @param memExpand The expanding memento.
	 * @param element The element to save.
	 */
	private void saveElement(IMemento memExpand, IAdaptable element) {
		IPersistableElement persistable = (IPersistableElement) element.getAdapter(IPersistableElement.class);
		if(persistable == null) {
			persistable = (IPersistableElement) Platform.getAdapterManager().getAdapter(element, IPersistableElement.class);
		}
		if(persistable != null) {
			String factoryId = persistable.getFactoryId();
			IMemento memElement = memExpand.createChild("element"); //$NON-NLS-1$
			memElement.putString("factoryId", factoryId); //$NON-NLS-1$
			persistable.saveState(memElement);
		}
	}
}
