/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.internal.debug.model;

import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.Breakpoint;
import org.eclipse.tcf.internal.debug.Activator;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.services.IBreakpoints;


public class TCFBreakpoint extends Breakpoint {

    public static final String MARKER_TYPE = "org.eclipse.tcf.debug.breakpoint.marker";

    private static long last_id = 0;

    private static String createNewID() {
        assert Protocol.isDispatchThread();
        long id = System.currentTimeMillis();
        if (id <= last_id) id = last_id + 1;
        last_id = id;
        return Long.toHexString(id);
    }

    public static TCFBreakpoint createFromMarkerAttributes(Map<String,Object> attrs) throws CoreException {
        assert !Protocol.isDispatchThread();
        assert attrs.get(TCFBreakpointsModel.ATTR_ID) != null;
        TCFBreakpoint bp = new TCFBreakpoint();
        IResource resource = ResourcesPlugin.getWorkspace().getRoot();
        IMarker marker = resource.createMarker(MARKER_TYPE);
        bp.setMarker(marker);
        marker.setAttributes(attrs);
        DebugPlugin.getDefault().getBreakpointManager().addBreakpoint(bp);
        return bp;
    }

    public static TCFBreakpoint createFromTCFProperties(Map<String,Object> props) {
        assert Protocol.isDispatchThread();
        if (props.get(IBreakpoints.PROP_ID) == null) props.put(IBreakpoints.PROP_ID, createNewID());
        final TCFBreakpoint bp = new TCFBreakpoint();
        final Map<String,Object> m = Activator.getBreakpointsModel().toMarkerAttributes(props);
        final IResource resource = ResourcesPlugin.getWorkspace().getRoot();
        final ISchedulingRule rule = bp.getMarkerRule(resource);
        Job job = new WorkspaceJob("Add Breakpoint") {  //$NON-NLS-1$
            @Override
            public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
                IMarker marker = resource.createMarker(MARKER_TYPE);
                bp.setMarker(marker);
                marker.setAttributes(m);
                DebugPlugin.getDefault().getBreakpointManager().addBreakpoint(bp);
                return Status.OK_STATUS;
            }
        };
        job.setRule(rule);
        job.setPriority(Job.SHORT);
        job.setSystem(true);
        job.schedule();
        return bp;
    }

    public TCFBreakpoint() {
    }

    @Override
    public void setEnabled(boolean b) throws CoreException {
        if (!Activator.getBreakpointsModel().isLocal(getMarker())) return;
        super.setEnabled(b);
    }

    public String getModelIdentifier() {
        return ITCFConstants.ID_TCF_DEBUG_MODEL;
    }

    public void notifyStatusChaged() throws CoreException {
        IMarker marker = getMarker();
        if (marker == null) return;
        int cnt = 0;
        String status = marker.getAttribute(TCFBreakpointsModel.ATTR_STATUS, null);
        if (status != null) cnt = Integer.parseInt(status);
        setAttribute(TCFBreakpointsModel.ATTR_STATUS, Integer.toString(cnt + 1));
    }
}
