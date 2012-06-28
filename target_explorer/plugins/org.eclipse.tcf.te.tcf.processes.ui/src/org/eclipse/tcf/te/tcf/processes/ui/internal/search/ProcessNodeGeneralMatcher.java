/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.processes.ui.internal.search;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.tcf.te.tcf.processes.core.model.ProcessTreeNode;
import org.eclipse.tcf.te.tcf.processes.ui.internal.columns.ProcessLabelProvider;
import org.eclipse.tcf.te.ui.interfaces.ISearchMatcher;
import org.eclipse.tcf.te.ui.utils.StringMatcher;
/**
 * The ISearchMatcher implementation for a Process Tree Node.
 */
public class ProcessNodeGeneralMatcher implements ISearchMatcher {
	// Whether it is case sensitive
	private boolean fCaseSensitive;
	// Whether it is precise matching.
	private boolean fMatchPrecise;
	// The string matcher for matching.
	private StringMatcher fStringMatcher;
	// The label provider used to get a text for a process.
	private ILabelProvider labelProvider = new ProcessLabelProvider();
	// The current target names.
	private String fTargetName;
	
	/**
	 * Constructor with options.
	 * 
	 * @param caseSensitive
	 * @param matchPrecise
	 * @param targetName
	 */
	public ProcessNodeGeneralMatcher(boolean caseSensitive, boolean matchPrecise, String targetName) {
		fCaseSensitive = caseSensitive;
		fTargetName = targetName;
		fMatchPrecise = matchPrecise;
		if (!fMatchPrecise) {
			fStringMatcher = new StringMatcher(fTargetName, !fCaseSensitive, false);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.interfaces.ISearchMatcher#match(java.lang.Object)
	 */
	@Override
	public boolean match(Object context) {
		if (context == null) return false;
		if (context instanceof ProcessTreeNode) {
			ProcessTreeNode node = (ProcessTreeNode) context;
			if (!node.isSystemRoot()) {
				String text = labelProvider.getText(node);
				if (text != null) {
					if (fMatchPrecise) {
						return fCaseSensitive ? text.equals(fTargetName) : text.equalsIgnoreCase(fTargetName);
					}
					return fStringMatcher.match(text);
				}
			}
		}
		return false;
	}
}
