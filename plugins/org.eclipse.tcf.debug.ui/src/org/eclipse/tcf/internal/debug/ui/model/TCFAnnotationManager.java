/*******************************************************************************
 * Copyright (c) 2008, 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.internal.debug.ui.model;

import java.io.File;
import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.core.model.ICLineBreakpoint2;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.ISourcePresentation;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tcf.internal.debug.launch.TCFSourceLookupDirector;
import org.eclipse.tcf.internal.debug.model.ITCFBreakpointListener;
import org.eclipse.tcf.internal.debug.model.TCFBreakpointsModel;
import org.eclipse.tcf.internal.debug.model.TCFBreakpointsStatus;
import org.eclipse.tcf.internal.debug.model.TCFContextState;
import org.eclipse.tcf.internal.debug.model.TCFLaunch;
import org.eclipse.tcf.internal.debug.model.TCFSourceRef;
import org.eclipse.tcf.internal.debug.ui.Activator;
import org.eclipse.tcf.internal.debug.ui.ImageCache;
import org.eclipse.tcf.protocol.JSON;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.services.IBreakpoints;
import org.eclipse.tcf.services.ILineNumbers;
import org.eclipse.tcf.services.IRunControl;
import org.eclipse.tcf.util.TCFDataCache;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class TCFAnnotationManager {

    private static final String
        TYPE_BP_INSTANCE = "org.eclipse.tcf.debug.breakpoint_instance",
        TYPE_TOP_FRAME = "org.eclipse.tcf.debug.top_frame",
        TYPE_STACK_FRAME = "org.eclipse.tcf.debug.stack_frame";

    class TCFAnnotation extends Annotation {

        final String ctx;
        final ILineNumbers.CodeArea area;
        final Image image;
        final String text;
        final String type;
        final int hash_code;

        IAnnotationModel model;

        TCFAnnotation(String ctx, ILineNumbers.CodeArea area, Image image, String text, String type) {
            this.ctx = ctx;
            this.area = area;
            this.image = image;
            this.text = text;
            this.type = type;
            hash_code = area.hashCode() + image.hashCode() + text.hashCode() + type.hashCode();
            setText(text);
            setType(type);
        }

        protected Image getImage() {
            return image;
        }

        void dispose() {
            assert Thread.currentThread() == display.getThread();
            if (model != null) {
                model.removeAnnotation(this);
                model = null;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof TCFAnnotation)) return false;
            TCFAnnotation a = (TCFAnnotation)o;
            if (!ctx.equals(a.ctx)) return false;
            if (!area.equals(a.area)) return false;
            if (!image.equals(a.image)) return false;
            if (!text.equals(a.text)) return false;
            if (!type.equals(a.type)) return false;
            return true;
        }

        @Override
        public int hashCode() {
            return hash_code;
        }

        @Override
        public String toString() {
            StringBuffer bf = new StringBuffer();
            bf.append('[');
            bf.append(area);
            bf.append(',');
            bf.append(text);
            bf.append(',');
            bf.append(type);
            bf.append(',');
            bf.append(model);
            bf.append(']');
            return bf.toString();
        }
    }

    private class WorkbenchWindowInfo {
        final LinkedList<TCFAnnotation> annotations = new LinkedList<TCFAnnotation>();
        final Map<IEditorInput,ITextEditor> editors = new HashMap<IEditorInput,ITextEditor>();

        Runnable update_task;
        TCFNode update_node;

        void dispose() {
            for (TCFAnnotation a : annotations) a.dispose();
            annotations.clear();
        }
    }

    private final HashMap<IWorkbenchWindow,WorkbenchWindowInfo> windows =
        new HashMap<IWorkbenchWindow,WorkbenchWindowInfo>();

    private final HashSet<IWorkbenchWindow> dirty_windows = new HashSet<IWorkbenchWindow>();
    private final HashSet<TCFLaunch> dirty_launches = new HashSet<TCFLaunch>();
    private final HashSet<TCFLaunch> changed_launch_cfgs = new HashSet<TCFLaunch>();

    private final TCFLaunch.LaunchListener launch_listener = new TCFLaunch.LaunchListener() {

        public void onCreated(TCFLaunch launch) {
        }

        public void onConnected(final TCFLaunch launch) {
            updateAnnotations(null, launch);
            TCFBreakpointsStatus bps = launch.getBreakpointsStatus();
            if (bps == null) return;
            bps.addListener(new ITCFBreakpointListener() {

                public void breakpointStatusChanged(String id) {
                    updateAnnotations(null, launch);
                }

                public void breakpointRemoved(String id) {
                    updateAnnotations(null, launch);
                }

                public void breakpointChanged(String id) {
                }
            });
        }

        public void onDisconnected(final TCFLaunch launch) {
            assert Protocol.isDispatchThread();
            updateAnnotations(null, launch);
        }

        public void onProcessOutput(TCFLaunch launch, String process_id, int stream_id, byte[] data) {
        }

        public void onProcessStreamError(TCFLaunch launch, String process_id,
                int stream_id, Exception error, int lost_size) {
        }
    };

    private final ISelectionListener selection_listener = new ISelectionListener() {

        public void selectionChanged(IWorkbenchPart part, ISelection selection) {
            updateAnnotations(part.getSite().getWorkbenchWindow(), (TCFLaunch)null);
            if (selection instanceof IStructuredSelection) {
                final Object obj = ((IStructuredSelection)selection).getFirstElement();
                if (obj instanceof TCFNodeStackFrame && ((TCFNodeStackFrame)obj).isTraceLimit()) {
                    Protocol.invokeLater(new Runnable() {
                        public void run() {
                            ((TCFNodeStackFrame)obj).riseTraceLimit();
                        }
                    });
                }
            }
        }
    };

    private final IWindowListener window_listener = new IWindowListener() {

        public void windowActivated(IWorkbenchWindow window) {
        }

        public void windowClosed(IWorkbenchWindow window) {
            assert windows.get(window) != null;
            window.getSelectionService().removeSelectionListener(
                    IDebugUIConstants.ID_DEBUG_VIEW, selection_listener);
            windows.remove(window).dispose();
        }

        public void windowDeactivated(IWorkbenchWindow window) {
        }

        public void windowOpened(IWorkbenchWindow window) {
            if (windows.get(window) != null) return;
            window.getSelectionService().addSelectionListener(
                    IDebugUIConstants.ID_DEBUG_VIEW, selection_listener);
            windows.put(window, new WorkbenchWindowInfo());
            updateAnnotations(window, (TCFLaunch)null);
        }
    };

    private final ILaunchConfigurationListener launch_conf_listener = new ILaunchConfigurationListener() {

        public void launchConfigurationAdded(ILaunchConfiguration cfg) {
        }

        public void launchConfigurationChanged(final ILaunchConfiguration cfg) {
            displayExec(new Runnable() {
                public void run() {
                    ILaunch[] arr = launch_manager.getLaunches();
                    for (ILaunch l : arr) {
                        if (l instanceof TCFLaunch) {
                            TCFLaunch t = (TCFLaunch)l;
                            if (cfg.equals(t.getLaunchConfiguration())) {
                                changed_launch_cfgs.add(t);
                                updateAnnotations(null, t);
                            }
                        }
                    }
                }
            });
        }

        public void launchConfigurationRemoved(ILaunchConfiguration cfg) {
        }
    };

    private final Display display = Display.getDefault();
    private final ILaunchManager launch_manager = DebugPlugin.getDefault().getLaunchManager();
    private int update_unnotations_cnt = 0;
    private boolean started;
    private boolean disposed;

    public TCFAnnotationManager() {
        assert Protocol.isDispatchThread();
        TCFLaunch.addListener(launch_listener);
        launch_manager.addLaunchConfigurationListener(launch_conf_listener);
        displayExec(new Runnable() {
            public void run() {
                if (!PlatformUI.isWorkbenchRunning() || PlatformUI.getWorkbench().isStarting()) {
                    display.timerExec(200, this);
                }
                else if (!PlatformUI.getWorkbench().isClosing()) {
                    started = true;
                    PlatformUI.getWorkbench().addWindowListener(window_listener);
                    for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
                        window_listener.windowOpened(window);
                    }
                    IWorkbenchWindow w = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                    if (w != null) window_listener.windowActivated(w);
                }
            }
        });
    }

    public void dispose() {
        if (disposed) return;
        assert Protocol.isDispatchThread();
        disposed = true;
        launch_manager.removeLaunchConfigurationListener(launch_conf_listener);
        TCFLaunch.removeListener(launch_listener);
        displayExec(new Runnable() {
            public void run() {
                if (!started) return;
                PlatformUI.getWorkbench().removeWindowListener(window_listener);
                for (IWorkbenchWindow window : windows.keySet()) {
                    window.getSelectionService().removeSelectionListener(
                            IDebugUIConstants.ID_DEBUG_VIEW, selection_listener);
                    windows.get(window).dispose();
                }
                windows.clear();
            }
        });
    }

    private void displayExec(Runnable r) {
        synchronized (Device.class) {
            if (!display.isDisposed()) {
                display.asyncExec(r);
            }
        }
    }

    /**
     * Return breakpoint status info for all active TCF debug sessions.
     * @param breakpoint
     * @return breakpoint status as defined by TCF Breakpoints service.
     */
    Map<TCFLaunch,Map<String,Object>> getBreakpointStatus(IBreakpoint breakpoint) {
        assert Protocol.isDispatchThread();
        Map<TCFLaunch,Map<String,Object>> map = new HashMap<TCFLaunch,Map<String,Object>>();
        if (disposed) return null;
        ILaunch[] launches = DebugPlugin.getDefault().getLaunchManager().getLaunches();
        for (ILaunch l : launches) {
            if (l instanceof TCFLaunch) {
                TCFLaunch launch = (TCFLaunch)l;
                TCFBreakpointsStatus bs = launch.getBreakpointsStatus();
                if (bs != null) map.put(launch, bs.getStatus(breakpoint));
            }
        }
        return map;
    }

    /**
     * Return breakpoint status text for all active TCF debug sessions.
     * @param breakpoint
     * @return breakpoint status as a string.
     */
    @SuppressWarnings("unchecked")
    String getBreakpointStatusText(IBreakpoint breakpoint) {
        assert Protocol.isDispatchThread();
        String error = null;
        for (Map<String,Object> map : getBreakpointStatus(breakpoint).values()) {
            if (map != null) {
                String s = (String)map.get(IBreakpoints.STATUS_ERROR);
                if (s != null && error == null) error = s;
                Object planted = map.get(IBreakpoints.STATUS_INSTANCES);
                if (planted != null) {
                    Collection<Map<String,Object>> list = (Collection<Map<String,Object>>)planted;
                    for (Map<String,Object> m : list) {
                        if (m.get(IBreakpoints.INSTANCE_ERROR) == null) {
                            return Messages.TCFAnnotationManager_3;
                        }
                    }
                }
            }
        }
        return error;
    }

    @SuppressWarnings("unchecked")
    private Object[] toObjectArray(Object o) {
        if (o == null) return null;
        Collection<Object> c = (Collection<Object>)o;
        return (Object[])c.toArray(new Object[c.size()]);
    }

    @SuppressWarnings("unchecked")
    private Map<String,Object> toObjectMap(Object o) {
        return (Map<String,Object>)o;
    }

    private void addBreakpointErrorAnnotation(List<TCFAnnotation> set, TCFLaunch launch, String ctx, String id, String error) {
        Map<String,Object> props = launch.getBreakpointsStatus().getProperties(id);
        if (props != null) {
            String file = (String)props.get(IBreakpoints.PROP_FILE);
            Number line = (Number)props.get(IBreakpoints.PROP_LINE);
            if (file != null && line != null) {
                ILineNumbers.CodeArea area = new ILineNumbers.CodeArea(null, file,
                        line.intValue(), 0, line.intValue() + 1, 0,
                        null, null, 0, false, false, false, false);
                TCFAnnotation a = new TCFAnnotation(ctx, area,
                        ImageCache.getImage(ImageCache.IMG_BREAKPOINT_ERROR),
                        MessageFormat.format(Messages.TCFAnnotationManager_4, error),
                        TYPE_BP_INSTANCE);
                set.add(a);
            }
        }
    }

    /**
     * Add/Modify marker properties for a given breakpoint.
     * 
     * @param  markerAttrs - Attributes to adjust for marker
     * @param  bp - breakpoint to update marker data.
     */    
    private void updateMarkerAttributes(final Map<String, Object> markerAttrs, final IBreakpoint bp) {
        final IMarker marker = bp.getMarker();
        IResource resources = marker.getResource();
        ISchedulingRule changeRule = null;
        IResourceRuleFactory ruleFactory = ResourcesPlugin.getWorkspace().getRuleFactory();
        ISchedulingRule markerRule = ruleFactory.markerRule(resources);
        changeRule = MultiRule.combine(changeRule, markerRule);
        
        WorkspaceJob job = new WorkspaceJob(Messages.TCFAnnotationManager_5) { //$NON_NLS-1$
            @Override
            public IStatus runInWorkspace(IProgressMonitor monitor) {
                Map<?,?> oldAttrs = null;
                List<String> keys = new ArrayList<String>(markerAttrs.size());
                List<Object> values = new ArrayList<Object>(markerAttrs.size());
                try {
                    oldAttrs = marker.getAttributes();
                }
                catch (CoreException e) {
                    Activator.log(Messages.TCFAnnotationManager_11, e);
                    e.printStackTrace();
                }
                for (Map.Entry<?,?> entry : markerAttrs.entrySet()) {
                    String key = (String) entry.getKey();
                    Object newVal = entry.getValue();
                    Object oldVal = oldAttrs.remove(key);
                    if (oldVal == null || !oldVal.equals(newVal)) {
                        keys.add(key);
                        values.add(newVal);
                    }
                }
                if (keys.size() != 0) {
                    String[] keyArr = (String[]) keys.toArray(new String[keys.size()]);
                    Object[] valueArr = (Object[]) values.toArray(new Object[values.size()]);
                    try {
                        marker.setAttributes(keyArr, valueArr);
                    }
                    catch (CoreException e) {
                        Activator.log(Messages.TCFAnnotationManager_11, e);                 
                        e.printStackTrace();
                    }                    
                }
                try {
                    ICBreakpoint cbp = (ICBreakpoint)bp;
                    cbp.refreshMessage();
                }
                catch (CoreException e) {
                    IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                            NLS.bind(Messages.TCFAnnotationManager_7, null ),
                            e);                                
                    Activator.getDefault().getLog().log(status);                        
                    e.printStackTrace();
                }
                
                return Status.OK_STATUS;
            }
        };
        job.setPriority(Job.INTERACTIVE);
        job.setSystem(true);
        job.setRule(changeRule);
        job.schedule();
    }    
    
    /**
     * If a line BP was specified, and the lines do not match, update the marker
     * 
     * @param  bp - Registered Breakpoint 
     * @param  area - Resolved location for status
     */    
    private void updateBPMarker(final IBreakpoint bp, final ILineNumbers.CodeArea area ) {
        try {
            if ( !(bp instanceof ICLineBreakpoint2) ||
                 area.start_line == ((ICLineBreakpoint2) bp).getLineNumber()) 
            {
                return; // No line change
            }
        } catch (CoreException e) {
            IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    NLS.bind(Messages.TCFAnnotationManager_6, IMarker.LINE_NUMBER, bp.toString()),
                    e);                                
            Activator.getDefault().getLog().log(status);
            return; // Breakpoint being disposed.
        }

        IResource resources = bp.getMarker().getResource();
        ISchedulingRule changeRule = null;
        IResourceRuleFactory ruleFactory = ResourcesPlugin.getWorkspace().getRuleFactory();
        ISchedulingRule markerRule = ruleFactory.markerRule(resources);
        changeRule = MultiRule.combine(changeRule, markerRule);
        
        WorkspaceJob job = new WorkspaceJob(Messages.TCFAnnotationManager_5) { //$NON_NLS-1$
            @Override
            public IStatus runInWorkspace(IProgressMonitor monitor) {
                try {
                    ((ICLineBreakpoint2)bp).setInstalledLine(area.start_line);
                }
                catch (CoreException e) {
                    IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                            NLS.bind(Messages.TCFAnnotationManager_6, IMarker.LINE_NUMBER, bp.toString()),
                            e);                                
                    Activator.getDefault().getLog().log(status);
                }
                return Status.OK_STATUS;
            }
        };
        job.setPriority(Job.INTERACTIVE);
        job.setSystem(true);
        job.setRule(changeRule);
        job.schedule();
    }
    
    private void updateAnnotations(IWorkbenchWindow window, TCFNode node, List<TCFAnnotation> set) {
        if (disposed) return;
        assert Thread.currentThread() == display.getThread();
        WorkbenchWindowInfo win_info = windows.get(window);
        if (win_info == null) return;

        Map<IEditorInput,ITextEditor> editors = new HashMap<IEditorInput,ITextEditor>();
        for (IEditorReference ref : window.getActivePage().getEditorReferences()) {
            IEditorPart part = ref.getEditor(false);
            if (!(part instanceof ITextEditor)) continue;
            ITextEditor editor = (ITextEditor)part;
            editors.put(editor.getEditorInput(), editor);
        }
        boolean flush_all = node == null || !editors.equals(win_info.editors) || changed_launch_cfgs.contains(node.launch);
        Iterator<TCFAnnotation> i = win_info.annotations.iterator();
        while (i.hasNext()) {
            TCFAnnotation a = i.next();
            if (!flush_all && set != null && set.remove(a)) continue;
            a.dispose();
            i.remove();
        }
        if (set == null || set.size() == 0) return;
        win_info.editors.clear();
        win_info.editors.putAll(editors);
        ISourcePresentation presentation = TCFModelPresentation.getDefault();
        for (TCFAnnotation a : set) {
            Object source_element = TCFSourceLookupDirector.lookup(node.launch, a.ctx, a.area);
            if (source_element == null) continue;
            IEditorInput editor_input = presentation.getEditorInput(source_element);
            ITextEditor editor = editors.get(editor_input);
            if (editor == null) continue;
            IDocumentProvider doc_provider = editor.getDocumentProvider();
            IAnnotationModel ann_model = doc_provider.getAnnotationModel(editor_input);
            if (ann_model == null) continue;
            IRegion region = null;
            try {
                doc_provider.connect(editor_input);
            }
            catch (CoreException e) {
            }
            try {
                IDocument document = doc_provider.getDocument(editor_input);
                if (document != null) region = document.getLineInformation(a.area.start_line - 1);
            }
            catch (BadLocationException e) {
            }
            finally {
                doc_provider.disconnect(editor_input);
            }
            if (region == null) continue;
            ann_model.addAnnotation(a, new Position(region.getOffset(), region.getLength()));
            a.model = ann_model;
            win_info.annotations.add(a);
        }
    }

    private void updateAnnotations(final IWorkbenchWindow window, final TCFNode node) {
        if (disposed) return;
        assert Thread.currentThread() == display.getThread();
        final WorkbenchWindowInfo win_info = windows.get(window);
        if (win_info == null) return;
        if (win_info.update_task != null && win_info.update_node == node) return;
        win_info.update_node = node;
        win_info.update_task = new Runnable() {
            public void run() {
                if (win_info.update_task != this) {
                    /* Selection has changed and another update has started - abort this */
                    return;
                }
                if (node == null) {
                    /* No selection - no annotations */
                    done(null);
                    return;
                }
                if (node.isDisposed()) {
                    /* Selected node disposed - no annotations */
                    done(null);
                    return;
                }
                TCFNodeExecContext thread = null;
                TCFNodeExecContext memory = null;
                TCFNodeStackFrame frame = null;
                TCFNodeStackFrame last_top_frame = null;
                String bp_group = null;
                IBreakpoint bp = null;
                boolean suspended = false;
                if (node instanceof TCFNodeStackFrame) {
                    thread = (TCFNodeExecContext)node.parent;
                    frame = (TCFNodeStackFrame)node;
                }
                else if (node instanceof TCFNodeExecContext) {
                    thread = (TCFNodeExecContext)node;
                    TCFChildrenStackTrace trace = thread.getStackTrace();
                    if (!trace.validate(this)) return;
                    frame = trace.getTopFrame();
                }
                if (thread != null) {
                    TCFDataCache<IRunControl.RunControlContext> rc_ctx_cache = thread.getRunContext();
                    if (!rc_ctx_cache.validate(this)) return;
                    IRunControl.RunControlContext rc_ctx_data = rc_ctx_cache.getData();
                    if (rc_ctx_data != null) bp_group = rc_ctx_data.getBPGroup();
                    TCFDataCache<TCFNodeExecContext> mem_cache = thread.getMemoryNode();
                    if (!mem_cache.validate(this)) return;
                    memory = mem_cache.getData();
                    if (bp_group == null && memory != null && rc_ctx_data != null && rc_ctx_data.hasState()) bp_group = memory.id;
                    last_top_frame = thread.getLastTopFrame();
                    TCFDataCache<TCFContextState> state_cache = thread.getState();
                    if (!state_cache.validate(this)) return;
                    suspended = state_cache.getData() != null && state_cache.getData().is_suspended;
                }
                List<TCFAnnotation> set = new ArrayList<TCFAnnotation>();
                if (memory != null) {
                    TCFLaunch launch = node.launch;
                    TCFBreakpointsStatus bs = launch.getBreakpointsStatus();
                    if (bs != null) {
                        for (String id : bs.getStatusIDs()) {
                            Map<String,Object> map = bs.getStatus(id);
                            if (map == null) continue;
                            TCFBreakpointsModel bp_model = TCFBreakpointsModel.getBreakpointsModel();
                            if (bp_model != null) {
                                bp = bp_model.getBreakpoint(id);
                            }
                            String error = (String)map.get(IBreakpoints.STATUS_ERROR);
                            if (error != null) addBreakpointErrorAnnotation(set, launch, memory.id, id, error);
                            Object[] arr = toObjectArray(map.get(IBreakpoints.STATUS_INSTANCES));
                            if (arr == null) continue;
                            for (Object o : arr) {
                                Map<String,Object> m = toObjectMap(o);
                                String ctx_id = (String)m.get(IBreakpoints.INSTANCE_CONTEXT);
                                if (ctx_id == null) continue;
                                if (!ctx_id.equals(node.id) && !ctx_id.equals(bp_group)) continue;
                                error = (String)m.get(IBreakpoints.INSTANCE_ERROR);
                                BigInteger addr = JSON.toBigInteger((Number)m.get(IBreakpoints.INSTANCE_ADDRESS));
                                if (addr != null) {
                                    ILineNumbers.CodeArea area = null;
                                    TCFDataCache<TCFSourceRef> line_cache = memory.getLineInfo(addr);
                                    if (line_cache != null) {
                                        if (!line_cache.validate(this)) return;
                                        TCFSourceRef line_data = line_cache.getData();
                                        if (line_data != null && line_data.area != null) area = line_data.area;
                                    }
                                    Map<String,Object> props = bs.getProperties(id);
                                    String file = null;
                                    Number line = 0;
                                    if (props != null) {
                                        file = (String)props.get(IBreakpoints.PROP_FILE);
                                        line = (Number)props.get(IBreakpoints.PROP_LINE);
                                    }

                                    if (area == null && file != null && line != null) {
                                        area = new ILineNumbers.CodeArea(null, file,
                                                line.intValue(), 0, line.intValue() + 1, 0,
                                                null, null, 0, false, false, false, false);
                                    }
                                    if (area != null) {
                                        if ( bp != null && file == null ) {
                                            final Map<String, Object> markerAttrs = new HashMap<String, Object>();
                                            String File = new File(area.directory, area.file).toString();
                                            markerAttrs.put(IMarker.LINE_NUMBER, area.start_line);
                                            markerAttrs.put(ICBreakpoint.SOURCE_HANDLE, File);
                                            updateMarkerAttributes ( markerAttrs, bp );
                                        }                                        
                                        if (error != null) {
                                            Object[] obj = {String.format("0x%x", addr.toString(16)), error};
                                            TCFAnnotation a = new TCFAnnotation(memory.id, area,
                                                    ImageCache.getImage(ImageCache.IMG_BREAKPOINT_ERROR),
                                                    MessageFormat.format(Messages.TCFAnnotationManager_8, obj),
                                                    TYPE_BP_INSTANCE);
                                            set.add(a);
                                            error = null;
                                        }
                                        else {
                                            if (bp != null) {
                                                //Check if the marker needs to be updated.
                                                updateBPMarker(bp, area);
                                            }
                                            else {
                                                TCFAnnotation a = new TCFAnnotation(memory.id, area,
                                                        ImageCache.getImage(ImageCache.IMG_BREAKPOINT_INSTALLED),
                                                        MessageFormat.format(Messages.TCFAnnotationManager_0, String.format("0x%s",addr.toString(16))),
                                                        TYPE_BP_INSTANCE);
                                            set.add(a);
                                            }
                                        }
                                    }
                                }
                                if (error != null) addBreakpointErrorAnnotation(set, launch, memory.id, id, error);
                            }
                        }
                    }
                }
                if (suspended && frame != null && frame.getFrameNo() >= 0) {
                    TCFDataCache<TCFSourceRef> line_cache = frame.getLineInfo();
                    if (!line_cache.validate(this)) return;
                    TCFSourceRef line_data = line_cache.getData();
                    if (line_data != null && line_data.area != null) {
                        TCFAnnotation a = null;
                        if (frame.getFrameNo() == 0) {
                            a = new TCFAnnotation(line_data.context_id, line_data.area,
                                    DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_INSTRUCTION_POINTER_TOP),
                                    Messages.TCFAnnotationManager_1,
                                    TYPE_TOP_FRAME);
                        }
                        else {
                            a = new TCFAnnotation(line_data.context_id, line_data.area,
                                    DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_INSTRUCTION_POINTER),
                                    Messages.TCFAnnotationManager_2,
                                    TYPE_STACK_FRAME);
                        }
                        set.add(a);
                    }
                }
                if (!suspended && last_top_frame != null) {
                    TCFDataCache<TCFSourceRef> line_cache = last_top_frame.getLineInfo();
                    if (!line_cache.validate(this)) return;
                    TCFSourceRef line_data = line_cache.getData();
                    if (line_data != null && line_data.area != null) {
                        TCFAnnotation a = new TCFAnnotation(line_data.context_id, line_data.area,
                                DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_INSTRUCTION_POINTER),
                                Messages.TCFAnnotationManager_9,
                                TYPE_STACK_FRAME);
                        set.add(a);
                    }
                }
                done(set);
            }
            private void done(final List<TCFAnnotation> res) {
                final Runnable update_task = this;
                displayExec(new Runnable() {
                    public void run() {
                        if (update_task != win_info.update_task) return;
                        assert win_info.update_node == node;
                        win_info.update_task = null;
                        updateAnnotations(window, node, res);
                    }
                });
            }
        };
        Protocol.invokeLater(win_info.update_task);
    }

    private void updateAnnotations(final int cnt) {
        displayExec(new Runnable() {
            public void run() {
                synchronized (TCFAnnotationManager.this) {
                    if (cnt != update_unnotations_cnt) return;
                }
                for (IWorkbenchWindow window : windows.keySet()) {
                    if (dirty_windows.contains(null) || dirty_windows.contains(window)) {
                        TCFNode node = null;
                        try {
                            ISelection active_context = DebugUITools.getDebugContextManager()
                                    .getContextService(window).getActiveContext();
                            if (active_context instanceof IStructuredSelection) {
                                IStructuredSelection selection = (IStructuredSelection)active_context;
                                if (!selection.isEmpty()) {
                                    Object first_element = selection.getFirstElement();
                                    if (first_element instanceof IAdaptable) {
                                        node = (TCFNode)((IAdaptable)first_element).getAdapter(TCFNode.class);
                                    }
                                }
                            }
                            if (dirty_launches.contains(null) || node != null && dirty_launches.contains(node.launch)) {
                                updateAnnotations(window, node);
                            }
                        }
                        catch (Throwable x) {
                            if (node == null || !node.isDisposed()) {
                                Activator.log(Messages.TCFAnnotationManager_10, x);
                            }
                        }
                    }
                }
                for (TCFLaunch launch : dirty_launches) {
                    if (launch != null) launch.removePendingClient(TCFAnnotationManager.this);
                }
                changed_launch_cfgs.clear();
                dirty_windows.clear();
                dirty_launches.clear();
            }
        });
    }

    synchronized void updateAnnotations(final IWorkbenchWindow window, final TCFLaunch launch) {
        final int cnt = ++update_unnotations_cnt;
        displayExec(new Runnable() {
            public void run() {
                dirty_windows.add(window);
                dirty_launches.add(launch);
                updateAnnotations(cnt);
            }
        });
    }
}
