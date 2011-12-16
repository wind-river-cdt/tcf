/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.ui.views.scriptpad;

import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.tcf.te.runtime.interfaces.IDisposable;

/**
 * Script Pad line style listener implementation.
 */
public class ScriptPadLineStyleListener implements LineStyleListener, IDisposable {
	private static final Pattern COMMENT_LINE = Pattern.compile("\\s*#.*"); //$NON-NLS-1$
	private static final Pattern CONNECT_LINE = Pattern.compile("\\s*connect\\s+.*"); //$NON-NLS-1$
//	private static final Pattern COMMAND_LINE = Pattern.compile("\\s*tcf\\s+(\\w+)\\s+(\\w+)(.*)"); //$NON-NLS-1$

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.interfaces.IDisposable#dispose()
	 */
	@Override
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.custom.LineStyleListener#lineGetStyle(org.eclipse.swt.custom.LineStyleEvent)
	 */
	@Override
	public void lineGetStyle(LineStyleEvent event) {
		String text = event.lineText;

		if (text != null && !"".equals(text)) { //$NON-NLS-1$

			if (COMMENT_LINE.matcher(text).matches()) {
				StyleRange range = new StyleRange(event.lineOffset, text.length(),
													event.widget.getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN),
													((StyledText)event.widget).getBackground());
				event.styles = new StyleRange[] { range };
			}
			else if (CONNECT_LINE.matcher(text).matches()) {
				StyleRange range = new StyleRange(event.lineOffset, text.length(),
													event.widget.getDisplay().getSystemColor(SWT.COLOR_GRAY),
													((StyledText)event.widget).getBackground(),
													SWT.ITALIC);
				event.styles = new StyleRange[] { range };
			}
		}
	}

}
