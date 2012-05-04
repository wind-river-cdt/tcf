/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems and others.
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
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointsListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.ui.AbstractBreakpointOrganizerDelegate;
import org.eclipse.tcf.internal.cdt.ui.Activator;
import org.eclipse.tcf.internal.debug.model.ITCFConstants;
import org.eclipse.tcf.internal.debug.model.TCFBreakpointsModel;

/**
 * 
 */
public class BreakpointScopeOrganizer extends AbstractBreakpointOrganizerDelegate implements IBreakpointsListener {

    private static IAdaptable[] EMPTY_CATEGORY_ARRAY = new IAdaptable[0];

    public BreakpointScopeOrganizer() {
        DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);
    }
    
    public IAdaptable[] getCategories(IBreakpoint breakpoint) {
        IMarker marker = breakpoint.getMarker();
        if (marker != null) {
            String filter = marker.getAttribute(TCFBreakpointsModel.ATTR_CONTEXT_QUERY, null);
            if (filter != null) {
                return new IAdaptable[] { new BreakpointScopeCategory(filter) };
            }
        }
        return EMPTY_CATEGORY_ARRAY;
    }

    @Override
    public void dispose() {
        DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);
        super.dispose();
    }
    
    public void breakpointsAdded(IBreakpoint[] breakpoints) {
    }
    
    public void breakpointsChanged(IBreakpoint[] breakpoints, IMarkerDelta[] deltas) {
        boolean changed = false;
        
        for (IBreakpoint breakpoint : breakpoints) {
            IMarker marker = breakpoint.getMarker();
            if (marker != null && marker.getAttribute(TCFBreakpointsModel.ATTR_CONTEXT_QUERY, null) != null) {
                changed = true;
                break;
            }
        }
        if (changed) {
            fireCategoryChanged(null);
        }
    }
    
    public void breakpointsRemoved(IBreakpoint[] breakpoints, IMarkerDelta[] deltas) {
    }
    
    @Override
    public void addBreakpoint(IBreakpoint breakpoint, IAdaptable category) {
        if (category instanceof BreakpointScopeCategory && breakpoint instanceof ICBreakpoint) {
            String filter = ((BreakpointScopeCategory)category).getFilter();
            ICBreakpoint cBreakpoint = (ICBreakpoint) breakpoint;
            TCFBreakpointScopeExtension scopeExtension;
            try {
                scopeExtension = cBreakpoint.getExtension(
                        ITCFConstants.ID_TCF_DEBUG_MODEL, TCFBreakpointScopeExtension.class);
                if (scopeExtension != null) {
                    scopeExtension.setPropertiesFilter(filter);
                }
            }
            catch (CoreException e) {
                Activator.log(e);
            }
        }
    }
    
    @Override
    public boolean canAdd(IBreakpoint breakpoint, IAdaptable category) {
        return category instanceof BreakpointScopeCategory && breakpoint instanceof ICBreakpoint;
    }
    
    @Override
    public boolean canRemove(IBreakpoint breakpoint, IAdaptable category) {
        return breakpoint instanceof ICBreakpoint;
    }
    
    @Override
    public void removeBreakpoint(IBreakpoint breakpoint, IAdaptable category) {
        // Nothing to do, changes handled by add.
    }
}
