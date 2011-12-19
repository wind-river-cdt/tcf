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

import java.util.Map;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.tcf.te.runtime.services.ServiceManager;
import org.eclipse.tcf.te.runtime.services.interfaces.IPropertiesAccessService;
import org.eclipse.tcf.te.runtime.services.interfaces.constants.IPropertiesAccessServiceConstants;
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
     * @return The host name or IP, or <code>null</code>.
     */
    public String getSelectionHost() {
    	ISelection selection = getSelection();
    	if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
    		Object element = ((IStructuredSelection) selection).getFirstElement();
    		IPropertiesAccessService service = ServiceManager.getInstance().getService(element, IPropertiesAccessService.class);
    		if (service != null) {
    			Map<String, String> props = service.getTargetAddress(element);
    			if (props != null && props.containsKey(IPropertiesAccessServiceConstants.PROP_ADDRESS)) {
    				return props.get(IPropertiesAccessServiceConstants.PROP_ADDRESS);
    			}
    		}
    	}

    	return null;
    }

}
