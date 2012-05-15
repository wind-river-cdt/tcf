/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.ui.internal.decorators;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.tcf.te.tcf.filesystem.ui.activator.UIPlugin;
import org.eclipse.tcf.te.ui.jface.images.AbstractImageDescriptor;

/**
 * The descriptor for a phantom-like image.
 */
public class PhantomImageDescriptor extends AbstractImageDescriptor {
	// The alpha data when highlight the base image.
	private static final int HIGHLIGHT_ALPHA = 127;
	// The key to store the cut mask image.
	private static final String ID_FS_NODE_CUT_MASK = "FS_NODE_CUT_MASK@"; //$NON-NLS-1$
	// The key to store the cut decoration image.
	private static final String ID_FS_NODE_CUT = "FS_NODE_CUT@"; //$NON-NLS-1$
	// the base image to decorate with overlays
	private Image baseImage;

	/**
	 * Constructor.
	 */
	public PhantomImageDescriptor(final Image baseImage) {
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
			ImageData baseData = baseImage.getImageData();
			PaletteData palette = new PaletteData(new RGB[]{new RGB(255, 255, 255), new RGB(0,0,0)});
			ImageData imageData = new ImageData(baseData.width, baseData.height, 1, palette);
			// Get the base image's transparency mask.
			imageData.alphaData = createAlphaData();
			maskImage = new Image(baseImage.getDevice(), imageData);
			UIPlugin.getDefault().getImageRegistry().put(maskKey, maskImage);
		}
	    return maskImage;
    }

	/**
	 * Create the alpha data that will be used in the mask image data.
	 * 
	 * @return The alpha data.
	 */
	private byte[] createAlphaData() {
		ImageData imageData = baseImage.getImageData();
		if (imageData.maskData != null) {
			if (imageData.depth == 32) {
				return maskAlpha32();
			}
			return maskAlpha();
		}
		return nonMaskAlpha();
	}

	/**
	 * Create the alpha data for the base image that has no mask data.
	 * 
	 * @return The alpha data.
	 */
	private byte[] nonMaskAlpha() {
		ImageData imageData = baseImage.getImageData();
		Assert.isTrue(imageData.maskData == null);

		byte[] alphaData = new byte[imageData.width * imageData.height];
		int i = 0;
		for (int y = 0; y < imageData.height; y++) {
			for (int x = 0; x < imageData.width; x++) {
				int pixel = imageData.getPixel(x, y);
				int alpha = 255;
				if (imageData.transparentPixel != -1 && imageData.transparentPixel == pixel) {
					// If it has a transparent pixel and the current pixel is the transparent.
					alpha = 0;
				}
				else if (imageData.alpha != -1) {
					// If it has a global alpha value.
					alpha = imageData.alpha;
				}
				else if (imageData.alphaData != null) {
					// If it has alpha data.
					alpha = imageData.getAlpha(x, y);
				}
				alphaData[i++] = (byte) (alpha * HIGHLIGHT_ALPHA / 255);
			}
		}
		return alphaData;
	}

	/**
	 * Create the alpha data for the base image that has mask data, and the color depth is not of
	 * 32-bit.
	 * 
	 * @return The alpha data
	 */
	private byte[] maskAlpha() {
		ImageData imageData = baseImage.getImageData();
		Assert.isTrue(imageData.maskData != null && imageData.depth != 32);

		ImageData mask = imageData.getTransparencyMask();
		// Get the black index.
		int blackIndex = getBlackIndex(mask);
		byte[] alphaData = new byte[imageData.width * imageData.height];
		int i = 0;
		for (int y = 0; y < imageData.height; y++) {
			for (int x = 0; x < imageData.width; x++) {
				int alpha = mask.getPixel(x, y) == blackIndex ? 0 : 255;
				alphaData[i++] = (byte) (alpha * HIGHLIGHT_ALPHA / 255);
			}
		}
		return alphaData;
	}

	/**
	 * Create the alpha data for the base image that has mask data and the color depth is of 32-bit.
	 * 
	 * @return The alpha data.
	 */
	private byte[] maskAlpha32() {
		ImageData imageData = baseImage.getImageData();
		Assert.isTrue(imageData.maskData != null && imageData.depth == 32);

		ImageData mask = imageData.getTransparencyMask();
		// Get the black index.
		int blackIndex = getBlackIndex(mask);
		// Calculate the alpha mask and the alpha shift.
		int alphaMask = ~(imageData.palette.redMask | imageData.palette.greenMask | imageData.palette.blueMask);
		int alphaShift = 0;
		while (alphaMask != 0 && ((alphaMask >>> alphaShift) & 1) == 0)
			alphaShift++;
		byte[] alphaData = new byte[imageData.width * imageData.height];
		int i = 0;
		for (int y = 0; y < imageData.height; y++) {
			for (int x = 0; x < imageData.width; x++) {
				int pixel = imageData.getPixel(x, y);
				int alpha = (pixel & alphaMask) >>> alphaShift;
				if (alpha <= 0 || alpha > 255) {
					// If the alpha value is illegal, try to get it from the mask data.
					alpha = mask.getPixel(x, y) == blackIndex ? 0 : 255;
				}
				alphaData[i++] = (byte) (alpha * HIGHLIGHT_ALPHA / 255);
			}
		}
		return alphaData;
	}

    /**
     * Get the black index from the palette of the mask data.
     * 
     * @param mask
     * @return
     */
	private int getBlackIndex(ImageData mask) {
		RGB[] rgbs = mask.getRGBs();
		if (rgbs != null) {
			for (int i = 0; i < rgbs.length; i++) {
				RGB rgb = rgbs[i];
				if (rgb.red == 0 && rgb.green == 0 && rgb.blue == 0) {
					return i;
				}
			}
		}
		return 0;
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