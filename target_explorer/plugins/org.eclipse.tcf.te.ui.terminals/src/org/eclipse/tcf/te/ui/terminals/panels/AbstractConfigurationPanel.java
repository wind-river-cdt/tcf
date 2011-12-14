/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.terminals.panels;

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.ui.controls.BaseDialogPageControl;
import org.eclipse.tcf.te.ui.controls.panels.AbstractWizardConfigurationPanel;
import org.eclipse.tcf.te.ui.terminals.interfaces.IConfigurationPanel;

/**
 * Abstract terminal configuration panel implementation.
 */
public abstract class AbstractConfigurationPanel extends AbstractWizardConfigurationPanel implements IConfigurationPanel {
	// The selection
	private ISelection selection;

	/**
	 * Constructor.
	 *
	 * @param parentControl The parent control. Must not be <code>null</code>!
	 */
	public AbstractConfigurationPanel(BaseDialogPageControl parentControl) {
		super(parentControl);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.terminals.interfaces.IConfigurationPanel#setSelection(org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void setSelection(ISelection selection) {
		this.selection = selection;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.terminals.interfaces.IConfigurationPanel#getSelection()
	 */
	@Override
	public ISelection getSelection() {
	    return selection;
	}

	/**
     * Returns the host name or IP from the current selection.
     *
     * @return The host name or IP.
     */
    public String getSelectionHost() {
    	ISelection selection = getSelection();
    	final AtomicReference<String> result = new AtomicReference<String>();
    	if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
    		Object element = ((IStructuredSelection) selection).getFirstElement();
    		if (element instanceof IPeerModel) {
    			final IPeerModel peerModel = (IPeerModel) element;
    			if (Protocol.isDispatchThread()) {
    				result.set(peerModel.getPeer().getAttributes().get(IPeer.ATTR_IP_HOST));
    			}
    			else {
    				Protocol.invokeAndWait(new Runnable() {
    					@Override
    					public void run() {
    						result.set(peerModel.getPeer().getAttributes().get(IPeer.ATTR_IP_HOST));
    					}
    				});
    			}
    		}
    	}
    
    	return result.get();
    }

}
