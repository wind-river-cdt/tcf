/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.tcf.te.tcf.launch.ui.filetransfer;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tcf.te.launch.ui.tabs.filetransfers.AbstractFileTransferSection;
import org.eclipse.tcf.te.runtime.services.filetransfer.FileTransferItem;
import org.eclipse.tcf.te.runtime.services.interfaces.filetransfer.IFileTransferItem;
import org.eclipse.tcf.te.tcf.launch.core.filetransfer.FileTransferItemValidator;
import org.eclipse.ui.forms.IManagedForm;

/**
 * FileTransferSection
 */
public class FileTransferSection extends AbstractFileTransferSection {

	/**
	 * Constructor.
	 * @param form
	 * @param parent
	 */
	public FileTransferSection(IManagedForm form, Composite parent) {
		super(form, parent);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.ui.tabs.filetransfers.AbstractFileTransferSection#validateInputList()
	 */
	@Override
	protected boolean validateInputList() {
		List<IFileTransferItem> list = getInputList();
		boolean valid = true;
		for (IFileTransferItem item : list) {
			Map<String,String> invalid = item.getBooleanProperty(IFileTransferItem.PROPERTY_ENABLED) ? FileTransferItemValidator.validate(item) : null;
			item.setProperty(PROPERTY_VALIDATION_RESULT, invalid);
			if (valid && invalid != null) {
				valid = false;
				setMessage(invalid.get(invalid.keySet().toArray()[0]), IMessageProvider.ERROR);
			}
		}
		return valid;
	}

	@Override
	protected void onButtonAddClick() {
		int selIndex = ((TableViewer)getTablePart().getViewer()).getTable().getSelectionIndex();
		List<IFileTransferItem> list = getInputList();
		IFileTransferItem item = new FileTransferItem();
		AddEditFileTransferDialog dialog = new AddEditFileTransferDialog(getSection().getShell(), null, item, launchContext);
		if (dialog.open() == Window.OK) {
			item.setProperty(IFileTransferItem.PROPERTY_ENABLED, true);
			list.add(selIndex != -1 ? selIndex : 0, item);
			setInputList(list);
			((TableViewer)getTablePart().getViewer()).setSelection(new StructuredSelection(item), true);
		}
	}

	@Override
	protected void onButtonEditClick() {
		int selIndex = ((TableViewer)getTablePart().getViewer()).getTable().getSelectionIndex();

		if (selIndex >= 0) {
			List<IFileTransferItem> list = getInputList();
			IFileTransferItem item = list.get(selIndex);

			AddEditFileTransferDialog dialog = new AddEditFileTransferDialog(getSection().getShell(), null, item, launchContext);
			if (dialog.open() == Window.OK) {
				list.set(selIndex, item);
				setInputList(list);
				((TableViewer)getTablePart().getViewer()).setSelection(new StructuredSelection(item), true);
			}
		}
	}
}
