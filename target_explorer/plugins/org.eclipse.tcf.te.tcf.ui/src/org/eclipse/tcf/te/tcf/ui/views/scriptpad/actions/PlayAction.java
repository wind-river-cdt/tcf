/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.ui.views.scriptpad.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.tcf.te.tcf.ui.views.scriptpad.ScriptPad;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPartConstants;

/**
 * Play script action implementation.
 */
public class PlayAction extends Action implements IViewActionDelegate, IActionDelegate2 {
	// Reference to the action proxy
	/* default */ IAction actionProxy;
	// Parent view part
	/* default */ IViewPart view;

	// Reference to the view property listener
	private IPropertyListener listener;

	/**
     * Constructor.
     */
    public PlayAction() {
    	super();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
     */
    @Override
    public void init(IViewPart view) {
    	this.view = view;

    	if (listener == null) {
    		listener = new IPropertyListener() {
    			@Override
    			public void propertyChanged(Object source, int propId) {
    				if (IWorkbenchPartConstants.PROP_INPUT == propId && actionProxy != null) {
    					// Update the action enablement
    		    		boolean enabled = false;
    		    		if (PlayAction.this.view instanceof ScriptPad) enabled = ((ScriptPad)PlayAction.this.view).getPeerModel() != null;
    		    		actionProxy.setEnabled(enabled);
    				}
    			}
    		};
        	view.addPropertyListener(listener);
    	}
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
     */
    @Override
    public void init(IAction action) {
    	this.actionProxy = action;
    	if (action != null) {
    		boolean enabled = false;
    		if (view instanceof ScriptPad) enabled = ((ScriptPad)view).getPeerModel() != null;
    		action.setEnabled(enabled);
    	}
    }

	/* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    @Override
    public void run(IAction action) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate2#runWithEvent(org.eclipse.jface.action.IAction, org.eclipse.swt.widgets.Event)
     */
    @Override
    public void runWithEvent(IAction action, Event event) {
    }

	/* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    @Override
    public void selectionChanged(IAction action, ISelection selection) {
    }

	/* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate2#dispose()
     */
    @Override
    public void dispose() {
    	if (listener != null) view.removePropertyListener(listener);
    }
}
