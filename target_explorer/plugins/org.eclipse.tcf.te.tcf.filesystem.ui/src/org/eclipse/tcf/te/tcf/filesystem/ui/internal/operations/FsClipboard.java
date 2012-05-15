/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.ui.internal.operations;

import java.util.List;

import org.eclipse.swt.SWTException;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.OpClipboard;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;
import org.eclipse.ui.PlatformUI;

/**
 * The clip board to which copy or cut files/folders.
 */
public class FsClipboard extends OpClipboard {
	// The system clipboard.
	private Clipboard clipboard;

	/**
	 * Create a clip board instance.
	 */
	public FsClipboard() {
		super();
		clipboard = new Clipboard(PlatformUI.getWorkbench().getDisplay());
	}

	/**
	 * Cut the specified files/folders to the clip board.
	 * 
	 * @param files The file/folder nodes.
	 */
	@Override
    public void cutFiles(List<FSTreeNode> files) {
		super.cutFiles(files);
		clearSystemClipboard();
	}

	/**
	 * Copy the specified files/folders to the clip board.
	 * 
	 * @param files The file/folder nodes.
	 */
	@Override
    public void copyFiles(List<FSTreeNode> files) {
		super.copyFiles(files);
		clearSystemClipboard();
	}

	/**
	 * Clear the clip board.
	 */
	@Override
    public void clear() {
		super.clear();
		clearSystemClipboard();
	}
	
	/**
	 * Make sure the system clip board is cleared in a UI thread.
	 */
	void clearSystemClipboard() {
		if (Display.getCurrent() != null) {
			clipboard.clearContents();
		}
		else {
			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable(){
				@Override
                public void run() {
					clearSystemClipboard();
                }});
		}
	}
	
	/**
	 * Dispose the clipboard.
	 */
	@Override
    public void dispose() {
		if(Display.getCurrent() != null) {
			if (!clipboard.isDisposed()) {
				try {
					clipboard.dispose();
				}
				catch (SWTException e) {
				}
			}
		}
		else {
			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable(){
				@Override
                public void run() {
					dispose();
                }});
		}
	}

	/**
	 * Get the system clipboard.
	 * 
	 * @return The system clipboard.
	 */
	public Clipboard getSystemClipboard() {
		return clipboard;
	}
}
