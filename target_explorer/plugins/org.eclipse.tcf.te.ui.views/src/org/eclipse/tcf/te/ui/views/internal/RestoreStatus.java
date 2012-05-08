/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.views.internal;

import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.tcf.te.ui.views.activator.UIPlugin;

/**
 * A status used to return the expanded paths after the
 * restoring job is done.
 */
public class RestoreStatus extends Status {
	// The expanded paths to be restored.
	private List<TreePath> paths;
	
	/**
	 * Create an restore status with IStatus.OK and 
	 * the expanded paths.
	 * 
	 * @param paths The expanded paths.
	 */
	public RestoreStatus(List<TreePath> paths) {
        super(IStatus.OK, UIPlugin.getUniqueIdentifier(), null);
        this.paths = paths;
    }
	
	/**
	 * Get the expanded paths array.
	 * 
	 * @return The expanded paths array.
	 */
	public TreePath[] getExpandedPaths() {
		return paths.toArray(new TreePath[paths.size()]);
	}
}