/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.ui.internal.adapters;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.filesystem.ui.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.filesystem.ui.internal.columns.FSTreeElementLabelProvider;
import org.eclipse.tcf.te.tcf.filesystem.ui.internal.search.FSTreeNodeSearchable;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.ui.interfaces.ILazyLoader;
import org.eclipse.tcf.te.ui.interfaces.ISearchable;
import org.eclipse.ui.IActionFilter;
import org.eclipse.ui.IPersistableElement;

/**
 * The adapter factory of <code>FSTreeNode</code> over <code>IActionFilter</code>
 */
public class FSTreeNodeAdapterFactory implements IAdapterFactory {
	private static ILabelProvider nodeLabelProvider = new FSTreeElementLabelProvider();
	// The fFilters map caching fFilters for FS nodes.
	private Map<FSTreeNode, NodeStateFilter> filters;

	/**
	 * Constructor.
	 */
	public FSTreeNodeAdapterFactory(){
		this.filters = Collections.synchronizedMap(new HashMap<FSTreeNode, NodeStateFilter>());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if(adaptableObject instanceof FSTreeNode) {
			FSTreeNode node = (FSTreeNode) adaptableObject;
			if(adapterType == IActionFilter.class) {
				NodeStateFilter filter = filters.get(node);
				if(filter == null){
					filter = new NodeStateFilter(node);
					filters.put(node, filter);
				}
				return filter;
			}
			else if(adapterType == ILabelProvider.class) {
				return nodeLabelProvider;
			}
			else if(adapterType == IPersistableElement.class && UIPlugin.isExpandedPersisted()) {
				return new PersistableNode(node);
			}
			else if(adapterType == ILazyLoader.class) {
				return new FSTreeNodeLoader(node);
			}
			else if(adapterType == IPeerModel.class) {
				return node.getPeerModel();
			}
			else if(adapterType == ISearchable.class) {
				return new FSTreeNodeSearchable(node);
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	@Override
	public Class[] getAdapterList() {
		return new Class[] { IActionFilter.class, ILabelProvider.class, IPersistableElement.class, ILazyLoader.class, ISearchable.class };
	}
}
