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

import java.util.List;

import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.tcf.te.ui.utils.PropertyChangeProvider;

/**
 * The clip board to which copy or cut files/folders.
 */
public class FSClipboard extends PropertyChangeProvider {
	// The constants to define the current operation type of the clip board.
	public static final int NONE = -1;
	public static final int CUT = 0;
	public static final int COPY = 1;
	// The operation type, CUT, COPY or NONE.
	private int operation;
	// The currently selected files/folders.
	private List<FSTreeNode> files;

	/**
	 * Create a clip board instance.
	 */
	public FSClipboard() {
		operation = NONE;
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
	 * Return the current operation type.
	 * 
	 * @return The operation of the current clip board content.
	 */
	public int getOperation() {
		return operation;
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
		for (FSTreeNode file : files) {
			PropertyChangeEvent event = new PropertyChangeEvent(file, "cut", null, null); //$NON-NLS-1$
			firePropertyChange(event);
		}
	}

	/**
	 * Copy the specified files/folders to the clip board.
	 * 
	 * @param files The file/folder nodes.
	 */
	public void copyFiles(List<FSTreeNode> files) {
		operation = COPY;
		this.files = files;
		for (FSTreeNode file : files) {
			PropertyChangeEvent event = new PropertyChangeEvent(file, "copy", null, null); //$NON-NLS-1$
			firePropertyChange(event);
		}
	}

	/**
	 * Clear the clip board.
	 */
	public void clear() {
		operation = NONE;
		List<FSTreeNode> nodes = files;
		this.files = null;
		for (FSTreeNode node : nodes) {
			PropertyChangeEvent event = new PropertyChangeEvent(node, "clear", null, null); //$NON-NLS-1$
			firePropertyChange(event);
		}
	}
}
