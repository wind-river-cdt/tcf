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

import java.math.BigInteger;
import java.util.HashMap;

import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.model.ICBreakpointType;
import org.eclipse.cdt.debug.core.model.ICEventBreakpoint;
import org.eclipse.cdt.debug.core.model.ICFunctionBreakpoint;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.core.model.ICWatchpoint;
import org.eclipse.cdt.debug.ui.breakpoints.AbstractToggleBreakpointAdapter;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.tcf.internal.cdt.ui.Activator;
import org.eclipse.tcf.internal.cdt.ui.preferences.PreferenceConstants;
import org.eclipse.tcf.internal.debug.model.ITCFConstants;
import org.eclipse.tcf.internal.debug.model.TCFBreakpointsModel;
import org.eclipse.tcf.internal.debug.ui.model.TCFNode;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Toggles a TCF Scoped breakpoint in a C/C++ editor.
 */
public class TCFToggleBreakpointAdapter extends AbstractToggleBreakpointAdapter {

    private final String TOGGLE_TYPE;
    private bpAttributes attributes;

    TCFToggleBreakpointAdapter(String toggle_type ) {
        TOGGLE_TYPE = toggle_type;
        attributes = new bpAttributes();
    }

    private class bpAttributes {
        private HashMap<String, Object> attributes;
        private String error;

        bpAttributes() {
            attributes = new HashMap<String, Object>();
            error = null;
        }

        public void put (String str, Object obj) {
            attributes.put(str, obj);
        }

        public HashMap<String, Object> getMap() {
            return attributes;
        }

        public void clear() {
            attributes.clear();
        }

        public String getError() {
            return error;
        }
    }

    private static IStructuredSelection getDebugContext(IWorkbenchPart part) {
        ISelection selection = DebugUITools.getDebugContextManager().
            getContextService(part.getSite().getWorkbenchWindow()).getActiveContext();
        if (selection instanceof IStructuredSelection) {
            return (IStructuredSelection)selection;
        }
        return StructuredSelection.EMPTY;
    }

    private static boolean isDefaultBPContextQueryEnabled() {
        return Platform.getPreferencesService().getBoolean(
                Activator.PLUGIN_ID,
                PreferenceConstants.PREF_DEFAULT_TRIGGER_SCOPE_ENABLED,
                false,
                null);
    }

    private static String getDefaultBPContextQuery() {
        return Platform.getPreferencesService().getString(
                Activator.PLUGIN_ID,
                PreferenceConstants.PREF_DEFAULT_TRIGGER_SCOPE,
                null,
                null);
    }

