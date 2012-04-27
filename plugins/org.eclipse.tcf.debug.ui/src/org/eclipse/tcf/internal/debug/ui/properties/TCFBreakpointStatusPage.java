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
package org.eclipse.tcf.internal.debug.ui.properties;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.tcf.internal.debug.launch.TCFSourceLookupParticipant;
import org.eclipse.tcf.internal.debug.model.TCFBreakpointsModel;
import org.eclipse.tcf.internal.debug.model.TCFBreakpointsStatus;
import org.eclipse.tcf.internal.debug.model.TCFLaunch;
import org.eclipse.tcf.internal.debug.model.TCFSourceRef;
import org.eclipse.tcf.internal.debug.ui.ImageCache;
import org.eclipse.tcf.internal.debug.ui.model.TCFModel;
import org.eclipse.tcf.internal.debug.ui.model.TCFModelManager;
import org.eclipse.tcf.internal.debug.ui.model.TCFNode;
import org.eclipse.tcf.internal.debug.ui.model.TCFNodeExecContext;
import org.eclipse.tcf.internal.debug.ui.model.TCFNodeLaunch;
import org.eclipse.tcf.protocol.JSON;
import org.eclipse.tcf.services.IBreakpoints;
import org.eclipse.tcf.services.IRunControl;
import org.eclipse.tcf.util.TCFDataCache;
import org.eclipse.tcf.util.TCFTask;
import org.eclipse.ui.dialogs.PropertyPage;

public class TCFBreakpointStatusPage extends PropertyPage {

    private TreeViewer viewer;
    private List<StatusItem> status;

    private static class StatusItem implements Comparable<StatusItem> {
        Object object;
        IMarker marker;
        String text;
        boolean has_state;
        boolean planted_ok;
        List<StatusItem> children;
        StatusItem parent;

        void add(StatusItem i) {
            i.parent = this;
            if (children == null) children = new ArrayList<StatusItem>();
            children.add(i);
        }

        void add(String text) {
            StatusItem i = new StatusItem();
            i.text = text;
            add(i);
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        public int compareTo(StatusItem n) {
            if (object instanceof TCFNode) {
                if (n.object instanceof TCFNode) {
                    return ((Comparable)object).compareTo(n.object);
                }
                return 1;
            }
            if (n.object instanceof TCFNode) return -1;
            return 0;
        }
    }

    private class StatusCache extends TCFDataCache<StatusItem> {

        final TCFLaunch launch;

        TCFDataCache<?> pending;

        public StatusCache(TCFLaunch launch) {
            super(launch.getChannel());
            this.launch = launch;
        }

        @Override
        protected boolean startDataRetrieval() {
            pending = null;
            TCFBreakpointsStatus status = launch.getBreakpointsStatus();
            if (status == null) {
                set(null, null, null);
                return true;
            }
            Map<String,Object> map = status.getStatus(getBreakpoint());
            if (map == null || map.size() == 0) {
                set(null, null, null);
                return true;
            }
            StatusItem x = new StatusItem();
            x.object = launch;
            Object planted = map.get(IBreakpoints.STATUS_INSTANCES);
            if (planted != null) {
                TCFModel model = TCFModelManager.getModelManager().getModel(launch);
                for (Object o : toObjectArray(planted)) {
                    Map<String,Object> m = toObjectMap(o);
                    String ctx_id = (String)m.get(IBreakpoints.INSTANCE_CONTEXT);
                    if (!model.createNode(ctx_id, this)) return false;
                    if (isValid()) {
                        /* Invalid context ID, ignore */
                        reset();
                        continue;
                    }
                    StatusItem y = getNodeItem(x, model.getNode(ctx_id));
                    if (y == null) continue;
                    StatusItem z = new StatusItem();
                    z.marker = getBreakpoint().getMarker();
                    z.text = z.marker.getAttribute(TCFBreakpointsModel.ATTR_MESSAGE, "");
                    String error = (String)m.get(IBreakpoints.INSTANCE_ERROR);
                    if (error != null) z.add("Error: " + error);
                    Number addr = (Number)m.get(IBreakpoints.INSTANCE_ADDRESS);
                    z.planted_ok = error == null;
                    if (addr != null) {
                        BigInteger i = JSON.toBigInteger(addr);
                        z.add("Address: 0x" +  i.toString(16));
                    }
                    Number size = (Number)m.get(IBreakpoints.INSTANCE_SIZE);
                    if (size != null) z.add("Size: " + size);
                    String type = (String)m.get(IBreakpoints.INSTANCE_TYPE);
                    if (type != null) z.add("Type: " + type);
                    if (addr != null && y.object instanceof TCFNode) {
                        TCFDataCache<TCFNodeExecContext> mem = model.searchMemoryContext((TCFNode)y.object);
                        if (mem != null) {
                            if (!mem.validate()) {
                                pending = mem;
                            }
                            else {
                                TCFNodeExecContext ctx = mem.getData();
                                if (ctx != null) {
                                    BigInteger i = JSON.toBigInteger(addr);
                                    TCFDataCache<TCFSourceRef> ln_cache = ctx.getLineInfo(i);
                                    if (ln_cache != null) {
                                        if (!ln_cache.validate()) {
                                            pending = ln_cache;
                                        }
                                        else {
                                            addLocationInfo(z, ln_cache.getData());
                                        }
                                    }
                                }
                            }
                        }
                    }
                    String mem_id = (String)m.get(IBreakpoints.INSTANCE_MEMORY_CONTEXT);
                    if (mem_id != null) {
                        if (!model.createNode(mem_id, this)) return false;
                        if (isValid()) reset();
                        else addMemoryContext(z, model.getNode(mem_id));
                    }
                    Number hit_count = (Number)m.get(IBreakpoints.INSTANCE_HIT_COUNT);
                    if (hit_count != null) z.add("Hit count: " + hit_count);
                    y.add(z);
                }
            }
            if (pending != null) {
                pending.wait(this);
                return false;
            }
            String error = (String)map.get(IBreakpoints.STATUS_ERROR);
            if (error != null) {
                StatusItem y = new StatusItem();
                y.text = error;
                x.add(y);
            }
            set(null, null, x);
            return true;
        }

