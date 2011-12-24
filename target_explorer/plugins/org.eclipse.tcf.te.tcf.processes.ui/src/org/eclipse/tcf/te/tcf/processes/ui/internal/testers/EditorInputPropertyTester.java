package org.eclipse.tcf.te.tcf.processes.ui.internal.testers;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.processes.ui.model.ProcessModel;
import org.eclipse.tcf.te.tcf.processes.ui.model.ProcessModelManager;
import org.eclipse.ui.IEditorInput;

public class EditorInputPropertyTester extends PropertyTester {

	public EditorInputPropertyTester() {
	}

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if(receiver instanceof IEditorInput) {
			IEditorInput editorInput = (IEditorInput) receiver;
			IPeerModel peer = (IPeerModel) editorInput.getAdapter(IPeerModel.class);
			if(property.equals("isRefreshStopped")) { //$NON-NLS-1$
				ProcessModel processModel = ProcessModelManager.getInstance().getProcessModel(peer);
				return processModel.isRefreshStopped();
			}
		}
		return false;
	}

}
