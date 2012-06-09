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
package org.eclipse.tcf.internal.cdt.ui.breakpoints;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxyFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ITreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.PresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.provisional.AbstractModelProxy;
import org.eclipse.debug.ui.IDetailPane;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.tcf.debug.ui.ITCFDebugUIConstants;
import org.eclipse.tcf.internal.cdt.ui.ImageCache;
import org.eclipse.tcf.internal.debug.model.ITCFConstants;
import org.eclipse.tcf.internal.debug.model.TCFLaunch;
import org.eclipse.tcf.internal.debug.ui.model.TCFContextQueryDescendants;
import org.eclipse.tcf.internal.debug.ui.model.TCFModel;
import org.eclipse.tcf.internal.debug.ui.model.TCFModelManager;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.ui.IWorkbenchPartSite;

/**
 * This detail pane uses a tree viewer to show which contexts a given breakpoint 
 * can potentially trigger.
 */
@SuppressWarnings("restriction") 
public class TCFBreakpointScopeDetailPane implements IDetailPane {

    public static final String ID = "org.eclipse.tcf.debug.DetailPaneFactory";
    public static final String NAME = "TCF Detail Pane";
    public static final String DESC = "TCF Detail Pane";

    private Composite fComposite;
    private Label fFilterName; 
    private TreeModelViewer fTreeViewer;

    public static class ScopeDetailInputObject extends PlatformObject implements IElementContentProvider, IModelProxyFactory {
        
        private final ContextQueryElement fContextQueryElement;
        
        public ScopeDetailInputObject(ContextQueryElement query) {
            fContextQueryElement = query;
        }
        
        @Override
        public boolean equals(Object other) {
            if (other instanceof ScopeDetailInputObject) {
                return fContextQueryElement.equals( ((ScopeDetailInputObject)other).fContextQueryElement );
            }
            return false;
        }
        
        @Override
        public int hashCode() {
            return fContextQueryElement.hashCode();
        }
        
        public void update(IChildrenCountUpdate[] updates) {
            for (IChildrenCountUpdate update : updates) {
                update.setChildCount(1);
                update.done();
            }
        }
        
        public void update(IChildrenUpdate[] updates) {
            for (IChildrenUpdate update : updates) {
                if (update.getOffset() == 0) {
                    update.setChild(fContextQueryElement, 0);
                    update.done();
                }
            }
        }
        
        public void update(IHasChildrenUpdate[] updates) {
            for (IHasChildrenUpdate update : updates) {
                update.setHasChilren(true);
                update.done();
            }
        }
        
        public IModelProxy createModelProxy(Object element, IPresentationContext context) {
            return new AbstractModelProxy() {
                @Override
                public void initialize(ITreeModelViewer viewer) {
                    super.initialize(viewer);
                    ModelDelta delta = new ModelDelta(this, IModelDelta.NO_CHANGE);
                    delta.addNode(fContextQueryElement, 0, IModelDelta.INSTALL);
                    fireModelChanged(delta);
                }
            };
        }
    }
    