        private void addLocationInfo(StatusItem z, TCFSourceRef ref) {
            if (ref == null) return;
            if (ref.area == null) return;
            if (ref.area.file == null) return;

            String req_file = z.marker.getAttribute(TCFBreakpointsModel.ATTR_REQESTED_FILE, null);
            if (req_file == null) req_file = z.marker.getAttribute(TCFBreakpointsModel.ATTR_FILE, null);

            int req_line = z.marker.getAttribute(TCFBreakpointsModel.ATTR_REQESTED_LINE, -1);
            if (req_line < 0) req_line = z.marker.getAttribute(TCFBreakpointsModel.ATTR_LINE, -1);

            int req_char = z.marker.getAttribute(TCFBreakpointsModel.ATTR_REQESTED_CHAR, -1);
            if (req_char < 0) req_char = z.marker.getAttribute(TCFBreakpointsModel.ATTR_CHAR, -1);

            String area_file = TCFSourceLookupParticipant.toFileName(ref.area);
            if (req_file != null && req_line >= 0) {
                String req_file_name = new File(req_file).getName();
                String file_name = new File(ref.area.file).getName();
                if (!req_file_name.equals(file_name) || req_line != ref.area.start_line) {
                    addLocationInfo(z, "Requested location", req_file, req_line, req_char);
                    addLocationInfo(z, "Adjusted location", area_file, ref.area.start_line, ref.area.start_column);
                    return;
                }
            }
            addLocationInfo(z, "Location", area_file, ref.area.start_line, ref.area.start_column);
        }

        private void addLocationInfo(StatusItem z, String name, String file, int line, int column) {
            String text = name + ": " + file;
            text += "; line: " + line;
            if (column > 0) text += "; column: " + column;
            z.add(text);
        }

        private void addMemoryContext(StatusItem z, TCFNode node) {
            if (node instanceof TCFNodeExecContext) {
                TCFNodeExecContext exe_node = (TCFNodeExecContext)node;
                TCFDataCache<String> cache = exe_node.getFullName();
                if (!cache.validate()) {
                    pending = cache;
                    return;
                }
                z.add("Memory context: " + cache.getData());
            }
        }

        private StatusItem getNodeItem(StatusItem root, TCFNode node) {
            TCFNode parent = node.getParent();
            if (parent == null) return root;
            StatusItem x = null; // parent status item
            Set<String> filter = launch.getContextFilter();
            if (filter != null) {
                if (filter.contains(node.getID())) x = root;
                else if (parent instanceof TCFNodeLaunch) return null;
            }
            if (x == null) x = getNodeItem(root, parent);
            if (x == null) return null;
            if (x.children != null) {
                for (StatusItem y : x.children) {
                    if (y.object == node) return y;
                }
            }
            StatusItem y = new StatusItem();
            y.object = node;
            TCFDataCache<IRunControl.RunControlContext> cache = ((TCFNodeExecContext)node).getRunContext();
            if (!cache.validate()) {
                pending = cache;
            }
            else {
                IRunControl.RunControlContext ctx = cache.getData();
                if (ctx != null) {
                    y.text = ctx.getName();
                    y.has_state = ctx.hasState();
                }
                if (y.text == null) y.text = node.getID();
            }
            x.add(y);
            return y;
        }
    }

