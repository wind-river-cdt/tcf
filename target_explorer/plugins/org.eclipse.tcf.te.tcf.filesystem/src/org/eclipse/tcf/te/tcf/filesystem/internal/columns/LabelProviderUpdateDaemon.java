/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.internal.columns;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.tcf.te.tcf.filesystem.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.filesystem.internal.utils.ContentTypeHelper;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.tcf.te.ui.interfaces.IViewerInput;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

public class LabelProviderUpdateDaemon extends Thread {
	IEditorRegistry editorRegistry;
	BlockingQueue<FSTreeNode> queueNodes;
	public LabelProviderUpdateDaemon() {
		super("Label Provider Updater"); //$NON-NLS-1$
		setDaemon(true);
		IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench != null) editorRegistry = workbench.getEditorRegistry();
		this.queueNodes = new LinkedBlockingQueue<FSTreeNode>();
	}
	
	public void enqueue(final FSTreeNode node) {
		SafeRunner.run(new ISafeRunnable(){
			@Override
            public void handleException(Throwable exception) {
				// Ignore
            }

			@Override
            public void run() throws Exception {
				queueNodes.put(node);
            }});
	}
	
	private FSTreeNode take() {
		while (true) {
			try {
				return queueNodes.take();
			}
			catch (InterruptedException e) {
			}
		}
	}
	
	@Override
	public void run() {
		while(true) {
			FSTreeNode node = take();
			IContentType contentType = ContentTypeHelper.getInstance().getContentType(node);
			if(contentType != null) {
				String key = node.name;
				ImageDescriptor descriptor = editorRegistry.getImageDescriptor(key, contentType);
				if(descriptor == null) {
					descriptor = editorRegistry.getImageDescriptor(null, contentType);
				}
				if (descriptor != null) {
					ImageDescriptor old = UIPlugin.getDefault().getImageRegistry().getDescriptor(key);
					if(old != null) {
						UIPlugin.getDefault().getImageRegistry().remove(key);
					}
					UIPlugin.getDefault().getImageRegistry().put(key, descriptor);
					sendNotification(node, key, old, descriptor);
				}
			}
		}
	}

	private void sendNotification(FSTreeNode node, String key, ImageDescriptor oldImg, ImageDescriptor newImg) {
		if (node.peerNode != null) {
			IViewerInput viewerInput = (IViewerInput) node.peerNode.getAdapter(IViewerInput.class);
			viewerInput.firePropertyChange(new PropertyChangeEvent(this, key, oldImg, newImg));
		}
    }
}
