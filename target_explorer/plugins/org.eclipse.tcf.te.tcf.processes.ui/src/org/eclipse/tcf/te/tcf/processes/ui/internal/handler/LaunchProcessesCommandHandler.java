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

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tcf.te.runtime.callback.Callback;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.runtime.properties.PropertiesContainer;
import org.eclipse.tcf.te.runtime.statushandler.StatusHandlerManager;
import org.eclipse.tcf.te.runtime.statushandler.interfaces.IStatusHandler;
import org.eclipse.tcf.te.runtime.statushandler.interfaces.IStatusHandlerConstants;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.processes.core.interfaces.launcher.IProcessLauncher;
import org.eclipse.tcf.te.tcf.processes.core.launcher.ProcessLauncher;
import org.eclipse.tcf.te.tcf.processes.ui.help.IContextHelpIds;
import org.eclipse.tcf.te.tcf.processes.ui.internal.dialogs.LaunchObjectDialog;
import org.eclipse.tcf.te.tcf.processes.ui.nls.Messages;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Launch a process on the selected peer.
 */
public class LaunchProcessesCommandHandler extends AbstractHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		LaunchObjectDialog dialog = createLaunchDialog(event);
		IPeerModel node = getPeerNode(event);
		dialog.setNode(node);
		if (dialog.open() == Window.OK) {
			Map<String, Object> attrs = dialog.getLaunchAttributes();
			if (attrs != null) {
				ProcessLauncher launcher = new ProcessLauncher();
				attrs.put(IProcessLauncher.PROP_PROCESS_ASSOCIATE_CONSOLE, Boolean.TRUE);
				IPropertiesContainer properties = new PropertiesContainer();
				properties.setProperties(attrs);
				launcher.launch(node.getPeer(), properties, new Callback() {
					@Override
					public void internalDone(Object caller, IStatus status) {
						handleStatus(status);
					}
				});
			}
		}
		return null;
	}

	/**
	 * Get the selected peer model from the execution event.
	 * 
	 * @param event The execution event with which the handler is invoked.
	 * @return The peer model selected in this execution event.
	 * @throws ExecutionException The exception when getting this peer model.
	 */
	private IPeerModel getPeerNode(ExecutionEvent event) throws ExecutionException {
	    IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getActiveMenuSelectionChecked(event);
		Assert.isTrue(selection.size() == 1);
		Object element = selection.getFirstElement();
		Assert.isTrue(element instanceof IPeerModel);
		return (IPeerModel) element;
    }

	/**
	 * Create a launch dialog in which the configuration for this launch is entered.
	 * 
	 * @param event The execution event with which the handler is invoked.
	 * @return The launch dialog to input the configuration.
	 * @throws ExecutionException Exception thrown during creating the dialog
	 */
	private LaunchObjectDialog createLaunchDialog(ExecutionEvent event) throws ExecutionException {
	    Shell shell = HandlerUtil.getActiveShellChecked(event);
		IWorkbenchPart activePart = HandlerUtil.getActivePartChecked(event);
		IEditorPart editorPart = (IEditorPart) activePart.getAdapter(IEditorPart.class);
		return new LaunchObjectDialog(editorPart, shell);
    }

	/**
	 * Handle the resulting status of the process launching.
	 * 
	 * @param status The resulting status of the process launching.
	 */
	protected void handleStatus(IStatus status) {
		if (status.getSeverity() != IStatus.OK && status.getSeverity() != IStatus.CANCEL) {
			IStatusHandler[] handlers = StatusHandlerManager.getInstance().getHandler(getClass());
			if (handlers != null && handlers.length > 0) {
				IPropertiesContainer data = new PropertiesContainer();
				data.setProperty(IStatusHandlerConstants.PROPERTY_TITLE, Messages.AbstractChannelCommandHandler_statusDialog_title);
				data.setProperty(IStatusHandlerConstants.PROPERTY_CONTEXT_HELP_ID, IContextHelpIds.CHANNEL_COMMAND_HANDLER_STATUS_DIALOG);
				data.setProperty(IStatusHandlerConstants.PROPERTY_DONT_ASK_AGAIN_ID, null);
				data.setProperty(IStatusHandlerConstants.PROPERTY_CALLER, this);
				handlers[0].handleStatus(status, data, null);
			}
		}
    }
}
