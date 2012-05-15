/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.ui.internal.columns;

import org.eclipse.swt.graphics.Image;
import org.eclipse.tcf.te.runtime.utils.Host;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;
import org.eclipse.tcf.te.ui.trees.PendingAwareLabelProvider;

/**
 * The label provider for the tree column "name".
 */
public class FSTreeElementLabelProvider extends PendingAwareLabelProvider {
	// The image provider to provide platform specific images.
	private ImageProvider imgProvider;
	
	/**
	 * Constructor.
	 */
	public FSTreeElementLabelProvider() {
		if(Host.isWindowsHost()) {
			imgProvider = new WindowsImageProvider();
		}
		else {
			imgProvider = new DefaultImageProvider();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element) {
		if (element instanceof FSTreeNode) {
			return ((FSTreeNode) element).name;
		}
		return super.getText(element);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
	 */
	@Override
	public Image getImage(Object element) {
		if (element instanceof FSTreeNode) {
			FSTreeNode node = (FSTreeNode) element;
			return imgProvider.getImage(node);
		}
		return super.getImage(element);
	}
}