    public static class ContextQueryElement extends PlatformObject 
        implements IElementContentProvider, IElementLabelProvider, IModelProxyFactory 
    {
        private final String fQuery;
        private Set<String> fContexts;
        
        public ContextQueryElement(String query, Set<String> contexts) {
            fQuery = query;
            fContexts = contexts;
        }
        
        @Override
        public boolean equals(Object other) {
            if (other instanceof ContextQueryElement) {
                ContextQueryElement element = (ContextQueryElement)other;
                return ((fQuery == null && element.fQuery == null) || 
                        (fQuery != null && fQuery.equals(element.fQuery))) &&
                       ((fContexts == null && element.fContexts == null) || 
                        (fContexts != null && fContexts.equals(element.fContexts)));
            }
            return false;
        }
        
        @Override
        public int hashCode() {
            return (fQuery != null ? fQuery.hashCode() : 0) + (fContexts != null ? fContexts.hashCode() : 0);
        }
        
        public void update(IChildrenCountUpdate[] updates) {
            for (IViewerUpdate update : updates) {
                getFilteredLaunches(update);
            }
        }
        
        public void update(IChildrenUpdate[] updates) {
            for (IViewerUpdate update : updates) {
                getFilteredLaunches(update);
            }
        }
        
        public void update(IHasChildrenUpdate[] updates) {
            for (IViewerUpdate update : updates) {
                getFilteredLaunches(update);
            }
        }
        
        public void update(ILabelUpdate[] updates) {
            for (ILabelUpdate update : updates) {
                getQueryFilteredContexts(update);
            }
        }
        
        private List<TCFLaunch> getTCFLaunches() {
            List<TCFLaunch> tcfLaunches = new ArrayList<TCFLaunch>();
            for (ILaunch launch : DebugPlugin.getDefault().getLaunchManager().getLaunches()) {
                if (launch instanceof TCFLaunch) {
                    tcfLaunches.add((TCFLaunch)launch);
                }
            }
            return tcfLaunches;
        }

        
        private void getFilteredLaunches (final IViewerUpdate update) {
            Protocol.invokeLater( new Runnable() {
                public void run() {
                    final List<TCFLaunch> filteredLaunches = new ArrayList<TCFLaunch>();
                    TCFModelManager modelManager = TCFModelManager.getModelManager();
                    for (TCFLaunch launch : getTCFLaunches()) {
                        TCFModel model = modelManager.getModel(launch);
                        if (model != null && model.getRootNode() != null) {
                            TCFContextQueryDescendants query_descendants = model.getRootNode().getContextQueryDescendants();
                            if (!query_descendants.setQuery(fQuery, launch.getModelContexts(fContexts), this)) return;
                            if (!query_descendants.validate(this)) return;
                            if (query_descendants.getData() != null && !query_descendants.getData().isEmpty()) {
                                filteredLaunches.add(launch);
                            }
                        }
                    }
                    
                    done(filteredLaunches, update);
                }
            });
        }

        private void getQueryFilteredContexts (final ILabelUpdate update) {
            Protocol.invokeLater( new Runnable() {
                public void run() {
                    TCFModelManager modelManager = TCFModelManager.getModelManager();
                    Set<String> set = new TreeSet<String>();
                    for (TCFLaunch launch : getTCFLaunches()) {
                        TCFModel model = modelManager.getModel(launch);
                        if (model != null && model.getRootNode() != null) {
                            TCFContextQueryDescendants query_descendants = model.getRootNode().getContextQueryDescendants();
                            if (!query_descendants.setQuery(fQuery, launch.getModelContexts(fContexts), this)) return;
                            if (!query_descendants.validate(this)) return;
                            if (query_descendants.getData() != null) {
                                set.addAll(query_descendants.getData());
                            }
                        }
                    }
                    
                    StringBuffer label = new StringBuffer();
                    label.append("(");
                    label.append(set.size());
                    label.append(") ");
                    
                    if (fQuery != null) {
                        label.append("Filter: ");
                        label.append(fQuery);
                        if (fContexts != null) {
                            label.append(", ");
                        }
                    } 
                    if (fContexts != null) {
                        label.append("Contexts: ");
                        label.append(fContexts);
                    }
                    update.setLabel(label.toString(), 0);
                    update.setImageDescriptor(ImageCache.getImageDescriptor(ImageCache.IMG_BREAKPOINT_SCOPE), 0);
                    update.done();
                }
            });
        }
        
        private void done(List<TCFLaunch> launches, IViewerUpdate update) {
            if (update instanceof IHasChildrenUpdate) {
                ((IHasChildrenUpdate)update).setHasChilren(!launches.isEmpty());
            } else if (update instanceof IChildrenCountUpdate) {
                ((IChildrenCountUpdate)update).setChildCount(launches.size());
            } else if (update instanceof IChildrenUpdate) {
                IChildrenUpdate childrenUpdate = (IChildrenUpdate)update;
                int updateStart = childrenUpdate.getOffset();
                int updateEnd = childrenUpdate.getOffset() + childrenUpdate.getLength();
                for (int i = updateStart; i < updateEnd && i < launches.size(); i++) {
                    childrenUpdate.setChild(launches.get(i), i);
                }
            }
            update.done();
        }
        
        public IModelProxy createModelProxy(Object element, IPresentationContext context) {
            return new QueryInputObjectProxy();
        }
    }

    public static class QueryInputObjectProxy extends AbstractModelProxy implements ILaunchesListener2 {

        private ILaunchManager fLaunchManager;

