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

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tcf.te.tcf.processes.ui.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.processes.ui.interfaces.ImageConsts;
import org.eclipse.tcf.te.tcf.processes.ui.model.ProcessTreeNode;
import org.eclipse.tcf.te.tcf.processes.ui.nls.Messages;

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
			if(node.type.equals("ProcRootNode")) { //$NON-NLS-1$
				return Messages.ProcessLabelProvider_RootNodeLabel;
			}
			String name = node.name;
			if (name == null) name = Messages.ProcessLabelProvider_NullNameNodeLabel; 
			return name;
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
			if(node.type.equals("ProcRootNode")) { //$NON-NLS-1$
				return UIPlugin.getImage(ImageConsts.OBJ_Process_Root);
			}
			return UIPlugin.getImage(ImageConsts.OBJ_Process);
		}
		return super.getImage(element);
	}
}
