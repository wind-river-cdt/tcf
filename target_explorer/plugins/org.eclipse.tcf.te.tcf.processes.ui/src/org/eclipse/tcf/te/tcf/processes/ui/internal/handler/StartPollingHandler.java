package org.eclipse.tcf.te.tcf.processes.ui.internal.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.processes.ui.model.ProcessModel;
import org.eclipse.tcf.te.tcf.processes.ui.model.ProcessModelManager;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.handlers.HandlerUtil;

public class StartPollingHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorInput editorInput = HandlerUtil.getActiveEditorInputChecked(event);
		IPeerModel peer = (IPeerModel) editorInput.getAdapter(IPeerModel.class);
		if (peer != null) {
			ProcessModel processModel = ProcessModelManager.getInstance().getProcessModel(peer);
			processModel.startPolling();
		}
		return null;
	}

}
