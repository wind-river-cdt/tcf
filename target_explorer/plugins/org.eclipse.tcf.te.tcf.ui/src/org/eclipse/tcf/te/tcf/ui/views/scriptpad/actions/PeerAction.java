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

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.ui.internal.navigator.LabelProviderDelegate;
import org.eclipse.tcf.te.tcf.ui.views.scriptpad.ScriptPad;
import org.eclipse.ui.IViewPart;

/**
 * Peer toggle action implementation.
 */
public class PeerAction extends Action {
	// Static reference to a label provider delegate providing the action label and image
	private final static LabelProviderDelegate delegate = new LabelProviderDelegate();

	// Reference to the peer model
	private IPeerModel peerModel;
	// Reference to the parent view part
	private IViewPart view;

	/**
     * Constructor.
     *
     * @param view The parent view part. Must not be <code>null</code>.
     * @param peerModel The peer model. Must not be <code>null</code>.
     */
    public PeerAction(IViewPart view, IPeerModel peerModel) {
    	super("", AS_CHECK_BOX); //$NON-NLS-1$

    	Assert.isNotNull(peerModel);
    	this.peerModel = peerModel;

    	String label = delegate.getText(peerModel);
    	if (label != null) {
    		setText(delegate.decorateText(label, peerModel));
    	}

    	Assert.isNotNull(view);
    	this.view = view;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
    	setChecked(!isChecked());

    	// Pass on the selected peer model to the parent view
    	if (view instanceof ScriptPad) {
    		((ScriptPad)view).setPeerModel(peerModel);
    	}
    }
}
