/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.internal.columns;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.filechooser.FileSystemView;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tcf.te.tcf.filesystem.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.filesystem.internal.utils.CacheManager;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.tcf.te.ui.interfaces.IViewerInput;

/**
 * The background daemon that updates the images of the file system using
 * images retrieved by FileSystemView from Swing.
 */
public class LabelProviderUpdateDaemon extends Thread {
	// The dummy AWT component used to render the icon.
	Component dummyComponent = new JComponent(){private static final long serialVersionUID = 5926798769323111209L;};
	//The queue that caches the current file nodes to be updated.
	BlockingQueue<FSTreeNode> queueNodes;
	// The image update adapter for a file which has a local cache copy.
	ImageUpdateAdapter cacheAdapter;
	// The image update adapter for a file which does not has a local cache copy.
	ImageUpdateAdapter extAdapter;

	/**
	 * Constructor
	 */
	public LabelProviderUpdateDaemon() {
		super("Image Updater Daemon"); //$NON-NLS-1$
		setDaemon(true);
		this.queueNodes = new LinkedBlockingQueue<FSTreeNode>();
		this.cacheAdapter = new CacheFileImageUpdater();
		this.extAdapter = new FileExtBasedImageUpdater(this);
	}

	/**
	 * Cache the node which is to be updated with its icon in the file tree.
	 * 
	 * @param node The node to be enqueued
	 */
	public void enqueue(final FSTreeNode node) {
		SafeRunner.run(new ISafeRunnable() {
			@Override
			public void handleException(Throwable exception) {
				// Ignore
			}

			@Override
			public void run() throws Exception {
				queueNodes.put(node);
			}
		});
	}

