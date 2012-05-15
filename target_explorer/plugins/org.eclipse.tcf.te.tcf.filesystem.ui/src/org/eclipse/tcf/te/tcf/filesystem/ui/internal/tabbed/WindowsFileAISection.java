/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.ui.internal.tabbed;

import org.eclipse.tcf.te.tcf.filesystem.ui.nls.Messages;

/**
 * The property section to display the file's archive and index attributes on Windows.
 */
public class WindowsFileAISection extends WindowsFolderAISection {
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.internal.tabbed.WindowsFolderAISection#getAchiveText()
	 */
	@Override
    protected String getAchiveText() {
		return Messages.AdvancedAttributesDialog_FileArchive;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.internal.tabbed.WindowsFolderAISection#getIndexText()
	 */
	@Override
    protected String getIndexText() {
		return Messages.AdvancedAttributesDialog_IndexFile;
	}
}
