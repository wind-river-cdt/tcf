/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * William Chen (Wind River) - [361324] Add more file operations in the file 
 * 												system of Target Explorer.
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.internal.handlers;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.tcf.te.tcf.filesystem.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.filesystem.internal.ImageConsts;
import org.eclipse.tcf.te.ui.jface.images.AbstractImageDescriptor;

/**
 * Cut image descriptor implementation.
 */
public class CutImageDescriptor extends AbstractImageDescriptor {
	// The key to store the cut mask image.
	private static final String ID_FS_NODE_CUT_MASK = "FS_NODE_CUT_MASK@"; //$NON-NLS-1$
	// The key to store the cut decoration image.
	private static final String ID_FS_NODE_CUT = "FS_NODE_CUT@"; //$NON-NLS-1$
	// the base image to decorate with overlays
	private Image baseImage;

	/**
	 * Constructor.
	 */
	public CutImageDescriptor(final Image baseImage) {
		super(UIPlugin.getDefault().getImageRegistry());
		this.baseImage = baseImage;
		// build up the key for the image registry
		String key = ID_FS_NODE_CUT + baseImage.hashCode();
		setDecriptorKey(key);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.resource.CompositeImageDescriptor#drawCompositeImage(int, int)
	 */
	@Override
	protected void drawCompositeImage(int width, int height) {
		drawCentered(baseImage, width, height);
		drawCentered(getMaskImage(), width, height);
	}

	/**
	 * Get the mask image of the base image. The mask image is an image which
	 * has the data of the decorator image and the transparent mask of the 
	 * base image. The decorator image (the key of which is CUT_DECORATOR_IMAGE)
	 * is a translucent white board, which will be drawn over the base image and
	 * make the base image sightly lighter. Try to the cut a file in a file explorer
	 * on Windows host, you'll see its icon is changed to a lighter version. The
	 * mask image created by this method will be drawn over the base image and
	 * generate the similar effect.
	 *  
	 * @return The mask image used to decorate the base image.
	 */
	private Image getMaskImage() {
	    String maskKey = ID_FS_NODE_CUT_MASK + baseImage.hashCode();
		Image maskImage = UIPlugin.getImage(maskKey);
		if (maskImage == null) {
			Image cutImage = UIPlugin.getImage(ImageConsts.CUT_DECORATOR_IMAGE);
			ImageData cutSrcData = cutImage.getImageData();
			ImageData baseData = baseImage.getImageData();
			// Get the base image's transparency mask.
			ImageData transparencyMask = baseData.getTransparencyMask();
			// Mask the decorator image.
			maskImage = new Image(baseImage.getDevice(), cutSrcData, transparencyMask);
			UIPlugin.getDefault().getImageRegistry().put(maskKey, maskImage);
		}
	    return maskImage;
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.resource.CompositeImageDescriptor#getSize()
	 */
	@Override
	protected Point getSize() {
		return new Point(baseImage.getImageData().width, baseImage.getImageData().height);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.ide.util.ui.AbstractImageDescriptor#getBaseImage()
	 */
	@Override
	protected Image getBaseImage() {
		return baseImage;
	}
}