    private final ITreeContentProvider content_provider = new ITreeContentProvider() {

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

        public Object[] getElements(Object input) {
            return status.toArray(new StatusItem[status.size()]);
        }

        public Object[] getChildren(Object parent) {
            StatusItem x = (StatusItem)parent;
            if (x.children == null) return null;
            Object[] arr = x.children.toArray(new StatusItem[x.children.size()]);
            Arrays.sort(arr);
            return arr;
        }

        public Object getParent(Object element) {
            StatusItem x = (StatusItem)element;
            return x.parent;
        }

        public boolean hasChildren(Object element) {
            StatusItem x = (StatusItem)element;
            return x.children != null && x.children.size() > 0;
        }
    };

    private final LabelProvider label_provider = new LabelProvider() {

        @Override
        public Image getImage(Object element) {
            StatusItem x = (StatusItem)element;
            if (x.object instanceof ILaunch) {
                ImageDescriptor desc = DebugUITools.getDefaultImageDescriptor(x.object);
                if (desc != null) return ImageCache.getImage(desc);
            }
            if (x.has_state) return DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_THREAD_RUNNING);
            if (x.object != null) return DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_DEBUG_TARGET);
            if (x.marker != null) {
                if (x.planted_ok) return DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_BREAKPOINT);
                return DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_BREAKPOINT_DISABLED);
            }
            return null;
        }

        @Override
        public String getText(Object element) {
            StatusItem x = (StatusItem)element;
            if (x.object instanceof ILaunch) {
                ILaunchConfiguration cfg = ((ILaunch)x.object).getLaunchConfiguration();
                if (cfg != null) return cfg.getName();
            }
            return x.text;
        }
    };

    @Override
    protected Control createContents(Composite parent) {
        noDefaultAndApplyButton();
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setFont(parent.getFont());
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        status = getCurrentStatus();
        createStatusViewer(composite);
        setValid(true);
        return composite;
    }

    private void createStatusViewer(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText("Breakpoint planting status:");
        label.setFont(parent.getFont());
        label.setLayoutData(new GridData());
        GridData data = new GridData(GridData.FILL_BOTH);
        data.heightHint = 100;
        viewer = new TreeViewer(parent, SWT.BORDER);
        viewer.getTree().setLayoutData(data);
        viewer.getTree().setFont(parent.getFont());
        viewer.setContentProvider(content_provider);
        viewer.setLabelProvider(label_provider);
        viewer.setInput(this);
        viewer.expandAll();
    }

    private IBreakpoint getBreakpoint() {
        return (IBreakpoint)getElement().getAdapter(IBreakpoint.class);
    }

    private List<StatusItem> getCurrentStatus() {
        final List<StatusCache> caches = new ArrayList<StatusCache>();
        final ILaunch[] launches = DebugPlugin.getDefault().getLaunchManager().getLaunches();
        for (ILaunch launch : launches) {
            if (!(launch instanceof TCFLaunch)) continue;
            TCFLaunch tcf_launch = (TCFLaunch)launch;
            if (tcf_launch.isConnecting()) continue;
            if (tcf_launch.isDisconnected()) continue;
            caches.add(new StatusCache(tcf_launch));
        }
        List<StatusItem> status = new TCFTask<List<StatusItem>>(10000) {
            public void run() {
                StatusCache pending = null;
                for (StatusCache cache : caches) {
                    if (!cache.validate()) pending = cache;
                }
                if (pending != null) {
                    pending.wait(this);
                    return;
                }
                List<StatusItem> roots = new ArrayList<StatusItem>();
                for (StatusCache cache : caches) {
                    StatusItem x = cache.getData();
                    if (x != null) roots.add(x);
                }
                for (StatusCache cache : caches) cache.dispose();
                if (roots.size() == 0) {
                    StatusItem x = new StatusItem();
                    x.text = "Not planted";
                    roots.add(x);
                }
                done(roots);
            }
        }.getE();
        return status;
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
}
