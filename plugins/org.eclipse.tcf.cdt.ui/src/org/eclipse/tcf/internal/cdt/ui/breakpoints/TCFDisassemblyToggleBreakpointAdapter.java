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

import java.util.Map;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.model.ICBreakpointType;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.dsf.debug.ui.actions.AbstractDisassemblyBreakpointsTarget;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Toggles a TCF Scoped breakpoint in a C/C++ editor.
 */
public class TCFDisassemblyToggleBreakpointAdapter extends AbstractDisassemblyBreakpointsTarget {

    private final String TOGGLE_TYPE;
    private IWorkbenchPart fTogglePart;


    TCFDisassemblyToggleBreakpointAdapter(String toggle_type ) {
        TOGGLE_TYPE = toggle_type;
    }

    @Override
    public void toggleLineBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
        // Save the workbench part that toggle is invoked on, use it in {@link #createLineBreakpoint}
        fTogglePart = part;
        super.toggleLineBreakpoints(part, selection);
        fTogglePart = null;
    }

    @Override
    protected void createLineBreakpoint( String sourceHandle, IResource resource, int lineNumber ) throws CoreException {
        CDIDebugModel.createLineBreakpoint(
                sourceHandle, resource, getBreakpointType(), lineNumber, true, 0, "", true ); //$NON-NLS-1$
        Map<String, Object> attributes = TCFToggleBreakpointAdapter.getDefaultAttributes(fTogglePart, TOGGLE_TYPE);
        ICLineBreakpoint lineBp = CDIDebugModel.createBlankLineBreakpoint();
        CDIDebugModel.setLineBreakpointAttributes(
                attributes, sourceHandle, getBreakpointType(), lineNumber, true, 0, "" ); //$NON-NLS-1$
        CDIDebugModel.createBreakpointMarker(lineBp, resource, attributes, true);
    }

    @Override
    protected void createLineBreakpointInteractive(IWorkbenchPart part, String sourceHandle, IResource resource, int lineNumber) throws CoreException {
        ICLineBreakpoint lineBp = CDIDebugModel.createBlankLineBreakpoint();
        Map<String, Object> attributes = TCFToggleBreakpointAdapter.getDefaultAttributes(fTogglePart, TOGGLE_TYPE);
        CDIDebugModel.setLineBreakpointAttributes(
                attributes, sourceHandle, getBreakpointType(), lineNumber, true, 0, ""); //$NON-NLS-1$
        openBreakpointPropertiesDialog(lineBp, part, resource, attributes);
    }

    @Override
    protected void createAddressBreakpoint(IResource resource, IAddress address) throws CoreException {
        CDIDebugModel.createAddressBreakpoint(null, null, resource, getBreakpointType(), address, true, 0, "", true); //$NON-NLS-1$
    }

    @Override
    protected void createAddressBreakpointInteractive(IWorkbenchPart part, IResource resource, IAddress address) throws CoreException {
        ICLineBreakpoint lineBp = CDIDebugModel.createBlankAddressBreakpoint();
        Map<String, Object> attributes = TCFToggleBreakpointAdapter.getDefaultAttributes(fTogglePart, TOGGLE_TYPE);
        CDIDebugModel.setAddressBreakpointAttributes(
                attributes, null, null, getBreakpointType(), -1, address, true, 0, ""); //$NON-NLS-1$
        openBreakpointPropertiesDialog(lineBp, part, resource, attributes);
    }

    protected int getBreakpointType() {
        return ICBreakpointType.REGULAR;
    }

}
