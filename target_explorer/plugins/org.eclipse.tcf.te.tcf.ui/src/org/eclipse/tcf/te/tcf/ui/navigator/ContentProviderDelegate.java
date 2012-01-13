/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.ui.navigator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.tcf.locator.interfaces.IModelListener;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.ILocatorModel;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerRedirector;
import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelLookupService;
import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelRefreshService;
import org.eclipse.tcf.te.tcf.locator.model.Model;
import org.eclipse.tcf.te.tcf.ui.navigator.nodes.PeerRedirectorGroupNode;
import org.eclipse.tcf.te.ui.views.interfaces.IRoot;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.internal.navigator.NavigatorFilterService;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonContentProvider;
import org.eclipse.ui.navigator.INavigatorContentService;
import org.eclipse.ui.navigator.INavigatorFilterService;


/**
 * Content provider delegate implementation.
 */
@SuppressWarnings("restriction")
public class ContentProviderDelegate implements ICommonContentProvider {
	private final static Object[] NO_ELEMENTS = new Object[0];

	// The "Redirected Peers" filter id
	private final static String REDIRECT_PEERS_FILTER_ID = "org.eclipse.tcf.te.tcf.ui.navigator.RedirectPeersFilter"; //$NON-NLS-1$

	// The locator model listener instance
	/* default */ IModelListener modelListener = null;

	// Internal map of RemotePeerDiscoverRootNodes per peer id
	private final Map<String, PeerRedirectorGroupNode> roots = new HashMap<String, PeerRedirectorGroupNode>();

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	@Override
	public Object[] getChildren(Object parentElement) {
		Object[] children = NO_ELEMENTS;

		// If the parent element is null or IRoot, than we assume
		// the locator model as parent element.
		if (parentElement == null || parentElement instanceof IRoot) {
			parentElement = Model.getModel();
		}
		// If it is the locator model, get the peers
		if (parentElement instanceof ILocatorModel) {
			children = ((ILocatorModel)parentElement).getPeers();
		}
		// If it is a peer model itself, get the child peers
		else if (parentElement instanceof IPeerModel) {
			String parentPeerId = ((IPeerModel)parentElement).getPeerId();
			List<IPeerModel> candidates = Model.getModel().getChildren(parentPeerId);
			if (candidates != null && candidates.size() > 0) {
				PeerRedirectorGroupNode rootNode = roots.get(parentPeerId);
				if (rootNode == null) {
					rootNode = new PeerRedirectorGroupNode(parentPeerId);
					roots.put(parentPeerId, rootNode);
				}
				children = new Object[] { rootNode };
			} else {
				roots.remove(parentPeerId);
			}
		}
		// If it is a remote peer discover root node, return the children
		// for the associated peer id.
		else if (parentElement instanceof PeerRedirectorGroupNode) {
			List<IPeerModel> candidates = Model.getModel().getChildren(((PeerRedirectorGroupNode)parentElement).peerId);
			if (candidates != null && candidates.size() > 0) {
				children = candidates.toArray();
			}
		}

		return children;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	@Override
	public Object getParent(final Object element) {
		// If it is a peer model node, return the parent locator model
		if (element instanceof IPeerModel) {
			// If it is a peer redirector, return the parent remote peer discover root node
			if (((IPeerModel)element).getPeer() instanceof IPeerRedirector) {
				IPeer parentPeer =  ((IPeerRedirector)((IPeerModel)element).getPeer()).getParent();
				String parentPeerId = parentPeer.getID();
				if (!roots.containsKey(parentPeerId)) roots.put(parentPeer.getID(), new PeerRedirectorGroupNode(parentPeerId));
				return roots.get(parentPeerId);
			}
			return ((IPeerModel)element).getModel();
		} else if (element instanceof PeerRedirectorGroupNode) {
			// Return the parent peer model node
			final AtomicReference<IPeerModel> parent = new AtomicReference<IPeerModel>();
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					parent.set(Model.getModel().getService(ILocatorModelLookupService.class).lkupPeerModelById(((PeerRedirectorGroupNode)element).peerId));
				}
			};
			if (Protocol.isDispatchThread()) runnable.run();
			else Protocol.invokeAndWait(runnable);

			return parent.get();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	@Override
	public boolean hasChildren(Object element) {
		boolean hasChildren = false;

		if (element instanceof ILocatorModel) {
			hasChildren = ((ILocatorModel)element).getPeers().length > 0;
		}
		else if (element instanceof IPeerModel) {
			List<IPeerModel> children = Model.getModel().getChildren(((IPeerModel)element).getPeerId());
			hasChildren = children != null && children.size() > 0;
		}
		else if (element instanceof PeerRedirectorGroupNode) {
			List<IPeerModel> children = Model.getModel().getChildren(((PeerRedirectorGroupNode)element).peerId);
			hasChildren = children != null && children.size() > 0;
		}

		return hasChildren;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	@Override
	public void dispose() {
		roots.clear();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		final ILocatorModel model = Model.getModel();

		// Create and attach the model listener if not yet done
		if (modelListener == null && model != null && viewer instanceof CommonViewer) {
			modelListener = new ModelListener(model, (CommonViewer)viewer);
			Protocol.invokeLater(new Runnable() {
				@Override
				public void run() {
					model.addListener(modelListener);
				}
			});
		}

		if (model != null && newInput instanceof IRoot) {
			// Refresh the model asynchronously
			Protocol.invokeLater(new Runnable() {
				@Override
				public void run() {
					model.getService(ILocatorModelRefreshService.class).refresh();
				}
			});
		}
	}

	/* (non-Javadoc)
     * @see org.eclipse.ui.navigator.ICommonContentProvider#init(org.eclipse.ui.navigator.ICommonContentExtensionSite)
     */
    @Override
    public void init(ICommonContentExtensionSite config) {
    	Assert.isNotNull(config);

    	// Make sure that the hidden "Redirected Peers" filter is active
    	INavigatorContentService cs = config.getService();
    	INavigatorFilterService fs = cs != null ? cs.getFilterService() : null;
		if (fs != null && !fs.isActive(REDIRECT_PEERS_FILTER_ID)) {
			if (fs instanceof NavigatorFilterService) {
				NavigatorFilterService navFilterService = (NavigatorFilterService)fs;
				navFilterService.addActiveFilterIds(new String[] { REDIRECT_PEERS_FILTER_ID });
				navFilterService.updateViewer();
			}
		}
    }

	/* (non-Javadoc)
     * @see org.eclipse.ui.navigator.IMementoAware#restoreState(org.eclipse.ui.IMemento)
     */
    @Override
    public void restoreState(IMemento aMemento) {
    }

	/* (non-Javadoc)
     * @see org.eclipse.ui.navigator.IMementoAware#saveState(org.eclipse.ui.IMemento)
     */
    @Override
    public void saveState(IMemento aMemento) {
    }
}
