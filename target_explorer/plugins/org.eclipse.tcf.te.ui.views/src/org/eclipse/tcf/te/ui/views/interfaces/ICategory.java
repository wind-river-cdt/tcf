/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.views.interfaces;

import org.eclipse.swt.graphics.Image;
import org.eclipse.tcf.te.runtime.interfaces.extensions.IExecutableExtension;

/**
 * Main view category node.
 */
public interface ICategory extends IExecutableExtension {

	/**
	 * Returns the category image.
	 *
	 * @return The category image or <code>null</code>.
	 */
	public Image getImage();
}