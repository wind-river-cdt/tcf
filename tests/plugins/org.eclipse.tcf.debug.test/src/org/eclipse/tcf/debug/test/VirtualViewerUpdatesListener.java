/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.debug.test;

import java.util.Iterator;
import java.util.regex.Pattern;

import org.eclipse.debug.internal.ui.viewers.model.provisional.VirtualItem;
import org.eclipse.debug.internal.ui.viewers.model.provisional.VirtualTreeModelViewer;

/**
 * Extends base listener to use virtual viewer capabilities. 
 */
public class VirtualViewerUpdatesListener extends ViewerUpdatesListener {
    private final VirtualTreeModelViewer fVirtualViewer;
    
    public VirtualViewerUpdatesListener(VirtualTreeModelViewer viewer) {
        super(viewer, false, false);
        fVirtualViewer  = viewer;
    }

    public VirtualItem findElement(Pattern[] patterns) {
        return findElement(fVirtualViewer.getTree(), patterns);
    }
    
    public VirtualItem findElement(VirtualItem parent, Pattern[] patterns) {
        VirtualItem item = parent;
        patterns: for (int i = 0; i < patterns.length; i++) {
            for (VirtualItem child : item.getItems()) {
                String[] label = (String[])child.getData(VirtualItem.LABEL_KEY);
                if (label != null && label.length >= 1 && label[0] != null && patterns[i].matcher(label[0]).matches()) {
                    item = child;
                    continue patterns;
                }
            }
            return null;
        }
        return item;
    }
    
    
}
