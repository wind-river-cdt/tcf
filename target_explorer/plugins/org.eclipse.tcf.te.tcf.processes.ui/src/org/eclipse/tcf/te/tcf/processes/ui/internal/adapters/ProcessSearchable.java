/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.processes.ui.internal.adapters;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.te.tcf.processes.core.model.ProcessTreeNode;
import org.eclipse.tcf.te.tcf.processes.ui.internal.columns.ProcessLabelProvider;
import org.eclipse.tcf.te.tcf.processes.ui.nls.Messages;
import org.eclipse.tcf.te.ui.utils.CompositeSearchable;

/**
 * The ISearchable adapter for a ProcessTreeNode which creates a UI for the user to 
 * input the matching condition and returns a matcher to do the matching.
 */
public class ProcessSearchable extends CompositeSearchable {
	// The label provider used to get a text for a process.
	ILabelProvider labelProvider = new ProcessLabelProvider();
	
	/**
	 * Constructor
	 */
	public ProcessSearchable() {
		super(new GeneralSearchable(), new ProcessUserSearchable(), new ProcessStateSearchable());
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.interfaces.ISearchable#getSearchTitle()
	 */
	@Override
    public String getSearchTitle() {
	    return Messages.ProcessSearchable_SearchTitle;
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.interfaces.ISearchable#getSearchMessage(java.lang.Object)
	 */
	@Override
    public String getSearchMessage(Object rootElement) {
		if(rootElement == null) {
			return Messages.ProcessSearchable_PromptFindInProcessList;
		}
		ProcessTreeNode rootNode = (ProcessTreeNode) rootElement;
		if(rootNode.isSystemRoot()) {
			return Messages.ProcessSearchable_PromptFindInProcessList;
		}
		String message = Messages.ProcessSearchable_PromptFindUnderProcess;
		String rootName = getElementName(rootElement);
		message = NLS.bind(message, rootName);
		return message;
    }

	/**
	 * Get a name representation for each process node.
	 * 
	 * @param rootElement The root element whose name is being retrieved.
	 * @return The node's name.
	 */
	private String getElementName(Object rootElement) {
		if(rootElement == null) {
			return Messages.ProcessSearchable_ProcessList;
		}
		ProcessTreeNode rootNode = (ProcessTreeNode) rootElement;
		if(rootNode.isSystemRoot()) {
			return Messages.ProcessSearchable_ProcessList;
		}
		return labelProvider.getText(rootElement);
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
