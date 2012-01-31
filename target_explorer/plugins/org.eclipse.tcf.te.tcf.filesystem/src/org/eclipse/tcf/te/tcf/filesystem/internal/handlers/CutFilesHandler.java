/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.internal.handlers;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.tcf.te.tcf.filesystem.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSClipboard;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * The handler that cuts the selected files or folders to the clip board.
 */
public class CutFilesHandler extends AbstractHandler {
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		FSClipboard cb = UIPlugin.getDefault().getClipboard();
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		if (!selection.isEmpty()) {
			List<FSTreeNode> nodes = selection.toList();
			// Cut these files to the clip board.
			cb.cutFiles(nodes);
		}
		return null;
	}
}
