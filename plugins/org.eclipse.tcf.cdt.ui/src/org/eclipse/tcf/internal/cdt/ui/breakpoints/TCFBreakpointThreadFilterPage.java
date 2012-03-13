/*******************************************************************************
 * Copyright (c) 2004, 2011 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems - Adapted to TCF
 *******************************************************************************/
package org.eclipse.tcf.internal.cdt.ui.breakpoints;

import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.ui.breakpoints.ICBreakpointContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.tcf.internal.debug.model.ITCFConstants;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * Property page to define the scope of a breakpoint.
 */
public class TCFBreakpointThreadFilterPage extends PropertyPage {

    private TCFBreakpointScopeExtension fFilterExtension;
    private TCFThreadFilterEditor fThreadFilterEditor;

    @Override
    protected Control createContents(Composite parent) {
        noDefaultAndApplyButton();
        Composite mainComposite = new Composite(parent, SWT.NONE);
        mainComposite.setFont(parent.getFont());
        mainComposite.setLayout(new GridLayout());
        mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        createThreadFilterEditor(mainComposite);
        setValid(true);
        return mainComposite;
    }

    protected ICBreakpoint getBreakpoint() {
        if (getElement() instanceof ICBreakpointContext) {
            return ((ICBreakpointContext)getElement()).getBreakpoint();
        }
        return (ICBreakpoint) getElement().getAdapter(ICBreakpoint.class);
    }

    public IPreferenceStore getPreferenceStore() {
        IAdaptable element = getElement();
        if (element instanceof ICBreakpointContext) {
            return ((ICBreakpointContext)element).getPreferenceStore();
        }
        return getContainer().getPreferenceStore();
    }

    
    protected TCFBreakpointScopeExtension getFilterExtension() {
        if (fFilterExtension != null) return fFilterExtension;
        
        ICBreakpoint bp = getBreakpoint();
        if (bp != null) {
            try {
                fFilterExtension = bp.getExtension(
                        ITCFConstants.ID_TCF_DEBUG_MODEL, TCFBreakpointScopeExtension.class);
            } catch (CoreException e) {
                // potential race condition: ignore
            }
            if (fFilterExtension == null) {
                fFilterExtension = new TCFBreakpointScopeExtension();
                fFilterExtension.initialize(getPreferenceStore());
            }
            return fFilterExtension;
        }
        return null;
    }

    protected void createThreadFilterEditor(Composite parent) {
        fThreadFilterEditor = new TCFThreadFilterEditor(parent, this);
    }

    protected TCFThreadFilterEditor getThreadFilterEditor() {
        return fThreadFilterEditor;
    }

    @Override
    public boolean performOk() {
        doStore();
        return super.performOk();
    }

    /**
     * Stores the values configured in this page.
     */
    protected void doStore() {
        fThreadFilterEditor.doStore();
    }
}
