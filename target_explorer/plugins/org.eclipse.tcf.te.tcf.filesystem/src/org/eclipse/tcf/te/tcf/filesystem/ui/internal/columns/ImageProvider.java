/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.ui.internal.columns;

import org.eclipse.swt.graphics.Image;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;

/**
 * An image provider provides platform specific images for each files/folders.
 * It is used by FSTreeElementLabelProvider to provide the images of a file 
 * node.
 */
public interface ImageProvider {
	/**
	 * Get the image display for the specified file node.
	 * 
	 * @param node The file node.
	 * @return The image that represents the file node.
	 */
	Image getImage(FSTreeNode node);
}
