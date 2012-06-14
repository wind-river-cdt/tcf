/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.utils;

import org.eclipse.core.commands.common.EventManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tcf.te.ui.interfaces.IOptionListener;
import org.eclipse.tcf.te.ui.interfaces.ISearchable;

/**
 * The base class that implements ISearchable and provide basic implementation method
 * for adding and removing listeners.
 */
public abstract class AbstractSearchable extends EventManager implements ISearchable {
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.interfaces.ISearchable#addOptionListener(org.eclipse.tcf.te.ui.interfaces.IOptionListener)
	 */
	@Override
    public void addOptionListener(IOptionListener listener) {
		super.addListenerObject(listener);
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.interfaces.ISearchable#removeOptionListener(org.eclipse.tcf.te.ui.interfaces.IOptionListener)
	 */
	@Override
    public void removeOptionListener(IOptionListener listener) {
		super.removeListenerObject(listener);
    }
	
	/**
	 * Fire an option changed event to the listeners to notify 
	 * the current option input has changed.
	 */
	protected void fireOptionChanged() {
		Object[] listeners = super.getListeners();
		for(Object listener : listeners) {
			((IOptionListener)listener).optionChanged(null);
		}
	}

	@Override
    public void createCommonPart(Composite parent) {
    }

	@Override
    public void createAdvancedPart(Composite parent) {
    }

	@Override
    public boolean isInputValid() {
	    return true;
    }

	@Override
	public String getSearchTitle() {
		return null;
	}

	@Override
	public String getSearchMessage(Object rootElement) {
		return null;
	}

	@Override
	public String getElementText(Object element) {
		return null;
	}
}
