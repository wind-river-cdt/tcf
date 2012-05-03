package org.eclipse.tcf.te.tcf.filesystem.ui.internal.handlers;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.IOpExecutor;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.JobExecutor;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.OpRefresh;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;
import org.eclipse.ui.handlers.HandlerUtil;

public class RefreshHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
			List<FSTreeNode> nodes = ((IStructuredSelection) selection).toList();
			IOpExecutor executor = new JobExecutor();
			executor.execute(new OpRefresh(nodes));
		}
		return null;
	}
}
