/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.internal.operations;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.services.IFileSystem;
import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tcf.te.tcf.core.Tcf;
import org.eclipse.tcf.te.tcf.filesystem.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.filesystem.internal.exceptions.TCFException;
import org.eclipse.tcf.te.tcf.filesystem.internal.exceptions.TCFFileSystemException;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.filesystem.nls.Messages;
import org.eclipse.ui.PlatformUI;

/**
 * FSRefresh refreshes a specified tree node and its children and grand children recursively.
 */
public class FSRefresh extends FSOperation {
	/**
	 * The root node to be refreshed.
	 */
	FSTreeNode node;

	// The callback when everything is done
	ICallback callback;

	/**
	 * Create an FSRefresh to refresh the specified node and its descendants.
	 *
	 * @param node The root node to be refreshed.
	 */
	public FSRefresh(FSTreeNode node) {
		this.node = node;
	}

	/**
	 * Create an FSRefresh to refresh the specified node and its descendants.
	 *
	 * @param node The root node to be refreshed.
	 * @param callback The callback
	 */
	public FSRefresh(FSTreeNode node, ICallback callback) {
		this.node = node;
		this.callback = callback;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSOperation#doit()
	 */
	@Override
	public IStatus doit() {
		Assert.isNotNull(Display.getCurrent());
		Job job = new Job(NLS.bind(Messages.RefreshDirectoryHandler_RefreshJobTitle, node.name)){
			@Override
            protected IStatus run(IProgressMonitor monitor) {
				if (node.childrenQueried) {
					IChannel channel = null;
					try {
						channel = openChannel(node.peerNode.getPeer());
						if (channel != null) {
							IFileSystem service = getBlockingFileSystem(channel);
							if (service != null) {
								refresh(node, service);
							}
							else {
								String message = NLS.bind(Messages.FSOperation_NoFileSystemError, node.peerNode.getPeerId());
								throw new TCFFileSystemException(message);
							}
						}
					}
					catch (TCFException e) {
						return new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(), e.getMessage(), e);
					}
					finally {
						if (channel != null) Tcf.getChannelManager().closeChannel(channel);
					}
				}
	            return Status.OK_STATUS;
            }};
        job.addJobChangeListener(new JobChangeAdapter(){
			@Override
            public void done(final IJobChangeEvent event) {
				Display display = PlatformUI.getWorkbench().getDisplay();
				display.asyncExec(new Runnable(){
					@Override
                    public void run() {
						doneRefresh(event);
                    }});
            }});
        job.schedule();
		return Status.OK_STATUS;
	}

	/**
	 * Called when the refresh is done. Must be called within UI-thread.
	 * 
	 * @param event The job change event.
	 */
	void doneRefresh(IJobChangeEvent event) {
		Assert.isNotNull(Display.getCurrent());
		IStatus status = event.getResult();
		if (callback != null) {
			callback.done(this, status);
		}
	}
	
	/**
	 * Refresh the specified node and its children recursively using the file system service.
	 *
	 * @param node The node to be refreshed.
	 * @param service The file system service.
	 * @throws TCFFileSystemException Thrown during refreshing.
	 */
	void refresh(final FSTreeNode node, final IFileSystem service) {
		if ((node.isSystemRoot() || node.isDirectory()) && node.childrenQueried) {
			if (!node.isSystemRoot()) {
				SafeRunner.run(new SafeRunnable(){
					@Override
                    public void handleException(Throwable e) {
						// Ignore exception
                    }
					@Override
                    public void run() throws Exception {
						updateChildren(node, service);
                    }});
			}
			List<FSTreeNode> children = node.unsafeGetChildren();
			for (FSTreeNode child : children) {
				refresh(child, service);
			}
		}
	}


	/**
	 * Update the children of the specified folder node using the file system service.
	 *
	 * @param node The folder node.
	 * @param service The file system service.
	 * @throws TCFFileSystemException Thrown during querying the children nodes.
	 */
	protected void updateChildren(final FSTreeNode node, final IFileSystem service) throws TCFFileSystemException {
		List<FSTreeNode> current = node.unsafeGetChildren();
		List<FSTreeNode> latest = queryChildren(node, service);
		List<FSTreeNode> newNodes = diff(latest, current);
		List<FSTreeNode> deleted = diff(current, latest);
		node.removeChildren(deleted);
		node.addChidren(newNodes);
	}

	/**
	 * Find those nodes which are in aList yet not in bList and return them as a list.
	 *
	 * @param aList
	 * @param bList
	 * @return the difference list.
	 */
	private List<FSTreeNode> diff(List<FSTreeNode> aList, List<FSTreeNode> bList) {
		List<FSTreeNode> newList = new ArrayList<FSTreeNode>();
		for (FSTreeNode aNode : aList) {
			boolean found = false;
			for (FSTreeNode bNode : bList) {
				if (aNode.name.equals(bNode.name)) {
					found = true;
					break;
				}
			}
			if (!found) {
				newList.add(aNode);
			}
		}
		return newList;
	}
}
