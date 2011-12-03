/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.sequoyah.vnc.handler;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.sequoyah.vnc.vncviewer.vncviews.views.Messages;
import org.eclipse.sequoyah.vnc.vncviewer.vncviews.views.VNCViewerView;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Launch terminal command handler implementation.
 */
public class LaunchVNCCommandHandler extends AbstractHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Get the current selection
		ISelection selection = HandlerUtil.getCurrentSelection(event);

		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
			Object element = ((IStructuredSelection)selection).getFirstElement();
			if (element instanceof IPeerModel) {
				final IPeerModel peerModel = (IPeerModel)element;
				final AtomicReference<String> ip = new AtomicReference<String>();

				Runnable runnable = new Runnable() {
					@Override
					public void run() {
						ip.set(peerModel.getPeer().getAttributes().get(IPeer.ATTR_IP_HOST));
					}
				};

				if (Protocol.isDispatchThread()) runnable.run();
				else Protocol.invokeAndWait(runnable);

				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					page.showView("org.eclipse.sequoyah.vnc.vncviewer.vncviews.views.VNCViewerView"); //$NON-NLS-1$

					VNCViewerView.start(ip.get(), 5900, "VNC 3.7", "123456", false); //$NON-NLS-1$ //$NON-NLS-2$
				} catch (Exception e) { /* ignored on purpose */ }
			}
		}

		return null;
	}

	// Copied over the connect job implementation from org.eclipse.sequoyah.vnc.vncviewer.vncviews.views.OpenConnectionDialog.
	// As it would preferable to use the OpenConnectionDialog directly, it has some bad layout and does not allow to preset
	// the dialog fields from outside. To get rid of the connect job implementation here, we have to try to get in some extension
	// to the dialog to make it more pretty.

	public class ConnectJob extends Job{
		final String host;
		final int port;
		final String password;
		final String version;
		final boolean isBypassProxy;

		public ConnectJob(String host,int port,String password,String version,boolean isBypassProxy){
			super(Messages.OpenConnectionDialog_1+host+":"+port); //$NON-NLS-1$
			this.host=host;
			this.port=port;
			this.password=password;
			this.version=version;
			this.isBypassProxy=isBypassProxy;

		}

		@Override
		public IStatus run(IProgressMonitor monitor) {
			monitor.beginTask(Messages.OpenConnectionDialog_2, 2);
			if(VNCViewerView.getSWTRemoteDisplay().isActive()){
				if(isStopExistingClient()){
					VNCViewerView.stop();
					try {
						VNCViewerView.stopProtocol();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}else{
					monitor.done();
					return Status.OK_STATUS;
				}
			}

			if(!isStepNeeded(monitor, Messages.OpenConnectionDialog_3)){
				return Status.CANCEL_STATUS;
			}

			VNCViewerView.start(host, port, version, password,isBypassProxy);
			monitor.done();
			return Status.OK_STATUS;
		}

		private boolean isStepNeeded(IProgressMonitor monitor,String stepName){
			if(!monitor.isCanceled()){
				monitor.worked(1);
				monitor.setTaskName(stepName);
				return true;
			}
			return false;
		}
	}

	public boolean isStopExistingClient() {
		final Display display = Display.getDefault();
		RunnableMessageDialog msgd = new RunnableMessageDialog(display);
		display.syncExec(msgd);
		return msgd.returnCode==0;
	}

	private class RunnableMessageDialog implements Runnable{
		private Display display;
		int returnCode;

		public RunnableMessageDialog(Display display){
			this.display=display;
		}

		@Override
        public void run() {

			MessageDialog dialog =
							new MessageDialog(display.getActiveShell(),
											Messages.OpenConnectionDialog_6,
											null,
											Messages.OpenConnectionDialog_7 + VNCViewerView.getCurrentHost()
											+ ":" + VNCViewerView.getCurrentPort() + "?", //$NON-NLS-1$ //$NON-NLS-2$
											MessageDialog.QUESTION,
											new String[]{Messages.OpenConnectionDialog_10,Messages.OpenConnectionDialog_11},
											0);
			dialog.open();
			returnCode=dialog.getReturnCode();
		}

	}

}
