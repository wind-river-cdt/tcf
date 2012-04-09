/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.ui.internal.handlers;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tcf.te.tcf.filesystem.core.interfaces.IConfirmCallback;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.filesystem.ui.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.filesystem.ui.internal.ImageConsts;
import org.eclipse.tcf.te.tcf.filesystem.ui.nls.Messages;
import org.eclipse.ui.PlatformUI;

public class MoveCopyCallback implements IConfirmCallback {

	@Override
	public boolean requires(Object object) {
		return true;
	}

	@Override
	public int confirms(Object object) {
		final FSTreeNode node = (FSTreeNode) object;
		final int[] results = new int[1];
		Display display = PlatformUI.getWorkbench().getDisplay();
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				Shell parent = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				String title = node.isDirectory() ? Messages.FSOperation_ConfirmFolderReplaceTitle : Messages.FSOperation_ConfirmFileReplace;
				String message = NLS.bind(node.isDirectory() ? Messages.FSOperation_ConfirmFolderReplaceMessage : Messages.FSOperation_ConfirmFileReplaceMessage, node.name);
				final Image titleImage = UIPlugin.getImage(ImageConsts.REPLACE_FOLDER_CONFIRM);
				MessageDialog qDialog = new MessageDialog(parent, title, null, message, MessageDialog.QUESTION, new String[] { Messages.FSOperation_ConfirmDialogYes, Messages.FSOperation_ConfirmDialogYesToAll, Messages.FSOperation_ConfirmDialogNo, Messages.FSOperation_ConfirmDialogCancel }, 0) {
					@Override
					public Image getQuestionImage() {
						return titleImage;
					}
				};
				results[0] = qDialog.open();
			}
		});
		return results[0];
	}

}
