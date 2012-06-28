/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.ui.internal.search;

import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.filesystem.ui.nls.Messages;
import org.eclipse.tcf.te.ui.utils.CompositeSearchable;

/**
 * The ISearchable adapter for a FSTreeNode which creates a UI for the user to 
 * input the matching condition and returns a matcher to do the matching.
 */
public class FSTreeNodeSearchable extends CompositeSearchable {

	/**
	 * Create an instance with the specified node.
	 * 
	 * @param node The directory node.
	 */
	public FSTreeNodeSearchable(FSTreeNode node) {
		super(new FSGeneralSearchable(node), new FSModifiedSearchable(), new FSSizeSearchable());
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.interfaces.ISearchable#getSearchTitle()
	 */
	@Override
    public String getSearchTitle() {
	    return Messages.FSTreeNodeSearchable_FindFilesAndFolders;
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.interfaces.ISearchable#getSearchMessage(java.lang.Object)
	 */
	@Override
    public String getSearchMessage(Object rootElement) {
		String message = Messages.FSTreeNodeSearchable_FindMessage;
		FSTreeNode rootNode = (FSTreeNode) rootElement;
		String rootName = getElementName(rootElement);
		if (rootNode != null && !rootNode.isSystemRoot()) rootName = "\"" + rootName + "\""; //$NON-NLS-1$//$NON-NLS-2$
		message = NLS.bind(message, rootName);
		return message;
    }

	/**
	 * Get a name representation for each file node.
	 * 
	 * @param rootElement The root element whose name is being retrieved.
	 * @return The node's name or an expression for the file system.
	 */
	private String getElementName(Object rootElement) {
		if(rootElement == null) {
			return Messages.FSTreeNodeSearchable_SelectedFileSystem;
		}
		FSTreeNode rootNode = (FSTreeNode) rootElement;
		if(rootNode.isSystemRoot()) {
			return Messages.FSTreeNodeSearchable_SelectedFileSystem;
		}
		return rootNode.name;
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.interfaces.ISearchable#getElementText(java.lang.Object)
	 */
	@Override
    public String getElementText(Object element) {
	    return getElementName(element);
    }
}