        /* (non-Javadoc)
         * @see org.eclipse.debug.internal.ui.viewers.AbstractModelProxy#init(org.eclipse.debug.internal.ui.viewers.IPresentationContext)
         */
        public synchronized void init(IPresentationContext context) {
                super.init(context);
                fLaunchManager = DebugPlugin.getDefault().getLaunchManager();
                fLaunchManager.addLaunchListener(this);
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.debug.internal.ui.viewers.provisional.AbstractModelProxy#installed(org.eclipse.jface.viewers.Viewer)
         */
        public void installed(Viewer viewer) {
                // expand existing launches
                ILaunch[] launches = fLaunchManager.getLaunches();
                if (launches.length > 0) {
                        launchesAdded(launches);
                }
        }

        /* (non-Javadoc)
         * @see org.eclipse.debug.internal.ui.viewers.AbstractModelProxy#dispose()
         */
        public synchronized void dispose() {
                super.dispose();
                if (fLaunchManager != null) {
                        fLaunchManager.removeLaunchListener(this);
                        fLaunchManager = null;
                }
        }

        /* (non-Javadoc)
         * @see org.eclipse.debug.core.ILaunchesListener2#launchesTerminated(org.eclipse.debug.core.ILaunch[])
         */
        public void launchesTerminated(ILaunch[] launches) {
        }

        /* (non-Javadoc)
         * @see org.eclipse.debug.core.ILaunchesListener#launchesRemoved(org.eclipse.debug.core.ILaunch[])
         */
        public void launchesRemoved(ILaunch[] launches) {
            fireDelta(launches, IModelDelta.REMOVED);
        }

        /* (non-Javadoc)
         * @see org.eclipse.debug.core.ILaunchesListener#launchesAdded(org.eclipse.debug.core.ILaunch[])
         */
        public void launchesAdded(ILaunch[] launches) {
            fireDelta(launches, IModelDelta.ADDED | IModelDelta.INSTALL);
        }

        /* (non-Javadoc)
         * @see org.eclipse.debug.core.ILaunchesListener#launchesChanged(org.eclipse.debug.core.ILaunch[])
         */
        public void launchesChanged(ILaunch[] launches) {       
        }
        
        /**
         * Convenience method for firing a delta 
         * @param launches the launches to set in the delta
         * @param launchFlags the flags for the delta
         */
        protected void fireDelta(ILaunch[] launches, int launchFlags) {
            ModelDelta delta = new ModelDelta(fLaunchManager, IModelDelta.NO_CHANGE);
            for (int i = 0; i < launches.length; i++) {
                if (launches[i] instanceof TCFLaunch) {
                    delta.addNode(launches[i], launchFlags);
                }
            }
            fireModelChanged(delta);                
        }

}
    
    public Control createControl(Composite parent) {
        fComposite = SWTFactory.createComposite(parent, 1, 1, GridData.FILL_BOTH);
        fFilterName = SWTFactory.createLabel(fComposite, "Scope", 1);
        fTreeViewer = new TreeModelViewer(fComposite, SWT.VIRTUAL, new PresentationContext(ITCFDebugUIConstants.ID_CONTEXT_QUERY_VIEW));
        Control control = fTreeViewer.getControl();
        GridData treeLayoutData = new GridData(GridData.FILL_BOTH);
        treeLayoutData.horizontalIndent = 10;
        control.setLayoutData(treeLayoutData);
        GridData gd = new GridData(GridData.FILL_BOTH);
        control.setLayoutData(gd);
        
        return fComposite;
    }

    public void display(IStructuredSelection selection) {
        if (fTreeViewer == null) return;
        
        TCFBreakpointScopeExtension extension = getTCFBreakpointScopeExtension((ICBreakpoint)selection.getFirstElement());
        if (extension != null) {
            String filter = extension.getPropertiesFilter();
            if (filter != null && filter.trim().isEmpty()) {
                filter = null;
            }
            String[] contexts = extension.getThreadFilters();
            
            if (filter != null || contexts != null) {
                fFilterName.setText("Scope");
                Set<String> contextsSet = contexts != null ? new TreeSet<String>(Arrays.asList(contexts)) : null; 
                fTreeViewer.setInput( new ScopeDetailInputObject(
                        new ContextQueryElement(filter, contextsSet)) );
                fTreeViewer.getPresentationContext().setProperty(ITCFDebugUIConstants.PROP_CONTEXT_QUERY, filter);
                fTreeViewer.getPresentationContext().setProperty(ITCFDebugUIConstants.PROP_FILTER_CONTEXTS, contextsSet);
                fTreeViewer.refresh();
                return;
            }
        }
        fFilterName.setText("No scope specified.");
        fTreeViewer.setInput(null);
        fTreeViewer.getPresentationContext().setProperty(ITCFDebugUIConstants.PROP_CONTEXT_QUERY, null);
        fTreeViewer.getPresentationContext().setProperty(ITCFDebugUIConstants.PROP_FILTER_CONTEXTS, null);        
    }

    private TCFBreakpointScopeExtension getTCFBreakpointScopeExtension(ICBreakpoint bp) {
        if (bp == null) return null;
        try {
            return (TCFBreakpointScopeExtension) bp.getExtension(
                ITCFConstants.ID_TCF_DEBUG_MODEL, TCFBreakpointScopeExtension.class);
        } catch (CoreException e) {}
        return null;
    }
    
    public void dispose() {
        if (fTreeViewer != null) {
            fTreeViewer.getControl().dispose();
            fTreeViewer = null;
        }
        if (fFilterName != null) {
            fFilterName.dispose();
            fFilterName = null;
        }
        if (fComposite != null) {
            fComposite.dispose();
            fComposite = null;
        }
    }

    public String getDescription() {
        return DESC;
    }

    public String getID() {
        return ID;
    }

    public String getName() {
        return NAME;
    }

    public void init(IWorkbenchPartSite part_site) {
    }

    public boolean setFocus() {
        if (fTreeViewer == null) return false;
        fTreeViewer.getControl().setFocus();
        return true;
    }
}
