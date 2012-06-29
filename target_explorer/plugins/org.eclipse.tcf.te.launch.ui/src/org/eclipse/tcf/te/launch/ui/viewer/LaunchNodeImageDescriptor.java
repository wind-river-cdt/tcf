/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.tcf.te.launch.ui.viewer;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.tcf.te.launch.ui.internal.ImageConsts;
import org.eclipse.tcf.te.launch.ui.model.LaunchNode;
import org.eclipse.tcf.te.ui.jface.images.AbstractImageDescriptor;

/**
 * Launch node image descriptor implementation.
 */
public class LaunchNodeImageDescriptor extends AbstractImageDescriptor {
	// the base image to decorate with overlays
	private Image baseImage;
	// the image size
	private Point imageSize;

	// Flags representing the object states to decorate
	private boolean isValid;

	/**
	 * Constructor.
	 */
	public LaunchNodeImageDescriptor(final ImageRegistry registry, final Image baseImage, final LaunchNode node) {
		super(registry);

		this.baseImage = baseImage;
		imageSize = new Point(baseImage.getImageData().width, baseImage.getImageData().height);

		// Determine the current object state to decorate
		initialize(node);

		// build up the key for the image registry
		defineKey(baseImage.hashCode());
	}

	/**
	 * Initialize the image descriptor from the launch node.
	 *
	 * @param node The launch node. Must not be <code>null</code>.
	 */
	protected void initialize(LaunchNode node) {
		Assert.isNotNull(node);

		isValid = node.isValidFor(null);
	}

	protected void defineKey(int hashCode) {
		String key = "LaunchNodeID:" +  //$NON-NLS-1$
						hashCode + ":" + //$NON-NLS-1$
						isValid;

		setDecriptorKey(key);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.resource.CompositeImageDescriptor#drawCompositeImage(int, int)
	 */
	@Override
	protected void drawCompositeImage(int width, int height) {
		drawCentered(baseImage, width, height);

		if (!isValid) {
			drawBottomRight(ImageConsts.RED_X_OVR);
		}

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.resource.CompositeImageDescriptor#getSize()
	 */
	@Override
	protected Point getSize() {
		return imageSize;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.ide.util.ui.AbstractImageDescriptor#getBaseImage()
	 */
	@Override
	protected Image getBaseImage() {
		return baseImage;
	}
}
