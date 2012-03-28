/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.debug.ui;

import org.eclipse.debug.ui.sourcelookup.ISourceDisplay;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * ITCFSourceDisplay is an interface that is implemented by the TCF debug model.
 * A visual element in the debugger view can be adapted to this interface -
 * if the element represents a remote TCF object.
 * Clients can use this interface to open source text editor.
 */
public interface ITCFSourceDisplay extends ISourceDisplay {

    /**
     * Open source text editor.
     * The editor is shown in currently active Eclipse window.
     * Source file name is translated using source lookup settings of the debugger.
     * "Source not found" window is shown if the file cannot be located.
     * This method should be called on the display thread.
     * @param context_id - debug context ID.
     * @param source_file_name - compile-time source file name.
     * @param line - scroll the editor to reveal this line.
     * @return - text editor interface.
     */
    ITextEditor displaySource(String context_id, String source_file_name, int line);
}
