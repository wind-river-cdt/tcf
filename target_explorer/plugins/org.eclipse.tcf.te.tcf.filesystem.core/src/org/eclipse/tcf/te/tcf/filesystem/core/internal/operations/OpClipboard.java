/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.core.internal.operations;

import java.beans.PropertyChangeEvent;
import java.util.List;

import org.eclipse.tcf.te.core.utils.PropertyChangeProvider;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;

/**
 * The clip board to which copy or cut files/folders.
 */
public class OpClipboard extends PropertyChangeProvider {
	// The constants to define the current operation type of the clip board.
	private static final int NONE = -1;
	private static final int CUT = 0;
	private static final int COPY = 1;
	// The operation type, CUT, COPY or NONE.
	private int operation;
	// The currently selected files/folders.
	private List<FSTreeNode> files;

	/**
	 * Create a clip board instance.
	 */
	public OpClipboard() {
		operation = NONE;
	}
	
	/**
	 * If the current operation is a cut operation.
	 * 
	 * @return true if it is.
	 */
	public boolean isCutOp() {
		return operation == CUT;
	}
	
	/**
	 * If the current operation is a copy operation.
	 * 
	 * @return true if it is.
	 */
	public boolean isCopyOp() {
		return operation == COPY;
	}

	/**
	 * If the clip board is empty.
	 * 
	 * @return true if the operation is NONE and no files are selected.
	 */
	public boolean isEmpty() {
		return operation == NONE && (files == null || files.isEmpty());
	}

	/**
	 * Get the currently selected files/folders to operated.
	 * 
	 * @return The file/folder list using their location URLs.
	 */
	public List<FSTreeNode> getFiles() {
		return files;
	}

	/**
	 * Cut the specified files/folders to the clip board.
	 * 
	 * @param files The file/folder nodes.
	 */
	public void cutFiles(List<FSTreeNode> files) {
		operation = CUT;
		this.files = files;
		PropertyChangeEvent event = new PropertyChangeEvent(this, "cut", null, null); //$NON-NLS-1$
		firePropertyChange(event);
	}

	/**
	 * Copy the specified files/folders to the clip board.
	 * 
	 * @param files The file/folder nodes.
	 */
	public void copyFiles(List<FSTreeNode> files) {
		operation = COPY;
		this.files = files;
		PropertyChangeEvent event = new PropertyChangeEvent(this, "copy", null, null); //$NON-NLS-1$
		firePropertyChange(event);
	}

	/**
	 * Clear the clip board.
	 */
	public void clear() {
		operation = NONE;
		this.files = null;
		PropertyChangeEvent event = new PropertyChangeEvent(this, "clear", null, null); //$NON-NLS-1$
		firePropertyChange(event);
	}
	
	/**
	 * Dispose the clipboard.
	 */
	public void dispose() {
	}
}
