/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 * William Chen (Wind River)- [345552] Edit the remote files with a proper editor
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.internal.adapters;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.tcf.te.tcf.filesystem.internal.columns.FSTreeElementLabelProvider;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.tcf.te.ui.views.interfaces.handler.IRefreshHandlerDelegate;
import org.eclipse.ui.IActionFilter;

/**
 * The adapter factory of <code>FSTreeNode</code> over <code>IActionFilter</code>
 */
public class NodeStateFilterFactory implements IAdapterFactory {
	private static ILabelProvider nodeLabelProvider = new FSTreeElementLabelProvider();
	private static IRefreshHandlerDelegate refreshDelegate = new RefreshHandlerDelegate();
	// The ADAPTERS adapted by this factory.
	private static Class<?>[] ADAPTERS = {IActionFilter.class, ILabelProvider.class, IRefreshHandlerDelegate.class};
	// The fFilters map caching fFilters for FS nodes.
	private Map<FSTreeNode, NodeStateFilter> filters;

	/**
	 * Constructor.
	 */
	public NodeStateFilterFactory(){
		this.filters = Collections.synchronizedMap(new HashMap<FSTreeNode, NodeStateFilter>());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if(adaptableObject instanceof FSTreeNode) {
			if(adapterType == IActionFilter.class) {
				FSTreeNode node = (FSTreeNode) adaptableObject;
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
			else if(adapterType == IRefreshHandlerDelegate.class) {
				return refreshDelegate;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	@Override
	public Class[] getAdapterList() {
		return ADAPTERS;
	}
}
