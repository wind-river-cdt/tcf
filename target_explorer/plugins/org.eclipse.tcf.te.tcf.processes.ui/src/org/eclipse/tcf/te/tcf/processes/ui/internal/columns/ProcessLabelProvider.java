/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.processes.ui.internal.columns;

import java.io.File;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tcf.te.tcf.processes.core.model.ProcessTreeNode;
import org.eclipse.tcf.te.tcf.processes.ui.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.processes.ui.interfaces.ImageConsts;
import org.eclipse.tcf.te.tcf.processes.ui.nls.Messages;
import org.eclipse.tcf.te.ui.trees.Pending;

/**
 * The label provider for the tree column "name".
 */
public class ProcessLabelProvider extends LabelProvider {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element) {
		if (element instanceof ProcessTreeNode) {
			ProcessTreeNode node = (ProcessTreeNode) element;
			if(node.isRootNode()) {
				return Messages.ProcessLabelProvider_RootNodeLabel;
			}
			String name = node.name;
			if (name == null) name = Messages.ProcessLabelProvider_NullNameNodeLabel; 
			int slash = name.lastIndexOf(File.separator);
			if (slash != -1) name = name.substring(slash + 1);
			return name;
		}
		if (element instanceof Pending) {
			Pending pending = (Pending) element;
			return pending.getText();
		}
		return super.getText(element);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
	 */
	@Override
	public Image getImage(Object element) {
		if (element instanceof ProcessTreeNode) {
			ProcessTreeNode node = (ProcessTreeNode) element;
			if(node.isRootNode()) {
				return UIPlugin.getImage(ImageConsts.OBJ_Process_Root);
			}
			return UIPlugin.getImage(ImageConsts.OBJ_Process);
		}
		if (element instanceof Pending) {
			Pending pending = (Pending) element;
			return pending.getImage();
		}
		return super.getImage(element);
	}
}
