/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipe.tcf.te.launch.ui.activator;

import java.io.File;
import java.io.IOException;

import org.eclipse.cdt.utils.elf.Elf;
import org.eclipse.cdt.utils.elf.Elf.ELFhdr;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tcf.te.runtime.tracing.TraceHandler;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class UIPlugin extends AbstractUIPlugin {
	// The shared instance
	private static UIPlugin plugin;
	// The trace handler instance
	private static TraceHandler traceHandler;

	/**
	 * The constructor
	 */
	public UIPlugin() {
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static UIPlugin getDefault() {
		return plugin;
	}

	/**
	 * Convenience method which returns the unique identifier of this plugin.
	 */
	public static String getUniqueIdentifier() {
		if (getDefault() != null && getDefault().getBundle() != null) {
			return getDefault().getBundle().getSymbolicName();
		}
		return null;
	}

	/**
	 * Returns the bundles trace handler.
	 *
	 * @return The bundles trace handler.
	 */
	public static TraceHandler getTraceHandler() {
		if (traceHandler == null) {
			traceHandler = new TraceHandler(getUniqueIdentifier());
		}
		return traceHandler;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
    public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
    public void stop(BundleContext context) throws Exception {
		plugin = null;
		traceHandler = null;
		super.stop(context);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#initializeImageRegistry(org.eclipse.jface.resource.ImageRegistry)
	 */
	@Override
	protected void initializeImageRegistry(ImageRegistry registry) {
	}

	/**
	 * Loads the image registered under the specified key from the image
	 * registry and returns the <code>Image</code> object instance.
	 *
	 * @param key The key the image is registered with.
	 * @return The <code>Image</code> object instance or <code>null</code>.
	 */
	public static Image getImage(String key) {
		return getDefault().getImageRegistry().get(key);
	}

	/**
	 * Loads the image registered under the specified key from the image
	 * registry and returns the <code>ImageDescriptor</code> object instance.
	 *
	 * @param key The key the image is registered with.
	 * @return The <code>ImageDescriptor</code> object instance or <code>null</code>.
	 */
	public static ImageDescriptor getImageDescriptor(String key) {
		return getDefault().getImageRegistry().getDescriptor(key);
	}

	/**
	 * Returns the ELF image type if the specified file is an ELF file at all.
	 *
	 * @param file The file representation of the physical file to test. Must be not <code>null</code>!
	 * @return The ELF image type as defined within <code>org.eclipse.cdt.utils.elf.Elf.Attribute</code>, or <code>-1</code> if invalid or not detectable.
	 */
	public int getELFType(File file) throws IOException {
		int type = -1;

		if (file != null) {
			Elf elfFile = null;
			try {
				elfFile = new Elf(file.getAbsolutePath());
				type = elfFile.getAttributes().getType();
			}
			finally {
				if (elfFile != null) {
					elfFile.dispose();
				}
				elfFile = null;
			}
		}
		return type;
	}

	/**
	 * Returns the ELF address class if the specified file is an ELF file at all.
	 *
	 * @param file The file representation of the physical file to test. Must be not <code>null</code>!
	 * @return The ELF address class as defined within <code>org.eclipse.cdt.utils.elf.Elf.ELFhdr.ELFCLASS*</code>. <code>ELFCLASSNONE</code> (0) if the ELF address class is not set.
	 */
	public int getELFClass(File file) throws IOException {
		int elfclass = Elf.ELFhdr.ELFCLASSNONE;

		if (file != null) {
			Elf elfFile = null;
			try {
				elfFile = new Elf(file.getAbsolutePath());
				elfclass = elfFile.getELFhdr().e_ident[ELFhdr.EI_CLASS];
			}
			finally {
				if (elfFile != null) {
					elfFile.dispose();
				}
				elfFile = null;
			}
		}
		return elfclass;
	}
}
