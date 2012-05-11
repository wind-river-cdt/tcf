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
package org.eclipse.tcf.internal.debug.ui.model;

import java.util.LinkedList;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementCompareRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerInputUpdate;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.tcf.debug.ui.ITCFObject;
import org.eclipse.tcf.internal.debug.model.TCFLaunch;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.util.TCFDataCache;
import org.eclipse.ui.IWorkbenchPart;

/**
 * TCFNode is base class for all TCF debug model elements.
 */
public abstract class TCFNode extends PlatformObject implements ITCFObject, Comparable<TCFNode> {

    protected final String id;
    protected final TCFNode parent;
    protected final TCFModel model;
    protected final TCFLaunch launch;
    protected final IChannel channel;

    private boolean disposed;

    /**
     * An extension of TCFDataCache class that is automatically disposed when the node is disposed.
     */
    protected abstract class TCFData<V> extends TCFDataCache<V> {

        TCFData(IChannel channel) {
            super(channel);
            addDataCache(this);
        }

        @Override
        public void dispose() {
            removeDataCache(this);
            super.dispose();
        }
    }

    private LinkedList<TCFDataCache<?>> caches;

    /**
     * Constructor for a root node. There should be exactly one root in the model.
     * @param model
     */
    protected TCFNode(TCFModel model) {
        id = null;
        parent = null;
        launch = model.getLaunch();
        channel = model.getChannel();
        this.model = model;
    }

    /**
     * Constructor for a node other then root. Node ID must be unique.
     * @param parent - parent node.
     * @param id - node ID.
     */
    protected TCFNode(TCFNode parent, String id) {
        assert Protocol.isDispatchThread();
        assert parent != null;
        assert id != null;
        assert !parent.disposed;
        this.parent = parent;
        this.id = id;
        model = parent.model;
        model.addNode(id, this);
        launch = model.getLaunch();
        channel = model.getChannel();
    }

    /**
     * Register a data cache object that caches data for this node.
     * @param c - a TCFData object.
     */
    final void addDataCache(TCFDataCache<?> c) {
        if (caches == null) caches = new LinkedList<TCFDataCache<?>>();
        caches.add(c);
    }

    /**
     * Unregister a data cache object that caches children for this node.
     * @param c - a TCFData object.
     */
    final void removeDataCache(TCFDataCache<?> c) {
        if (caches != null) caches.remove(c);
    }

    /**
     * Flush (reset) all node data caches.
     */
    void flushAllCaches() {
        if (caches == null) return;
        for (TCFDataCache<?> c : caches) c.reset();
    }

    /**
     * Dispose this node and its children. The node is removed from the model.
     */
    void dispose() {
        assert !disposed;
        while (caches != null && caches.size() > 0) {
            caches.getLast().dispose();
        }
        if (parent != null && parent.caches != null) {
            for (TCFDataCache<?> c : parent.caches) {
                if (c instanceof TCFChildren) ((TCFChildren)c).onNodeDisposed(id);
            }
        }
        if (id != null) {
            assert model.getNode(id) == this;
            model.removeNode(id);
        }
        disposed = true;
    }

    /**
     * Check if node is disposed.
     * @return true if disposed.
     */
    public final boolean isDisposed() {
        return disposed;
    }

    /**
     * Get TCFModel that owns this node.
     * @return TCFModel object
     */
    public TCFModel getModel() {
        return model;
    }

    /**
     * Get IChannel of TCFModel that owns this node.
     * @return IChannel object
     */
    public IChannel getChannel() {
        return channel;
    }

    /**
     * Get TCF ID of the node.
     * @return TCF ID
     */
    public String getID() {
        return id;
    }

    /**
     * Returns an object which is an instance of the given class
     * associated with this object. Returns <code>null</code> if
     * no such object can be found.
     *
     * @param adapter the class to adapt to
     * @return the adapted object or <code>null</code>
     * @see IAdaptable#getAdapter(Class)
     */
    @Override
    @SuppressWarnings("rawtypes")
    public Object getAdapter(final Class adapter) {
        if (adapter.isInstance(this)) return this;
        if (adapter.isInstance(model)) return model;
        Object o = model.getAdapter(adapter, TCFNode.this);
        if (o != null) return o;
        return Platform.getAdapterManager().loadAdapter(this, adapter.getName());
    }

    /**
     * Get parent node.
     * @return parent node or null if the node is a root
     */
    public final TCFNode getParent() {
        return parent;
    }

    /**
     * Get parent node in given presentation context.
     * @param ctx - presentation context.
     * @return parent node or null if the node is a root
     */
    public TCFNode getParent(IPresentationContext ctx) {
        assert Protocol.isDispatchThread();
        return parent;
    }

