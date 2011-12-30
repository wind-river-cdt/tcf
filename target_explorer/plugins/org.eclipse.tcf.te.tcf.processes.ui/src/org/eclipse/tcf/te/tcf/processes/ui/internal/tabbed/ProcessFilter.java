package org.eclipse.tcf.te.tcf.processes.ui.internal.tabbed;

import org.eclipse.jface.viewers.IFilter;
import org.eclipse.tcf.te.tcf.processes.ui.model.ProcessTreeNode;

public class ProcessFilter implements IFilter {

	@Override
	public boolean select(Object toTest) {
		if(toTest instanceof ProcessTreeNode) {
			ProcessTreeNode node = (ProcessTreeNode) toTest;
			return !(node.type.equals("ProcRootNode") || node.type.equals("ProcPendingNode")); //$NON-NLS-1$//$NON-NLS-2$
		}
		return false;
	}

}
