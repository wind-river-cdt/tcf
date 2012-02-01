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

import java.io.File;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.services.IFileSystem;
import org.eclipse.tcf.services.IFileSystem.DoneRename;
import org.eclipse.tcf.services.IFileSystem.FileSystemException;
import org.eclipse.tcf.te.tcf.core.Tcf;
import org.eclipse.tcf.te.tcf.filesystem.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.filesystem.internal.exceptions.TCFException;
import org.eclipse.tcf.te.tcf.filesystem.internal.exceptions.TCFFileSystemException;
import org.eclipse.tcf.te.tcf.filesystem.internal.utils.CacheManager;
import org.eclipse.tcf.te.tcf.filesystem.internal.utils.PersistenceManager;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.filesystem.nls.Messages;
import org.eclipse.ui.PlatformUI;
/**
 * FSRename renames the specified file/folder to a
 * new name.
 *
 */
public class FSRename extends FSOperation {
	// The file/folder node to be renamed.
	FSTreeNode node;
	// The new name the file/folder is renamed to.
	String newName;

	/**
	 * Create a rename operation that renames the node with the new name.
	 *
	 * @param node The file/folder node to be renamed.
	 * @param newName The new name of this node.
	 */
	public FSRename(FSTreeNode node, String newName) {
		this.node = node;
		this.newName = newName;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSOperation#doit()
	 */
	@Override
	public IStatus doit() {
		Assert.isNotNull(Display.getCurrent());
		Job job = new Job(Messages.FSRename_RenameFileFolderTitle) {
			@Override
            protected IStatus run(IProgressMonitor monitor) {
				IChannel channel = null;
				try {
					channel = openChannel(node.peerNode.getPeer());
					if (channel != null) {
						IFileSystem service = getBlockingFileSystem(channel);
						if (service != null) {
							renameNode(service);
						}
						else {
							String message = NLS.bind(Messages.FSOperation_NoFileSystemError, node.peerNode.getPeerId());
							throw new TCFFileSystemException(message);
						}
						return Status.OK_STATUS;
					}
				}
				catch (TCFException e) {
					return new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(), e.getLocalizedMessage(), e);
				}
				finally {
					if (channel != null) Tcf.getChannelManager().closeChannel(channel);
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
						doneRename(event);
                    }});
            }});
		job.schedule();
		return Status.OK_STATUS;
	}

	/**
	 * Called when the renaming is done. Must be called within UI-thread.
	 * 
	 * @param event The job change event.
	 */
	void doneRename(IJobChangeEvent event) {
		Assert.isNotNull(Display.getCurrent());
		IStatus status = event.getResult();
		if (!status.isOK()) {
			String message = status.getMessage();
			Shell parent = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			MessageDialog.openError(parent, Messages.FSRename_RenameFileFolderTitle, message);
		}
	}
	
	/**
	 * Rename the node using the new name.
	 *
	 * @param service File system service used to rename.
	 * @throws TCFFileSystemException The exception thrown during renaming.
	 */
	void renameNode(IFileSystem service) throws TCFFileSystemException {
		String src_path = node.getLocation(true);
		String oldName = node.name;
		node.name = newName;
		String dst_path = node.getLocation(true);
		node.name = oldName;
		final TCFFileSystemException[] errors = new TCFFileSystemException[1];
		service.rename(src_path, dst_path, new DoneRename() {
			@Override
			public void doneRename(IToken token, FileSystemException error) {
				if (error != null) {
					String message = NLS.bind(Messages.FSRename_CannotRename, node.name, error);
					errors[0] = new TCFFileSystemException(message, error);
				}
				else {
					final File file = CacheManager.getInstance().getCacheFile(node);
					if (file.exists()) {
						if (node.isFile()) {
							Display display = PlatformUI.getWorkbench().getDisplay();
							display.asyncExec(new Runnable(){
								@Override
					            public void run() {
									closeEditor(file);
								}
							});
							PersistenceManager.getInstance().removeBaseTimestamp(node.getLocationURL());
						}
						file.delete();
					}
					node.setName(newName);
				}
			}
		});
		if (errors[0] != null) {
			throw errors[0];
		}
	}
}
