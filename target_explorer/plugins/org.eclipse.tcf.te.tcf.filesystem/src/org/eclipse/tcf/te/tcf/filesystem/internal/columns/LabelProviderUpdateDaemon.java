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

	/**
	 * Constructor
	 */
	public LabelProviderUpdateDaemon() {
		super("Label Provider Updater"); //$NON-NLS-1$
		setDaemon(true);
		this.queueNodes = new LinkedBlockingQueue<FSTreeNode>();
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
			String key = getExtImageKey(node);
			ImageDescriptor image = UIPlugin.getImageDescriptor(key);
			if (image == null) {
				String ext = getFileExt(node);
				File file = getTempFile(ext);
				File imgFile = getImgFile(file.getName());
				if (!imgFile.exists()) {
					FileSystemView view = FileSystemView.getFileSystemView();
					Icon icon = view.getSystemIcon(file);
					createImageFromIcon(icon, file);
				}
				try {
					image = ImageDescriptor.createFromURL(imgFile.toURI().toURL());
					UIPlugin.getDefault().getImageRegistry().put(key, image);
				}
				catch (MalformedURLException e) {
					// Ignore
				}
			}
			sendNotification(node, node.name, null, image);
		}
	}
	
	/**
	 * Get the node from the extension registry.
	 * 
	 * @param node The node for which the image is retrieved for.
	 * @return The image for this node.
	 */
	public Image getImage(FSTreeNode node) {
		String key = getExtImageKey(node);
		return UIPlugin.getImage(key);
	}

	/**
	 * Get an extension key as the image registry key for the 
	 * specified node.
	 * 
	 * @param node The node to get the key for.
	 * @return The key used to cache the image descriptor in the registry.
	 */
	private String getExtImageKey(FSTreeNode node) {
	    String ext = getFileExt(node);
		String key = ext == null ? "" : ext; //$NON-NLS-1$
		key = "EXT_IMAGE@"+key; //$NON-NLS-1$
	    return key;
    }

	/**
	 * Get the node's file extension or null
	 * if there is no extension.
	 * 
	 * @param node The file tree node.
	 * @return The file's extension or null.
	 */
	private String getFileExt(FSTreeNode node) {
	    String name = node.name;
		String ext = null;
		int index = name.lastIndexOf("."); //$NON-NLS-1$
		if (index != -1) ext = name.substring(index + 1);
	    return ext;
    }

	/**
	 * Create an image file using "png" format 
	 * for the specified temp file.
	 * 
	 * @param icon The icon that is used for the tmp file.
	 * @param tmpfile The temp file.
	 */
	private void createImageFromIcon(Icon icon, File tmpfile) {
		BufferedImage bi = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_4BYTE_ABGR);
		Graphics g = bi.createGraphics();
		icon.paintIcon(dummyComponent, g, 0, 0);
		g.dispose();
		File imgFile = getImgFile(tmpfile.getName());
		try {
			ImageIO.write(bi, "png", imgFile); //$NON-NLS-1$
		}
		catch (IOException e) {
		}
	}

	/**
	 * Get the image file object for the specified temp file name.
	 * 
	 * @param tmpName The temp file name.
	 * @return The file object.
	 */
	private File getImgFile(String tmpName) {
		File imgFolder = getImgDir();
		return new File(imgFolder, tmpName + ".png"); //$NON-NLS-1$
	}

	/**
	 * Get the temporary file used to obtain icons. If it does
	 * not exist, then create one.
	 * 
	 * @param ext The extension of the temporary file.
	 * @return The file object.
	 */
	private File getTempFile(String ext) {
		File tmpDir = getTempDir();
		File file = new File(tmpDir, "temp" + (ext == null ? "" : ("." + ext))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (!file.exists()) {
			try {
				file.createNewFile();
			}
			catch (IOException e) {
			}
		}
		return file;
	}

	/**
	 * Get the temporary directory used to store temporary file and images.
	 * 
	 * @return The directory file object.
	 */
	private File getTempDir() {
		File file = CacheManager.getInstance().getCacheRoot();
		file = new File(file, ".tmp"); //$NON-NLS-1$
		if (!file.exists()) file.mkdirs();
		return file;
	}

	/**
	 * Get the image's directory to hold the temporary images.
	 * 
	 * @return The directory
	 */
	private File getImgDir() {
		File file = CacheManager.getInstance().getCacheRoot();
		file = new File(file, ".img"); //$NON-NLS-1$
		if (!file.exists()) file.mkdirs();
		return file;
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
