package org.eclipse.tcf.te.tcf.filesystem.internal.tabbed;

import org.eclipse.jface.viewers.IFilter;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;

public class FolderFilter implements IFilter {

	@Override
	public boolean select(Object toTest) {
		if(toTest instanceof FSTreeNode) {
			FSTreeNode node = (FSTreeNode) toTest;
			return !node.isSystemRoot() && node.isDirectory();
		}
		return false;
	}

}
