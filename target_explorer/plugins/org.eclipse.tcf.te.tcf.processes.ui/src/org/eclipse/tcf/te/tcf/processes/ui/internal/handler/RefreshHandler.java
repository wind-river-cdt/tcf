package org.eclipse.tcf.te.tcf.processes.ui.internal.handler;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.tcf.te.tcf.processes.core.model.ProcessModel;
import org.eclipse.tcf.te.tcf.processes.core.model.ProcessTreeNode;
import org.eclipse.ui.handlers.HandlerUtil;

public class RefreshHandler extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
			List<ProcessTreeNode> processes = ((IStructuredSelection) selection).toList();
			for(ProcessTreeNode process : processes) {
				ProcessModel model = ProcessModel.getProcessModel(process.peerNode);
				model.refresh(process);
			}
		}
		return null;
	}
}
