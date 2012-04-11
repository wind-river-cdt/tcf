/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.core.persistence.filetransfer;

import java.util.List;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.tcf.te.launch.core.lm.interfaces.IFileTransferLaunchAttributes;
import org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification;
import org.eclipse.tcf.te.launch.core.persistence.AbstractItemListPersistenceDelegate;
import org.eclipse.tcf.te.launch.core.persistence.AbstractItemListXMLParser;
import org.eclipse.tcf.te.runtime.services.interfaces.filetransfer.IFileTransferItem;

/**
 * File transfer persistence delegate.
 */
public class FileTransfersPersistenceDelegate {

	private static final String TAG_FILE_TRANSFER = "fileTransfer"; //$NON-NLS-1$

	private static final AbstractItemListPersistenceDelegate<IFileTransferItem> delegate =
					new AbstractItemListPersistenceDelegate<IFileTransferItem>(TAG_FILE_TRANSFER, IFileTransferLaunchAttributes.ATTR_FILE_TRANSFERS) {
		@Override
		protected AbstractItemListXMLParser<IFileTransferItem> getXMLParser() {
			return new AbstractItemListXMLParser<IFileTransferItem>(TAG_FILE_TRANSFER) {
				@Override
				protected Class<?> getReadClass() {
					return IFileTransferItem.class;
				}
			};
		}
	};

	/**
	 * Saves the selected file transfer items to the specified launch configuration working copy. If the
	 * selected file transfer items are <code>null</code> or empty, the attribute will be removed from
	 * the specified launch configuration working copy.
	 *
	 * @param wc The launch configuration working copy. Must not be <code>null</code>.
	 * @param items The file transfer items to save or <code>null</code>.
	 */
	public final static void setFileTransfers(ILaunchConfigurationWorkingCopy wc, IFileTransferItem[] items) {
		delegate.setItems(wc, items);
	}

	/**
	 * Saves the selected file transfer items to the specified launch specification. If the selected
	 * file transfer items are <code>null</code> or empty, the attribute will be removed from the
	 * specified launch specification.
	 *
	 * @param launchSpec The launch specification. Must not be <code>null</code>.
	 * @param items The file transfer items to save or <code>null</code>.
	 */
	public final static void setFileTransfers(ILaunchSpecification launchSpec, IFileTransferItem[] items) {
		delegate.setItems(launchSpec, items);
	}

	/**
	 * Writes the given file transfer items into a string encoded in XML.
	 *
	 * @param items The file transfer items to encode. Must not be <code>null</code>.
	 * @return The full XML representation of the given items or <code>null</code>.
	 */
	public final static String encodeFileTransferItems(IFileTransferItem[] items) {
		return delegate.encodeItems(items);
	}

	/**
	 * Reads the selected file transfer items from the given XML encoded string.
	 *
	 * @param encodedItems The selected file transfer items encoded as XML string. Must not be <code>null</code>.
	 * @return The selected file transfer items or an empty array.
	 */
	public final static IFileTransferItem[] decodeFileTransferItems(String encodedItems) {
		List<IFileTransferItem> list = delegate.decodeItems(encodedItems);
		return list.toArray(new IFileTransferItem[list.size()]);
	}

	/**
	 * Returns the list of configured file transfer items from the given launch configuration.
	 * <p>
	 * If the given launch configuration is <code>null</code> and the method will return an empty
	 * array.
	 *
	 * @param configuration The launch configuration or <code>null</code>.
	 * @return The list of configured file transfer items or an empty array.
	 */
	public static final IFileTransferItem[] getFileTransfers(ILaunchConfiguration configuration) {
		List<IFileTransferItem> list = delegate.getItems(configuration);
		return list.toArray(new IFileTransferItem[list.size()]);
	}

	/**
	 * Returns the list of configured file transfer items from the given launch specification.
	 * <p>
	 * If the given launch specification is <code>null</code> and the method will return an empty
	 * array.
	 *
	 * @param launchSpec The launch specification or <code>null</code>.
	 * @return The list of configured file transfer items or an empty array.
	 */
	public static final IFileTransferItem[] getFileTransfers(ILaunchSpecification launchSpec) {
		List<IFileTransferItem> list = delegate.getItems(launchSpec);
		return list.toArray(new IFileTransferItem[list.size()]);
	}
}
