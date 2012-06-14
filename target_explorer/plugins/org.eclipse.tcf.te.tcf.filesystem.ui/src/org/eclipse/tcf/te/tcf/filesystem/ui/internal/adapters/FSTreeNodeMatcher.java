/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.ui.internal.adapters;

import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;
import org.eclipse.tcf.te.ui.interfaces.ISearchMatcher;
/**
 * The ISearchMatcher implementation for FSTreeNode.
 */
public class FSTreeNodeMatcher implements ISearchMatcher {
	// Whether it is case sensitive
	boolean fCaseSensitive;
	// Whether it is precise matching.
	boolean fMatchPrecise;
	// The current selected target type index.
	int fTargetType;
	// The current target names.
	String fTargetName;
	boolean fIncludeSystem;
	boolean fIncludeHidden;
	
	public FSTreeNodeMatcher(boolean caseSensitive, boolean matchPrecise, int targetType, String targetName, boolean includeSystem, boolean includeHidden) {
		fCaseSensitive = caseSensitive;
		fMatchPrecise = matchPrecise;
		fTargetName = targetName;
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
			if (text == null) return false;
			String target = fTargetName;
			if (!fCaseSensitive) {
				text = text.toLowerCase();
				target = target != null ? target.toLowerCase() : null;
			}
			if (fMatchPrecise) return text.equals(target);
			return text.indexOf(target) != -1;
		}
		return false;
	}
}
