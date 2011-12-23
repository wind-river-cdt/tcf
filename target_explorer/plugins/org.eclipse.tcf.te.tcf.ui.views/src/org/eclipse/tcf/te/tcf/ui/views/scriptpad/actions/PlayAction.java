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

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.runtime.callback.Callback;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.runtime.properties.PropertiesContainer;
import org.eclipse.tcf.te.runtime.statushandler.StatusHandlerManager;
import org.eclipse.tcf.te.runtime.statushandler.interfaces.IStatusHandler;
import org.eclipse.tcf.te.runtime.statushandler.interfaces.IStatusHandlerConstants;
import org.eclipse.tcf.te.tcf.core.scripting.interfaces.IScriptLauncherProperties;
import org.eclipse.tcf.te.tcf.core.scripting.launcher.ScriptLauncher;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.ui.views.help.IContextHelpIds;
import org.eclipse.tcf.te.tcf.ui.views.nls.Messages;
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

	// Flag to remember if a script is currently running
	/* default */ boolean running;

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
    		    		actionProxy.setEnabled(enabled && !running);
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
    		action.setEnabled(enabled && !running);
    	}
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
    	String script = null;
    	IPeerModel peerModel = null;

    	if (view instanceof ScriptPad && ((ScriptPad)view).getStyledText() != null) {
    		script = ((ScriptPad)view).getStyledText().getText();
    		peerModel = ((ScriptPad)view).getPeerModel();
    	}

    	if (script != null && !"".equals(script) && peerModel != null) { //$NON-NLS-1$
        	final ScriptLauncher launcher = new ScriptLauncher();

        	IPropertiesContainer properties = new PropertiesContainer();
        	properties.setProperty(IScriptLauncherProperties.PROP_SCRIPT, script);

        	final AtomicReference<IPeer> peer = new AtomicReference<IPeer>();
        	final IPeerModel finPeerModel = peerModel;
        	Runnable runnable = new Runnable() {
        		@Override
        		public void run() {
        			peer.set(finPeerModel.getPeer());
        		}
        	};
        	if (Protocol.isDispatchThread()) runnable.run();
        	else Protocol.invokeAndWait(runnable);

        	running = true;

        	launcher.launch(peer.get(), properties, new Callback() {
        		@Override
        		protected void internalDone(Object caller, IStatus status) {
        			running = false;
        			launcher.dispose();

		    		boolean enabled = false;
		    		if (PlayAction.this.view instanceof ScriptPad) enabled = ((ScriptPad)PlayAction.this.view).getPeerModel() != null;
		    		actionProxy.setEnabled(enabled && !running);

		    		if (status != null && (status.getSeverity() == IStatus.ERROR || status.getSeverity() == IStatus.WARNING)) {
		    			IStatusHandler[] handler = StatusHandlerManager.getInstance().getHandler(view);
		    			if (handler != null && handler.length > 0) {
		    				IPropertiesContainer data = new PropertiesContainer();
		    				data.setProperty(IStatusHandlerConstants.PROPERTY_TITLE, Messages.ScriptPad_error_title);
		    				data.setProperty(IStatusHandlerConstants.PROPERTY_CONTEXT_HELP_ID, IContextHelpIds.SCRIPT_PAD_ERROR_PLAY_FAILED);
		    				data.setProperty(IStatusHandlerConstants.PROPERTY_CALLER, view);

		    				handler[0].handleStatus(status, data, null);
		    			}
		    		}
        		}
        	});
    	}
    }

	/* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    @Override
    public void run(IAction action) {
    	run();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate2#runWithEvent(org.eclipse.jface.action.IAction, org.eclipse.swt.widgets.Event)
     */
    @Override
    public void runWithEvent(IAction action, Event event) {
    	runWithEvent(event);
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
