package org.eclipse.tcf.te.tcf.processes.ui.internal.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tcf.te.tcf.processes.ui.internal.dialogs.IntervalConfigDialog;
import org.eclipse.ui.handlers.HandlerUtil;

public class ConfigRefreshIntervalHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell parent = HandlerUtil.getActiveShellChecked(event);
		IntervalConfigDialog dialog = new IntervalConfigDialog(parent);
		if(dialog.open() == Window.OK) {
			
		}
		return null;
	}

}
