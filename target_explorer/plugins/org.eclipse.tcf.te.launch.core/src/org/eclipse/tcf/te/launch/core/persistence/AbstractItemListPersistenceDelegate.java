/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.core.persistence;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.tcf.te.launch.core.activator.CoreBundleActivator;
import org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification;
import org.eclipse.tcf.te.runtime.persistence.PersistenceManager;
import org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistenceDelegate;

/**
 * Item list persistence delegate.
 */
public abstract class AbstractItemListPersistenceDelegate<ItemType> {

	private final String tagName;
	private final String key;

	public AbstractItemListPersistenceDelegate(String tagName, String key) {
		super();

		Assert.isNotNull(tagName);
		Assert.isNotNull(key);
		this.tagName = tagName;
		this.key = key;
	}

	/**
	 * Saves the selectedr items to the specified launch configuration working copy. If the
	 * selected items are <code>null</code> or empty, the attribute will be removed from
	 * the specified launch configuration working copy.
	 * @param <ItemType>
	 *
	 * @param wc The launch configuration working copy. Must not be <code>null</code>.
	 * @param items The items to save or <code>null</code>.
	 */
	public final void setItems(ILaunchConfigurationWorkingCopy wc, ItemType[] items) {
		Assert.isNotNull(wc);

		if (items == null || items.length == 0) {
			DefaultPersistenceDelegate.setAttribute(wc, key, (String)null);
			return;
		}

		// Get the encoded XML representation
		String xml = encodeItems(items);
		// And save them to the launch configuration. If XML == null, the
		// items will be removed from the launch configuration
		DefaultPersistenceDelegate.setAttribute(wc, key, xml);
	}

	/**
	 * Saves the selected items to the specified launch specification. If the selected
	 * items are <code>null</code> or empty, the attribute will be removed from the
	 * specified launch specification.
	 *
	 * @param launchSpec The launch specification. Must not be <code>null</code>.
	 * @param items The items to save or <code>null</code>.
	 */
	public final void setItems(ILaunchSpecification launchSpec, ItemType[] items) {
		Assert.isNotNull(launchSpec);

		if (items == null || items.length == 0) {
			launchSpec.removeAttribute(key);
			return;
		}

		// Get the encoded XML representation
		String xml = encodeItems(items);
		// And save them to the launch specification. If XML == null, the
		// items will be removed from the launch specification
		launchSpec.addAttribute(key, xml);
	}

	/**
	 * Writes the given items into a string encoded in XML.
	 *
	 * @param items The items to encode. Must not be <code>null</code>.
	 * @return The full XML representation of the given items or <code>null</code>.
	 */
	public final String encodeItems(ItemType[] items) {
		Assert.isNotNull(items);

		// The final result
		String result = null;

		// First, we write the selected items as XML representation into a string
		StringWriter writer = new StringWriter();

		try {
			// Write the header and get the initial indentation
			String indentation = writeHeader(writer);
			// Iterate over the given selected items and write them out.
			for (ItemType item : items) {
				writeItem(writer, indentation, item);
			}
			// Write the footer
			writeFooter(writer);

			// Convert into a string
			result = writer.toString();
		}
		catch (IOException e) {
			// Export to the string writer failed --> remove attribute from launch configuration
			if (Platform.inDebugMode()) {
				IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(),
								"Launch framework internal error: " + e.getLocalizedMessage(), e); //$NON-NLS-1$
				Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(status);
			}
			result = null;
		}
		finally {
			try {
				writer.close();
			}
			catch (IOException e) { /* ignored on purpose */
			}
		}

