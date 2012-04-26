/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.tcf.te.launch.ui.tabs.filetransfers;

import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.tcf.te.runtime.services.interfaces.filetransfer.IFileTransferItem;

/**
 * FileTransferCheckStateProvider
 */
public class FileTransferCheckStateProvider implements ICheckStateProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ICheckStateProvider#isGrayed(java.lang.Object)
	 */
	@Override
	public boolean isGrayed(Object element) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ICheckStateProvider#isChecked(java.lang.Object)
	 */
	@Override
	public boolean isChecked(Object element) {
		if (element instanceof IFileTransferItem) {
			IFileTransferItem item = (IFileTransferItem)element;
			return item.getBooleanProperty(IFileTransferItem.PROPERTY_ENABLED);
		}
		return false;
	}
}