/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.trees;

import java.io.InputStream;
import java.net.URL;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tcf.te.ui.activator.UIPlugin;
import org.eclipse.tcf.te.ui.interfaces.ImageConsts;
import org.eclipse.tcf.te.ui.nls.Messages;

/**
 * The pending node used in various tree viewer. It can start an animation that
 * displays an animated GIF image read from "pending.gif".
 */
public class Pending {
	// The pending images used to display the animation.
	static Image[] pendingImages;
	// The interval between two frames.
	private static final int FRAME_INTERVAL = 100;
	// Reference to the parent tree viewer
	TreeViewer viewer;
	// The display used to create image and timer.
	Display display;
	// If it is animating.
	boolean animating;
	// The current frame index of the image list.
	int frame;
	
	/**
	 * Create a pending node for the specified tree viewer.
	 * 
	 * @param viewer The tree viewer in which the pending node is added to.
	 */
	public Pending(TreeViewer viewer) {
		this.viewer = viewer;
		this.display = viewer.getTree().getDisplay();
		this.animating = true;
		this.frame = 0;
		if(pendingImages == null) {
			loadPendingImages(display);
		}
	}

	/**
	 * Load the pending images used to animate.
	 */
	private static void loadPendingImages(final Display display) {
		SafeRunner.run(new ISafeRunnable() {
			@Override
			public void handleException(Throwable exception) {
				// Ignore it.
			}

			@Override
			public void run() throws Exception {
				InputStream is = null;
				try {
					URL url = UIPlugin.getDefault().getBundle().getEntry(ImageConsts.IMAGE_DIR_ROOT + ImageConsts.IMAGE_DIR_ELCL + "pending.gif"); //$NON-NLS-1$
					if (url != null) {
						is = url.openStream();
						ImageData[] imageDatas = new ImageLoader().load(is);
						pendingImages = new Image[imageDatas.length];
						for (int i = 0; i < imageDatas.length; i++) {
							pendingImages[i] = new Image(display, imageDatas[i]);
						}
					}
				}
				finally {
					if (is != null) {
						try { is.close(); } catch (Exception e) {}
					}
				}
			}
		});
	}
	
	/**
	 * Animate the pending images. Start a SWT timer to update
	 * the pending image periodically.
	 */
	public void startAnimation() {
		if (Display.getCurrent() == null) {
			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					startAnimation();
				}
			});
		}
		else {
			display.timerExec(FRAME_INTERVAL, new Runnable() {
				@Override
				public void run() {
					viewer.update(Pending.this, null);
					if (animating) startAnimation();
				}
			});
		}
	}
	
	/**
	 * Get the label for this pending node.
	 * 
	 * @return The label of this pending node.
	 */
	public String getText() {
		return Messages.Pending_Label; 
	}

	/**
	 * Get the current image in the pending image list.
	 * 
	 * @return The current image.
	 */
	public Image getImage() {
		Image img = null;
		if (pendingImages != null && pendingImages.length > 0) {
			img = pendingImages[frame++];
			frame = frame % pendingImages.length;
		}
		return img;
	}
	
	/**
	 * Dispose the pending images used.
	 */
	public static void dispose() {
		if (pendingImages != null && pendingImages.length > 0) {
			for (Image img : pendingImages) {
				img.dispose();
			}
		}
		pendingImages = null;
	}

	/**
	 * Stop the animation.
	 */
	public void stopAnimation() {
		animating = false;
	}
}