		return result;
	}

	/**
	 * Writes the header to the given writer and returns the indentation to be used for following
	 * elements.
	 *
	 * @param writer The writer instance. Must not be <code>null</code>.
	 * @throws IOException in case the write failed.
	 */
	private String writeHeader(Writer writer) throws IOException {
		Assert.isNotNull(writer);
		writer.write("<" + tagName + "s>\n"); //$NON-NLS-1$ //$NON-NLS-2$
		return "\t"; //$NON-NLS-1$
	}

	/**
	 * Writes the footer to the given writer and returns the indentation to be used for following
	 * elements.
	 *
	 * @param writer The writer instance. Must not be <code>null</code>.
	 * @throws IOException in case the write failed.
	 */
	private String writeFooter(Writer writer) throws IOException {
		Assert.isNotNull(writer);
		writer.write("</" + tagName + "s>\n"); //$NON-NLS-1$ //$NON-NLS-2$
		return ""; //$NON-NLS-1$
	}

	/**
	 * Writes the item to the given writer.
	 *
	 * @param writer The writer instance. Must not be <code>null</code>.
	 * @param indentation The indentation to prefix each exported line with. Must not be <code>null</code>.
	 * @param item The item instance. Must not be <code>null</code>.
	 *
	 * @throws IOException in case the write failed.
	 */
	private void writeItem(Writer writer, String indentation, ItemType item) throws IOException {
		Assert.isNotNull(writer);
		Assert.isNotNull(indentation);
		Assert.isNotNull(item);

		IPersistenceDelegate delegate = PersistenceManager.getInstance().getDelegate(item, String.class, false);
		if (delegate != null) {
			writer.write(indentation + "<" + tagName + " " + AbstractItemListXMLParser.ATTR_TYPE + "=\"" + delegate.getPersistedClass(item).getName() + "\">\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			writer.write(indentation + "\t" + delegate.write(item, String.class, null) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
			writer.write(indentation + "</" + tagName + ">\n"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * Reads the selected items from the given XML encoded string.
	 *
	 * @param encodedItems The selected items encoded as XML string. Must not be <code>null</code>.
	 * @return The selected items or an empty array.
	 */
	public final List<ItemType> decodeItems(String encodedItems) {
		Assert.isNotNull(encodedItems);

		List<ItemType> items = new ArrayList<ItemType>();

		if (!"".equals(encodedItems.trim())) { //$NON-NLS-1$
			// We have to parse the items from the string
			InputStream input = new ByteArrayInputStream(encodedItems.getBytes());
			// Instantiate the XML parser
			AbstractItemListXMLParser<ItemType> xmlParser = getXMLParser();
			xmlParser.initXMLParser();
			xmlParser.setItems(items);
			try {
				xmlParser.getXMLReader().parse(input, xmlParser);
			}
			catch (Exception e) {
				// Import failed --> remove attribute from launch configuration
				if (Platform.inDebugMode()) {
					IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(),
									"Launch framework internal error: " + e.getLocalizedMessage(), e); //$NON-NLS-1$
					Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(status);
				}
				items = null;
			}
		}

		return items;
	}

	protected abstract AbstractItemListXMLParser<ItemType> getXMLParser();

	/**
	 * Returns the list of configured items from the given launch configuration.
	 * <p>
	 * If the given launch configuration is <code>null</code> and the method will return an empty
	 * array.
	 *
	 * @param configuration The launch configuration or <code>null</code>.
	 * @param ItemType
	 * @return The list of configured items or an empty array.
	 */
	public final List<ItemType> getItems(ILaunchConfiguration configuration) {
		List<ItemType> items = new ArrayList<ItemType>();

		if (configuration != null) {
			// Read the attribute from the launch configuration
			String encodedItems = DefaultPersistenceDelegate.getAttribute(configuration, key, (String) null);
			if (encodedItems != null) {
				items = decodeItems(encodedItems);
			}
		}

		return items;
	}

	/**
	 * Returns the list of configured items from the given launch specification.
	 * <p>
	 * If the given launch specification is <code>null</code> and the method will return an empty
	 * array.
	 *
	 * @param launchSpec The launch specification or <code>null</code>.
	 * @return The list of configured items or an empty array.
	 */
	public final List<ItemType> getItems(ILaunchSpecification launchSpec) {
		List<ItemType> items = new ArrayList<ItemType>();

		if (launchSpec != null) {
			// Read the attribute from the launch specification
			String encodedItems = (String) launchSpec.getAttribute(key, null);
			if (encodedItems != null) {
				items = decodeItems(encodedItems);
			}
		}

		return items;
	}
}