	/**
	 * Take next node to be processed.
	 * 
	 * @return The next node.
	 */
	private FSTreeNode take() {
		while (true) {
			try {
				return queueNodes.take();
			}
			catch (InterruptedException e) {
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		while (true) {
			FSTreeNode node = take();
			ImageUpdateAdapter adapter = getUpdateAdapter(node);
			String imgKey = adapter.getImageKey(node);
			ImageDescriptor image = UIPlugin.getImageDescriptor(imgKey);
			if (image == null) {
				File mrrFile = adapter.getMirrorFile(node);
				File imgFile = adapter.getImageFile(node);
				image = createImage(imgKey, mrrFile, imgFile);
			}
			if (image != null) {
				sendNotification(node, node.name, null, image);
			}
		}
	}
	
	/**
	 * Select an image update adapter for the specified node.
	 * 
	 * @param node The FSTreeNode.
	 * @return an image update adapter, either cache based or extension based.
	 */
	private ImageUpdateAdapter getUpdateAdapter(FSTreeNode node) {
		File cacheFile = CacheManager.getInstance().getCacheFile(node);
		if (cacheFile.exists()) {
			return cacheAdapter;
		}
		return extAdapter;
	}

	/**
	 * Get the image for the specified node from its
	 * image update adapter.
	 * 
	 * @param node The file system tree node.
	 * @return The image or null if there's no image yet.
	 */
	public Image getImage(FSTreeNode node) {
		ImageUpdateAdapter adapter = getUpdateAdapter(node);
		String key = adapter.getImageKey(node);
		return UIPlugin.getImage(key);
	}

	/**
	 * Create an Image Descriptor based on the mirror file and store
	 * it in the imgFile and store it using the specified image key.
	 * 
	 * @param imgKey The image key.
	 * @param mrrFile The mirror file used to create the image.
	 * @param imgFile The image file used to store the image data.
	 * @return The Image Descriptor describing the image or null if it is not successful.
	 */
	private ImageDescriptor createImage(String imgKey, File mrrFile, File imgFile) {
	    ImageDescriptor image = UIPlugin.getImageDescriptor(imgKey);
	    if (image == null) {
	    	if (!imgFile.exists()) {
	    		FileSystemView view = FileSystemView.getFileSystemView();
	    		Icon icon = view.getSystemIcon(mrrFile);
				if (icon != null) createImageFromIcon(icon, imgFile);
	    	}
			if (imgFile.exists()) {
				try {
					image = ImageDescriptor.createFromURL(imgFile.toURI().toURL());
					UIPlugin.getDefault().getImageRegistry().put(imgKey, image);
				}
				catch (MalformedURLException e) {
					// Ignore
				}
			}
	    }
	    return image;
    }
	
	/**
	 * Get the image of disk drivers on Windows platform.
	 * 
	 * @return The disk driver image.
	 */
	public Image getDiskImage() {
		String key = "SWING_ROOT_DRIVER_IMAGE"; //$NON-NLS-1$
        ImageDescriptor imgDesc = UIPlugin.getImageDescriptor(key);
        if (imgDesc == null) {
        	File[] roots = File.listRoots();
        	File mirror;
			if (roots.length > 1) mirror = roots[1];
			else mirror = roots[0];
        	File imgFile = getTempImg("_disk_drive_"); //$NON-NLS-1$
        	createImage(key, mirror, imgFile);
        }
        return UIPlugin.getImage(key);
	}
	
	/**
	 * Get the folder image on Windows platform.
	 * 
	 * @return The folder image.
	 */
	public Image getFolderImage() {
		String key = "SWING_FOLDER_IMAGE"; //$NON-NLS-1$
		ImageDescriptor imgDesc = UIPlugin.getImageDescriptor(key);
		if (imgDesc == null) {
			String dir = System.getProperty("work.dir"); //$NON-NLS-1$
			if (dir == null) dir = System.getProperty("java.home"); //$NON-NLS-1$
			File mirror = null;
			if (dir != null) mirror = new File(dir);
			else mirror = new File("."); //$NON-NLS-1$
			File imgFile = getTempImg("_directory_"); //$NON-NLS-1$
			createImage(key, mirror, imgFile);
		}
		return UIPlugin.getImage(key);
	}
	
	/**
	 * Get the temporary directory store the images and temporary mirror files.
	 * @return
	 */
	protected File getTempDir() {
		File cacheRoot = CacheManager.getInstance().getCacheRoot();
		File tempDir = new File(cacheRoot, ".tmp"); //$NON-NLS-1$
		if (!tempDir.exists() && !tempDir.mkdirs()) {
			tempDir = cacheRoot;
		}
		return tempDir;
	}
	
	/**
	 * Get the an image file named "imgName" in the temporary image
	 * directory.
	 * 
	 * @param imgName The image's file name.
	 * @return The file object of this image file.
	 */
	protected File getTempImg(String imgName) {
		File tempDir = getTempDir();
		File imgDir = new File(tempDir, ".img"); //$NON-NLS-1$
		if (!imgDir.exists() && !imgDir.mkdirs()) {
			imgDir = tempDir;
		}
		return new File(imgDir, imgName + ".png"); //$NON-NLS-1$
	}

	/**
	 * Create an image file using "png" format 
	 * for the specified temporary file.
	 * 
	 * @param icon The icon that is used for the temporary file.
	 * @param tmpfile The temporary file.
	 */
	private void createImageFromIcon(Icon icon, File imgFile) {
		BufferedImage bi = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_4BYTE_ABGR);
		Graphics g = bi.createGraphics();
		icon.paintIcon(dummyComponent, g, 0, 0);
		g.dispose();
		try {
			ImageIO.write(bi, "png", imgFile); //$NON-NLS-1$
		}
		catch (IOException e) {
		}
	}

	/**
	 * Send a notification to inform the file tree for changed images.
	 * 
	 * @param node The node whose image has changed.
	 * @param key The key used to store the images.
	 * @param oldImg The old image descriptor.
	 * @param newImg The new image descriptor.
	 */
	private void sendNotification(FSTreeNode node, String key, ImageDescriptor oldImg, ImageDescriptor newImg) {
		if (node.peerNode != null) {
			IViewerInput viewerInput = (IViewerInput) node.peerNode.getAdapter(IViewerInput.class);
			viewerInput.firePropertyChange(new PropertyChangeEvent(node, key, oldImg, newImg));
		}
	}
}
