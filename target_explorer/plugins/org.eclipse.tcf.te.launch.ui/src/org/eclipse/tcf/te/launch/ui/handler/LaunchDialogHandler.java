/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.ui.handler;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.te.launch.core.lm.LaunchConfigHelper;
import org.eclipse.tcf.te.launch.core.lm.LaunchManager;
import org.eclipse.tcf.te.launch.ui.activator.UIPlugin;
import org.eclipse.tcf.te.launch.ui.model.LaunchNode;
import org.eclipse.tcf.te.launch.ui.nls.Messages;
import org.eclipse.tcf.te.ui.jface.dialogs.OptionalMessageDialog;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Launch dialog handler implementation.
 */
public class LaunchDialogHandler extends AbstractHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Get the current selection
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection && ((IStructuredSelection) selection).size() == 1) {
			Object element = ((IStructuredSelection)selection).getFirstElement();
			if (element instanceof LaunchNode) {
				doLaunch((LaunchNode)element);
			}
		}
		return null;
	}

	protected void doLaunch(LaunchNode node) {
		if (node != null && node.getLaunchConfiguration() != null) {
			final String[] modes = LaunchConfigHelper.getLaunchConfigTypeModes(node.getLaunchConfigurationType(), false);
			List<String> modeLabels = new ArrayList<String>();
			int defaultIndex = 0;
			for (String mode : modes) {
				if (LaunchManager.getInstance().validate(node.getLaunchConfiguration(), mode)) {
					ILaunchMode launchMode = DebugPlugin.getDefault().getLaunchManager().getLaunchMode(mode);
					modeLabels.add(launchMode.getLabel());
					if (mode.equals(ILaunchManager.DEBUG_MODE)) {
						defaultIndex = modeLabels.size()-1;
					}
				}
			}
			if (modeLabels.size() >= 1) {
				modeLabels.add(IDialogConstants.CANCEL_LABEL);
				OptionalMessageDialog dialog = new OptionalMessageDialog(
								UIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell(),
								Messages.LaunchDialogHandler_dialog_title,
								null,
								NLS.bind(Messages.LaunchDialogHandler_dialog_message, node.getLaunchConfigurationType().getName(), node.getLaunchConfiguration().getName()),
								MessageDialog.QUESTION,
								modeLabels.toArray(new String[modeLabels.size()]),
								defaultIndex,
								null, null);
				int result = dialog.open();
				if (result >= IDialogConstants.INTERNAL_ID) {
					DebugUITools.launch(node.getLaunchConfiguration(), modes[result - IDialogConstants.INTERNAL_ID]);
				}
			}
		}
	}
}
