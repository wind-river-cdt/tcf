/*******************************************************************************
 * Copyright (c) 2007, 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.internal.cdt.ui.breakpoints;
 
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.tcf.internal.cdt.ui.Activator;
import org.eclipse.tcf.internal.cdt.ui.preferences.PreferenceConstants;
import org.eclipse.tcf.internal.debug.model.ITCFConstants;
import org.eclipse.tcf.internal.debug.ui.model.TCFNode;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Toggle breakpoints target factory.
 * We use a separate factory so that we can control it through an action set.
 *
 */
 
public class TCFToggleBreakpointsTargetFactory implements IToggleBreakpointsTargetFactory {

    public static final String ID_TCF_BREAKPOINT_TOGGLE_TARGET = ITCFConstants.ID_TCF_DEBUG_MODEL + ".toggleTCFBreakpoint";
    
    private static final Set<String> BP_TOGGLE_TYPE_SET = new HashSet<String>(2);

    static {
        BP_TOGGLE_TYPE_SET.add(ID_TCF_BREAKPOINT_TOGGLE_TARGET);
    }

    private Map<String, TCFToggleBreakpointAdapter> fAdapterMap = new HashMap<String, TCFToggleBreakpointAdapter>();
    
    public IToggleBreakpointsTarget createToggleTarget(String targetID) {
        TCFToggleBreakpointAdapter adapter = null;         
        if ( ID_TCF_BREAKPOINT_TOGGLE_TARGET.equals(targetID)) {
            if (fAdapterMap != null && !fAdapterMap.isEmpty()) {
                adapter = fAdapterMap.get(ID_TCF_BREAKPOINT_TOGGLE_TARGET);
            }
            if (adapter == null) {
                adapter = new TCFToggleBreakpointAdapter(ID_TCF_BREAKPOINT_TOGGLE_TARGET);
                fAdapterMap.put(ID_TCF_BREAKPOINT_TOGGLE_TARGET, adapter);                    
            }            
        }
        return adapter;
    }

    public String getToggleTargetDescription(String targetID) {
        if (ID_TCF_BREAKPOINT_TOGGLE_TARGET.equals(targetID)) {
            String scope = getDefaultBPContextQuery();
            if (scope == null) scope = "None";
            return MessageFormat.format(Messages.TCFBreakpointToggle, scope);
        }
        return null;
    }

    public String getToggleTargetName(String targetID) {
        if (ID_TCF_BREAKPOINT_TOGGLE_TARGET.equals(targetID)) {
            String scope = getDefaultBPContextQuery();
            if (scope == null ) scope = "None";            
            return MessageFormat.format(Messages.TCFBreakpointToggle, scope);
        }
        return null;
    }

    public String getDefaultToggleTarget(IWorkbenchPart part, ISelection selection) {
        if (isTCFBreakpointActive(part, selection)) {
            return ID_TCF_BREAKPOINT_TOGGLE_TARGET;
        }
        return null;
    }
    
    public Set<String> getToggleTargets(IWorkbenchPart part, ISelection selection) {
        if (isTCFBreakpointActive(part, selection)) {
            return BP_TOGGLE_TYPE_SET;
        }
        return Collections.emptySet();
    }

    private static IStructuredSelection getDebugContext(IWorkbenchPart part) {
        ISelection selection = DebugUITools.getDebugContextManager().
            getContextService(part.getSite().getWorkbenchWindow()).getActiveContext();
        if (selection instanceof IStructuredSelection) {
            return (IStructuredSelection)selection;
        } 
        return StructuredSelection.EMPTY;
    }

    private static boolean isTCFBreakpointActive(IWorkbenchPart part, ISelection selection) {
        if (selection != null && !selection.isEmpty()) {
            // If the selection has the context data we want, use it.
            if (selection instanceof IStructuredSelection) {
                Object obj = ((IStructuredSelection)selection).getFirstElement();
                if (obj instanceof TCFNode) {
                    return true;
                }
            }
        }
        if (part != null) {
            // Get the debug context from the WorkbenchPart.
            Object obj = getDebugContext(part).getFirstElement();
            if (obj instanceof TCFNode) {
                return true;
            }
        }
        return false;
    }

    private static boolean isDefaultBPContextQueryEnabled() {
        return Platform.getPreferencesService().getBoolean(
                Activator.PLUGIN_ID,
                PreferenceConstants.PREF_DEFAULT_TRIGGER_SCOPE_ENABLED,
                false,
                null);
    }

    private static String getDefaultBPContextQuery() {
        String result = null;
        if (isDefaultBPContextQueryEnabled()) {
            result = Platform.getPreferencesService().getString(
                    Activator.PLUGIN_ID,
                    PreferenceConstants.PREF_DEFAULT_TRIGGER_SCOPE,
                    null,
                    null);
        }
        return result;
    }    
}