    private static Boolean checkToggleType ( IWorkbenchPart part, final String toggleType, final bpAttributes attributes) {
        Boolean bFoundError = false;
        if ( part != null ) {
            Object obj = getDebugContext(part).getFirstElement();
            if ( obj instanceof TCFNode ) {
                if ( toggleType.length() != 0) {
                    attributes.clear();
                    if (isDefaultBPContextQueryEnabled() == true) {
                        String query = getDefaultBPContextQuery();
                        attributes.put(TCFBreakpointsModel.ATTR_CONTEXT_QUERY, query);
                    }
                }
            }
        }
        return bFoundError;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractToggleBreakpointAdapter#findLineBreakpoint(java.lang.String, org.eclipse.core.resources.IResource, int)
     */
    @Override
    protected ICLineBreakpoint findLineBreakpoint( String sourceHandle, IResource resource, int lineNumber ) throws CoreException {
            return CDIDebugModel.lineBreakpointExists( sourceHandle, resource, lineNumber );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractToggleBreakpointAdapter#createLineBreakpoint(java.lang.String, org.eclipse.core.resources.IResource, int)
     */
    @Override
    protected void createLineBreakpoint( boolean interactive, IWorkbenchPart part, final String sourceHandle, final IResource resource, final int lineNumber ) throws CoreException {
        Boolean bFoundError = checkToggleType(part, TOGGLE_TYPE, attributes);
        if (!bFoundError ) {
            ICLineBreakpoint lineBp = CDIDebugModel.createBlankLineBreakpoint();
            CDIDebugModel.setLineBreakpointAttributes(
                    attributes.getMap(), sourceHandle, getBreakpointType(), lineNumber, true, 0, "" ); //$NON-NLS-1$
            if ( !interactive ) {
                CDIDebugModel.createBreakpointMarker(lineBp, resource, attributes.getMap(), true);
            }
            else {
                openBreakpointPropertiesDialog(lineBp, part, resource, attributes.getMap());
            }
        }
        else {
            // Throw an error to the user.
            IStatus error = new Status(IStatus.ERROR, ITCFConstants.ID_TCF_DEBUG_MODEL, attributes.getError(), null);
            ErrorDialog.openError(null, Messages.TCFBreakpointToggleError, null, error);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractToggleBreakpointAdapter#findFunctionBreakpoint(java.lang.String, org.eclipse.core.resources.IResource, java.lang.String)
     */
    @Override
    protected ICFunctionBreakpoint findFunctionBreakpoint(
                    String sourceHandle,
                    IResource resource,
                    String functionName ) throws CoreException {
            return CDIDebugModel.functionBreakpointExists( sourceHandle, resource, functionName );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractToggleBreakpointAdapter#createFunctionBreakpoint(java.lang.String, org.eclipse.core.resources.IResource, java.lang.String, int, int, int)
     */
    @Override
    protected void createFunctionBreakpoint(
                    boolean interactive,
                    IWorkbenchPart part,
                    String sourceHandle,
                    IResource resource,
                    String functionName,
                    int charStart,
                    int charEnd,
                    int lineNumber ) throws CoreException {
            Boolean bFoundError = checkToggleType(part, TOGGLE_TYPE, attributes);
            if (!bFoundError) {
                ICFunctionBreakpoint bp = CDIDebugModel.createBlankFunctionBreakpoint();
                CDIDebugModel.setFunctionBreakpointAttributes( attributes.getMap(), sourceHandle, getBreakpointType(), functionName,
                        charStart, charEnd, lineNumber, true, 0, "" ); //$NON-NLS-1$
                if (!interactive) {
                    CDIDebugModel.createBreakpointMarker(bp, resource, attributes.getMap(), true);
                }
                else {
                    openBreakpointPropertiesDialog(bp, part, resource, attributes.getMap());
                }
            }
            else {
                // Throw an error to the user.
                IStatus error = new Status(IStatus.ERROR, ITCFConstants.ID_TCF_DEBUG_MODEL, attributes.getError(), null);
                ErrorDialog.openError(null, Messages.TCFBreakpointToggleError, null, error);
            }
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractToggleBreakpointAdapter#findWatchpoint(java.lang.String, org.eclipse.core.resources.IResource, java.lang.String)
     */
    @Override
    protected ICWatchpoint findWatchpoint( String sourceHandle, IResource resource, String expression ) throws CoreException {
            return CDIDebugModel.watchpointExists( sourceHandle, resource, expression );
    }

    protected int getBreakpointType() {
            return ICBreakpointType.REGULAR;
    }

    @Override
    protected void createWatchpoint(boolean interactive, IWorkbenchPart part, String sourceHandle, IResource resource, int charStart, int charEnd, int lineNumber,
            String expression, String memorySpace, String range) throws CoreException {
        Boolean bFoundError = checkToggleType(part, TOGGLE_TYPE, attributes);
        if (!bFoundError) {
            ICWatchpoint bp = CDIDebugModel.createBlankWatchpoint();
            CDIDebugModel.setWatchPointAttributes(attributes.getMap(), sourceHandle, resource, true, false,
                expression, memorySpace, new BigInteger(range), true, 0, ""); //$NON-NLS-1$
            openBreakpointPropertiesDialog(bp, part, resource, attributes.getMap());
        }
        else {
            // Throw an error to the user.
            IStatus error = new Status(IStatus.ERROR, ITCFConstants.ID_TCF_DEBUG_MODEL, attributes.getError(), null);
            ErrorDialog.openError(null, Messages.TCFBreakpointToggleError, null, error);
        }
    }

    @Override
    protected void createEventBreakpoint(boolean interactive, IWorkbenchPart part, IResource resource, String type, String arg) throws CoreException {
        Boolean bFoundError = checkToggleType(part, TOGGLE_TYPE, attributes);
        if (!bFoundError) {
            ICEventBreakpoint bp = CDIDebugModel.createBlankEventBreakpoint();
            CDIDebugModel.setEventBreakpointAttributes(attributes.getMap(),type, arg);
            openBreakpointPropertiesDialog(bp, part, resource, attributes.getMap());
        }
        else {
            // Throw an error to the user.
            IStatus error = new Status(IStatus.ERROR, ITCFConstants.ID_TCF_DEBUG_MODEL, attributes.getError(), null);
            ErrorDialog.openError(null, Messages.TCFBreakpointToggleError, null, error);
        }
    }
}
