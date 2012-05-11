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
package org.eclipse.tcf.internal.debug.ui.model;

import java.util.HashMap;

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.tcf.protocol.Protocol;

public class TCFModelFonts {

    private static IPropertyChangeListener listener;
    private static final HashMap<String,FontData> fd_normal = new HashMap<String,FontData>();
    private static final HashMap<String,FontData> fd_italic = new HashMap<String,FontData>();
    private static final HashMap<String,FontData> fd_monospaced = new HashMap<String,FontData>();

    public static FontData getNormalFontData(String view_id) {
        FontData fd = fd_normal.get(view_id);
        if (fd == null) {
            if (listener == null) {
                listener = new IPropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent event) {
                        Protocol.invokeLater(new Runnable() {
                            public void run() {
                                fd_normal.clear();
                                fd_italic.clear();
                                fd_monospaced.clear();
                            }
                        });
                    }
                };
                JFaceResources.getFontRegistry().addListener(listener);
            }
            if (IDebugUIConstants.ID_DEBUG_VIEW.equals(view_id)) {
                fd = JFaceResources.getFontDescriptor(JFaceResources.DEFAULT_FONT).getFontData()[0];
            }
            else if (TCFDetailPane.ID.equals(view_id)) {
                FontData ff = JFaceResources.getFontDescriptor(JFaceResources.DEFAULT_FONT).getFontData()[0];
                FontData fp = JFaceResources.getFontDescriptor(IDebugUIConstants.PREF_DETAIL_PANE_FONT).getFontData()[0];
                fd = new FontData(fp.getName(), ff.getHeight(), SWT.NORMAL);
            }
            else {
                fd = JFaceResources.getFontDescriptor(IDebugUIConstants.PREF_VARIABLE_TEXT_FONT).getFontData()[0];
            }
            fd_normal.put(view_id, fd);
        }
        return fd;
    }

    public static FontData getItalicFontData(String view_id) {
        FontData fd = fd_italic.get(view_id);
        if (fd == null) {
            FontData fn = getNormalFontData(view_id);
            fd = new FontData(fn.getName(), fn.getHeight(), SWT.ITALIC);
            fd_italic.put(view_id, fd);
        }
        return fd;
    }

    public static FontData getMonospacedFontData(String view_id) {
        FontData fd = fd_monospaced.get(view_id);
        if (fd == null) {
            FontData fn = getNormalFontData(view_id);
            FontData fm = JFaceResources.getFontDescriptor(JFaceResources.TEXT_FONT).getFontData()[0];
            fd = new FontData(fm.getName(), fn.getHeight(), fn.getStyle());
            fd_monospaced.put(view_id, fd);
        }
        return fd;
    }
}
