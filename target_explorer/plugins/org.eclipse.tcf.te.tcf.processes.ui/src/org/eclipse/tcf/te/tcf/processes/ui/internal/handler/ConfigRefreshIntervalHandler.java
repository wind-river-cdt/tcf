/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.processes.ui.internal.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.processes.ui.internal.dialogs.IntervalConfigDialog;
import org.eclipse.tcf.te.tcf.processes.ui.model.ProcessModel;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * The handler to configure the refreshing interval in a dialog.
 */
public class ConfigRefreshIntervalHandler extends AbstractHandler {

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorInput editorInput = HandlerUtil.getActiveEditorInputChecked(event);
		IPeerModel peer = (IPeerModel) editorInput.getAdapter(IPeerModel.class);
		if (peer != null) {
			Shell parent = HandlerUtil.getActiveShellChecked(event);
			IntervalConfigDialog dialog = new IntervalConfigDialog(parent);
			ProcessModel model = ProcessModel.getProcessModel(peer);
			int interval = model.getInterval();
			dialog.setResult(interval);
			if (dialog.open() == Window.OK) {
				interval = dialog.getResult();
				ProcessModel processModel = ProcessModel.getProcessModel(peer);
				processModel.setInterval(interval);
				processModel.addMRUInterval(interval);
			}
		}
		return null;
	}

}
