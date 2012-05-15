/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.processes.ui.internal.tabbed;

import java.util.Map.Entry;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * The label provider to provide texts and images of map entries.
 */
public class MapEntryLabelProvider extends LabelProvider implements ITableLabelProvider {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
	 */
	@Override
    public Image getColumnImage(Object element, int columnIndex) {
        return null;
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
	 */
	@Override
    public String getColumnText(Object element, int columnIndex) {
		if(element instanceof Entry) {
			Entry<?, ?> entry = (Entry<?, ?>) element;
			if(columnIndex == 0) {
				Object key = entry.getKey();
				return key == null ? "" : key.toString(); //$NON-NLS-1$
			}
			Object object = entry.getValue();
			return object == null ? "" : object.toString(); //$NON-NLS-1$
		}
        return null;
    }
}