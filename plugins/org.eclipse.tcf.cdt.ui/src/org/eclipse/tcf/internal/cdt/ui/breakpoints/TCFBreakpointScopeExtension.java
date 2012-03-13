/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others.
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
import org.eclipse.cdt.debug.core.model.ICBreakpointExtension;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.tcf.internal.cdt.ui.Activator;
import org.eclipse.tcf.internal.debug.model.TCFBreakpointsModel;

public class TCFBreakpointScopeExtension implements ICBreakpointExtension {

    private String[] fContextIds;
    private IPreferenceStore fPreferenceStore;
    private ICBreakpoint fBreakpoint;

    public void initialize(IPreferenceStore prefStore) {
        fPreferenceStore = prefStore;
        String contextIdAttr = getContextAttr();
        if (contextIdAttr != null) fContextIds = contextIdAttr.split(",\\s*");
    }

    public void initialize(ICBreakpoint breakpoint) throws CoreException {
        fBreakpoint = breakpoint;
        String contextIdAttr = getContextAttr();
        if (contextIdAttr != null) fContextIds = contextIdAttr.split(",\\s*");
    }

    private String getContextAttr() {
        if (fPreferenceStore!= null) {
            return fPreferenceStore.getString(TCFBreakpointsModel.ATTR_CONTEXTIDS);
        }
        else if (fBreakpoint != null) {
            IMarker m = fBreakpoint.getMarker();
            if (m != null && m.exists()) {
                return m.getAttribute(TCFBreakpointsModel.ATTR_CONTEXTIDS, null);
            }
        }
        return null;
    }
    
    private void setContextAttr(final String attr) {
        if (fPreferenceStore!= null) {
            fPreferenceStore.setValue(TCFBreakpointsModel.ATTR_CONTEXTIDS, attr);
        }    
        else if (fBreakpoint != null) {
            final IMarker m = fBreakpoint.getMarker();
            try {
                ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
                    public void run(IProgressMonitor monitor) throws CoreException {
                        m.setAttribute(TCFBreakpointsModel.ATTR_CONTEXTIDS, attr);
                    }
                }, null);
            }
            catch (Exception e) {
                Activator.log(e);
            }
        }
    }
    
    public void setThreadFilter(String[] threadIds) {
        fContextIds = threadIds;
        String attr = "";
        if (fContextIds != null) {
            if (fContextIds.length == 0) {
                // empty string is filtered out in TCFBreakpointsModel
                attr = " ";
            }
            else {
                StringBuilder buf = new StringBuilder();
                for (int i = 0; i < fContextIds.length - 1; i++) {
                    buf.append(fContextIds[i]).append(',');
                }
                buf.append(fContextIds[fContextIds.length - 1]);
                attr = buf.toString();
            }
        }
        setContextAttr(attr);
    }

    public String[] getThreadFilters() {
        return fContextIds;
    }
}
