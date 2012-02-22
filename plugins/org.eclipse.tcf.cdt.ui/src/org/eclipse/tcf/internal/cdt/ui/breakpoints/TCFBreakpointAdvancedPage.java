/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.internal.cdt.ui.breakpoints;

import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPropertyPage;

public class TCFBreakpointAdvancedPage extends FieldEditorPreferencePage implements IWorkbenchPropertyPage {

    private static final String TCF_BREAKPOINT_MODEL_ID = "org.eclipse.tcf.debug";

    private static final String BreakOnPhysicalAddresses = "BreakOnPhysicalAddresses";

    private static final String BreakOnPhysAttribute = TCF_BREAKPOINT_MODEL_ID + "." + BreakOnPhysicalAddresses;

    private   Button phyMemBp;
    
    private IAdaptable fElement;


    public TCFBreakpointAdvancedPage() {
        super(GRID);
        noDefaultAndApplyButton();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
     */
    protected void createFieldEditors() {
       
    }

    private void createPhysMemField(Composite parent) {
        phyMemBp = new Button(parent, SWT.CHECK);
        phyMemBp.setSelection(getBreakpoint().getMarker().getAttribute(BreakOnPhysAttribute, false));
        phyMemBp.setText(Messages.TCFBreakpointAdvancedPage_0);
    }
    
    protected Control createContents(Composite parent) {
        noDefaultAndApplyButton();
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        createPhysMemField(composite);
        setValid(true);
        
        return composite;
    }
    
    /**
     * This method provides the currently selected breakpoint
     * @return the selected breakpoint
     */
    
    private ICBreakpoint getBreakpoint() {
        IAdaptable element = getElement();
        return (element instanceof ICBreakpoint) ? (ICBreakpoint)element : (ICBreakpoint)element.getAdapter(ICBreakpoint.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPropertyPage#getElement()
     */
    public IAdaptable getElement() {
        return fElement;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.IWorkbenchPropertyPage#setElement(org.eclipse.core.runtime
     * .IAdaptable)
     */
    public void setElement(IAdaptable element) {
        fElement = element;
    }

    public boolean performOk() {        
        boolean result = super.performOk();
        setBreakpointPhyMemProperties();
        return result;
    }

    protected void setBreakpointPhyMemProperties() {
        
        IWorkspaceRunnable wr = new IWorkspaceRunnable() {

            public void run( IProgressMonitor monitor ) throws CoreException {
                ICBreakpoint breakpoint = getBreakpoint();
                IMarker bpMarker = breakpoint.getMarker();
                boolean currentSelection =  phyMemBp.getSelection();
                if (bpMarker.getAttribute(BreakOnPhysAttribute, false) != currentSelection) {
                    try {
                        bpMarker.setAttribute(BreakOnPhysAttribute, currentSelection);
                    } catch (CoreException e) {
                        CDebugUIPlugin.log(e);
                    }
                }
            }
        };
        
        try {
            ResourcesPlugin.getWorkspace().run( wr, null );
        }
        catch( CoreException ce ) {
            CDebugUIPlugin.log( ce );
        }
    }
}
