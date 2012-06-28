/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.ui.internal.search;

import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;
import org.eclipse.tcf.te.ui.interfaces.ISearchMatcher;
import org.eclipse.tcf.te.ui.utils.StringMatcher;
/**
 * The ISearchMatcher implementation for FSTreeNode.
 */
public class FSTreeNodeMatcher implements ISearchMatcher {
	// Whether it is case sensitive
	private boolean fCaseSensitive;
	// Whether it is precise matching.
	private boolean fMatchPrecise;
	// The string matcher used for matching.
	private StringMatcher fStringMatcher;
	// The current selected target type index.
	private int fTargetType;
	// The current target names.
	private String fTargetName;
	// The flag if system files should be included
	private boolean fIncludeSystem;
	// The flag if hidden files should be included
	private boolean fIncludeHidden;
	
	/**
	 * Constructor with different option parameters.
	 * 
	 * @param caseSensitive Option of case sensitive
	 * @param matchPrecise Option of precise matching
	 * @param targetType Option of the target type
	 * @param targetName Option of the target name
	 * @param includeSystem Option if system files be included
	 * @param includeHidden Option if hidden files be included
	 */
	public FSTreeNodeMatcher(boolean caseSensitive, boolean matchPrecise, 
					int targetType, String targetName, boolean includeSystem, boolean includeHidden) {
		fCaseSensitive = caseSensitive;
		fTargetName = targetName;
		fMatchPrecise = matchPrecise;
		if (!fMatchPrecise) {
			fStringMatcher = new StringMatcher(fTargetName, !fCaseSensitive, false);
		}
		fTargetType = targetType;
		fIncludeSystem = includeSystem;
		fIncludeHidden = includeHidden;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.interfaces.ISearchMatcher#match(java.lang.Object)
	 */
	@Override
	public boolean match(Object context) {
		if (context == null) return false;
		if (context instanceof FSTreeNode) {
			FSTreeNode node = (FSTreeNode) context;
			if(fTargetType == 1 && !node.isFile() || fTargetType == 2 && !node.isDirectory()) return false;
			if(!fIncludeSystem && node.isSystem()) return false;
			if(!fIncludeHidden && node.isHidden()) return false;
			String text = node.name;
			if (text != null) {
				if (fMatchPrecise) {
					return fCaseSensitive ? text.equals(fTargetName) : text.equalsIgnoreCase(fTargetName);
				}
				return fStringMatcher.match(text);
			}
		}
		return false;
	}
}