    /**
     * Retrieve children count for a presentation context.
     * @param update - children count update request.
     */
    final void update(final IChildrenCountUpdate update) {
        new TCFRunnable(update) {
            public void run() {
                if (!done) {
                    if (!update.isCanceled()) {
                        if (!disposed && channel.getState() == IChannel.STATE_OPEN) {
                            if (!getData(update, this)) return;
                        }
                        else {
                            update.setChildCount(0);
                        }
                        update.setStatus(Status.OK_STATUS);
                    }
                    done();
                }
            }
        };
    }

    /**
     * Retrieve children for a presentation context.
     * @param update - children update request.
     */
    final void update(final IChildrenUpdate update) {
        new TCFRunnable(update) {
            public void run() {
                if (!done) {
                    if (!update.isCanceled()) {
                        if (!disposed && channel.getState() == IChannel.STATE_OPEN) {
                            if (!getLockedData(update, this)) return;
                        }
                        update.setStatus(Status.OK_STATUS);
                    }
                    done();
                }
            }
        };
    }

    /**
     * Check if the node has children in a presentation context.
     * @param update - "has children" update request.
     */
    final void update(final IHasChildrenUpdate update) {
        new TCFRunnable(update) {
            public void run() {
                if (!done) {
                    if (!update.isCanceled()) {
                        if (!disposed && channel.getState() == IChannel.STATE_OPEN) {
                            if (!getLockedData(update, this)) return;
                        }
                        else {
                            update.setHasChilren(false);
                        }
                        update.setStatus(Status.OK_STATUS);
                    }
                    done();
                }
            }
        };
    }

    /**
     * Retrieve node label for a presentation context.
     * @param update - label update request.
     */
    final void update(final ILabelUpdate update) {
        new TCFRunnable(update) {
            public void run() {
                if (!done) {
                    if (!update.isCanceled()) {
                        if (!disposed && channel.getState() == IChannel.STATE_OPEN) {
                            if (!getLockedData(update, this)) return;
                            getFontData(update, update.getPresentationContext().getId());
                        }
                        else {
                            update.setLabel("...", 0);
                        }
                        update.setStatus(Status.OK_STATUS);
                    }
                    done();
                }
            }
        };
    }

    /**
     * Retrieve viewer input object for a presentation context.
     * Allows a view to translate the active debug context into an appropriate viewer input element.
     * @param update - input update request.
     */
    final void update(final IViewerInputUpdate update) {
        new TCFRunnable(update) {
            public void run() {
                if (!done) {
                    if (!update.isCanceled()) {
                        if (!disposed && channel.getState() == IChannel.STATE_OPEN) {
                            if (!getData(update, this)) return;
                        }
                        else {
                            update.setInputElement(TCFNode.this);
                        }
                        update.setStatus(Status.OK_STATUS);
                    }
                    done();
                }
            }
        };
    }

    /**
     * Retrieve children count for a presentation context.
     * If the context is locked, return snapshot data.
     * Otherwise return live data from the target.
     * The method is always called on TCF dispatch thread.
     * @param update - children count update request.
     * @param done - client call back interface, during data waiting it is
     * called every time new portion of data becomes available.
     * @return false if waiting data retrieval, true if all done.
     */
    final boolean getLockedData(IChildrenCountUpdate update, Runnable done) {
        TCFSnapshot snapshot = model.getSnapshot(update.getPresentationContext());
        if (snapshot != null) return snapshot.getData(update, this, done);
        return getData(update, done);
    }

    /**
     * Retrieve children for a presentation context.
     * If the context is locked, return snapshot data.
     * Otherwise return live data from the target.
     * The method is always called on TCF dispatch thread.
     * @param update - children update request.
     * @param done - client call back interface, during data waiting it is
     * called every time new portion of data becomes available.
     * @return false if waiting data retrieval, true if all done.
     */
    final boolean getLockedData(IChildrenUpdate update, Runnable done) {
        TCFSnapshot snapshot = model.getSnapshot(update.getPresentationContext());
        if (snapshot != null) return snapshot.getData(update, this, done);
        return getData(update, done);
    }

    /**
     * Check if the node has children in a presentation context.
     * If the context is locked, return snapshot data.
     * Otherwise return live data from the target.
     * The method is always called on TCF dispatch thread.
     * @param update - "has children" update request.
     * @param done - client call back interface, during data waiting it is
     * called every time new portion of data becomes available.
     * @return false if waiting data retrieval, true if all done.
     */
    final boolean getLockedData(IHasChildrenUpdate update, Runnable done) {
        TCFSnapshot snapshot = model.getSnapshot(update.getPresentationContext());
        if (snapshot != null) return snapshot.getData(update, this, done);
        return getData(update, done);
    }

