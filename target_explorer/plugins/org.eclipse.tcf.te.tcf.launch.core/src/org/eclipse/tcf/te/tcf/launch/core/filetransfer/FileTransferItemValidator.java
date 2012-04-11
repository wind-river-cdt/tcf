/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.tcf.te.tcf.launch.core.filetransfer;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.tcf.te.runtime.services.interfaces.filetransfer.IFileTransferItem;
import org.eclipse.tcf.te.tcf.launch.core.nls.Messages;

/**
 * FileTransferItemValidator
 */
public class FileTransferItemValidator {

	public static final Map<String,String> validate(IFileTransferItem item) {
		Assert.isNotNull(item);

		Map<String,String> invalid = new HashMap<String,String>();

		String host = item.getStringProperty(IFileTransferItem.PROPERTY_HOST);
		String target = item.getStringProperty(IFileTransferItem.PROPERTY_TARGET);
		int direction = (item.getProperty(IFileTransferItem.PROPERTY_DIRECTION) != null ?
						item.getIntProperty(IFileTransferItem.PROPERTY_DIRECTION) : IFileTransferItem.HOST_TO_TARGET);

		if (host == null || host.trim().length() == 0) {
			if (direction == IFileTransferItem.HOST_TO_TARGET) {
				invalid.put(IFileTransferItem.PROPERTY_HOST, Messages.FileTransferItemValidator_missingFile);
			}
			else {
				invalid.put(IFileTransferItem.PROPERTY_HOST, Messages.FileTransferItemValidator_missingFileOrDirectory);
			}
		}
		else {
			IPath hostPath = new Path(host);
			File hostFile = hostPath.toFile();
			if (direction == IFileTransferItem.HOST_TO_TARGET) {
				if (!hostFile.exists() || !hostFile.isFile() || !hostFile.canRead()) {
					invalid.put(IFileTransferItem.PROPERTY_HOST, Messages.FileTransferItemValidator_notExistingFile);
				}
			}
			else {
				if ((hostFile.isFile() && !hostFile.canWrite()) || (hostFile.isDirectory() && (!hostFile.exists() || !hostFile.canWrite()))) {
					invalid.put(IFileTransferItem.PROPERTY_HOST, Messages.FileTransferItemValidator_notExistingFileOrDirectory);
				}
			}
		}

		if (target == null || target.trim().length() == 0) {
			if (direction == IFileTransferItem.HOST_TO_TARGET) {
				invalid.put(IFileTransferItem.PROPERTY_TARGET, Messages.FileTransferItemValidator_missingFileOrDirectory);
			}
			else {
				invalid.put(IFileTransferItem.PROPERTY_TARGET, Messages.FileTransferItemValidator_missingFile);
			}
		}
		else {
			IPath targetPath = new Path(target);
			if (direction == IFileTransferItem.HOST_TO_TARGET) {
				if (!targetPath.isValidPath(target)) {
					invalid.put(IFileTransferItem.PROPERTY_TARGET, Messages.FileTransferItemValidator_invalidFileOrDirectory);
				}
			}
			else {
				if (!targetPath.isValidPath(target)) {
					invalid.put(IFileTransferItem.PROPERTY_TARGET, Messages.FileTransferItemValidator_invalidFile);
				}
			}
		}

		return invalid.isEmpty() ? null : invalid;
	}
}