    /**
     * Retrieve node label for a presentation context.
     * If the context is locked, return snapshot data.
     * Otherwise return live data from the target.
     * The method is always called on TCF dispatch thread.
     * @param update - label update request.
     * @param done - client call back interface, during data waiting it is
     * called every time new portion of data becomes available.
     * @return false if waiting data retrieval, true if all done.
     */
    final boolean getLockedData(ILabelUpdate update, Runnable done) {
        TCFSnapshot snapshot = model.getSnapshot(update.getPresentationContext());
        if (snapshot != null) return snapshot.getData(update, this, done);
        return getData(update, done);
    }

    /**
     * Retrieve children count for a presentation context.
     * The method is always called on TCF dispatch thread.
     * @param update - children count update request.
     * @param done - client call back interface, during data waiting it is
     * called every time new portion of data becomes available.
     * @return false if waiting data retrieval, true if all done.
     */
    protected boolean getData(IChildrenCountUpdate update, Runnable done) {
        update.setChildCount(0);
        return true;
    }

    /**
     * Retrieve children for a presentation context.
     * The method is always called on TCF dispatch thread.
     * @param update - children update request.
     * @param done - client call back interface, during data waiting it is
     * called every time new portion of data becomes available.
     * @return false if waiting data retrieval, true if all done.
     */
    protected boolean getData(IChildrenUpdate update, Runnable done) {
        return true;
    }

    /**
     * Check if the node has children in a presentation context.
     * The method is always called on TCF dispatch thread.
     * @param update - "has children" update request.
     * @param done - client call back interface, during data waiting it is
     * called every time new portion of data becomes available.
     * @return false if waiting data retrieval, true if all done.
     */
    protected boolean getData(IHasChildrenUpdate update, Runnable done) {
        update.setHasChilren(false);
        return true;
    }

    /**
     * Retrieve node label for a presentation context.
     * The method is always called on TCF dispatch thread.
     * @param update - label update request.
     * @param done - client call back interface, during data waiting it is
     * called every time new portion of data becomes available.
     * @return false if waiting data retrieval, true if all done.
     */
    protected boolean getData(ILabelUpdate update, Runnable done) {
        update.setLabel(id, 0);
        return true;
    }

    /**
     * Retrieve viewer input object for a presentation context.
     * Allows a view to translate the active debug context into an appropriate viewer input element.
     * The method is always called on TCF dispatch thread.
     * @param update - view input update request.
     * @param done - client call back interface, during data waiting it is
     * called every time new portion of data becomes available.
     * @return false if waiting data retrieval, true if all done.
     */
    protected boolean getData(IViewerInputUpdate update, Runnable done) {
        update.setInputElement(this);
        return true;
    }

    /**
     * Get FontData info for node label update.
     * @param update - label update request.
     */
    protected void getFontData(ILabelUpdate update, String view_id) {
        FontData fd = TCFModelFonts.getNormalFontData(view_id);
        String[] cols = update.getColumnIds();
        if (cols == null || cols.length == 0) {
            update.setFontData(fd, 0);
        }
        else {
            for (int i = 0; i < cols.length; i++) update.setFontData(fd, i);
        }
    }

    /**
     * Creates and stores a IMemento for the node.
     * A request should be cancelled if a IMemento is not supported.
     * @param request Specifies IMemento.
     */
    public void encodeElement(IElementMementoRequest request) {
        request.getMemento().putString("TCF.ID", id);
    }

    /**
     * Determines whether a IMemento represents this node.
     * @param request Specifies previously created IMemento.
     */
    public void compareElements(IElementCompareRequest request) {
        request.setEqual(id.equals(request.getMemento().getString("TCF.ID")));
    }

    /*--------------------------------------------------------------------------------------*/
    /* Misc                                                                                 */

    /**
     * Flush all caches and repaint the node and its children in presentation context of given part.
     * @param part - workbench part that needs to be refreshed.
     */
    public void refresh(IWorkbenchPart part) {
        model.flushAllCaches();
        for (TCFModelProxy p : model.getModelProxies()) {
            if (p.getPresentationContext().getPart() != part) continue;
            p.addDelta(this, IModelDelta.STATE | IModelDelta.CONTENT);
        }
    }

    /**
     * Compare two nodes.
     * Extensions of TCFNode are expected to override this method.
     */
    public int compareTo(TCFNode n) {
        return id.compareTo(n.id);
    }

    /**
     * Returns a simple human readable string representation of the node.
     */
    public String toString() {
        String s = "[" + Integer.toHexString(hashCode()) + "] " + id;
        if (disposed) s += ", disposed";
        return s;
    }
}
