/*******************************************************************************
 * Copyright (c) 2009, 2012 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.debug.test;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.internal.ui.viewers.model.provisional.VirtualItem;
import org.eclipse.debug.internal.ui.viewers.model.provisional.VirtualTreeModelViewer;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.tcf.debug.ui.ITCFObject;

/**
 * Extends base listener to use virtual viewer capabilities. 
 */
@SuppressWarnings("restriction")
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
        return findElement(parent, patterns, 0);
    }

    private VirtualItem findElement(VirtualItem parent, Pattern[] patterns, int patternIdx) {
        if (patternIdx >= patterns.length) return parent;
        for (VirtualItem child : parent.getItems()) {
            String[] label = (String[])child.getData(VirtualItem.LABEL_KEY);
            if (label != null && label.length >= 1 && label[0] != null && 
                patterns[patternIdx].matcher(label[0]).matches()) 
            {
                VirtualItem item = findElement(child, patterns, patternIdx+1);
                if (item != null) {
                    return item;
                }
            }
        }
        return null;
    }

    @Override
    protected Set<TreePath> makeTreePathSet() {
        return new MatchingSet();
    }
    
    protected boolean matchPath(TreePath patternPath, TreePath elementPath) {
        if (patternPath.getSegmentCount() != elementPath.getSegmentCount()) {
            return false;
        }
        
        for (int i = 0; i < patternPath.getSegmentCount(); i++) {
            Object patternSegment = patternPath.getSegment(i);
            Object elementSegment = elementPath.getSegment(i);
            if ( fPatternComparer.equals(elementSegment, patternSegment) ) {
                continue;
            } else if (fTCFContextComparer.equals(elementSegment, patternSegment) ) {
                continue;
            } else if (patternSegment.equals(elementSegment)) {
                continue;
            } 
            return false;
        }
        return true;
    }

    
    private final IElementComparer fPatternComparer = new IElementComparer() {
        public boolean equals(Object a, Object b) {
            Pattern pattern = null;
            Object element = null;
            if (a instanceof Pattern) {
                pattern = (Pattern) a;
                element = b;
            } else if (b instanceof Pattern) {
                pattern = (Pattern) b;
                element = a;
            }
            if (pattern != null) {
                return elementMatches(element, pattern);
            }
            return false;
        }
        
        private boolean elementMatches(Object element, Pattern pattern) {
            VirtualItem[] items = fVirtualViewer.findItems(element);
            if (items.length >= 0) {
                String[] label = (String[])items[0].getData(VirtualItem.LABEL_KEY);
                if (label != null && label.length >= 1 && label[0] != null && pattern.matcher(label[0]).matches()) {
                    return true;
                }
            }
            return false;
        }
        
        public int hashCode(Object element) {
            throw new UnsupportedOperationException();
        }
    };

    private final IElementComparer fTCFContextComparer = new IElementComparer() {
        public boolean equals(Object a, Object b) {
            ITCFObject context = null;
            Object element = null;
            if (a instanceof ITCFObject) {
                context = (ITCFObject) a;
                element = b;
            } else if (b instanceof ITCFObject) {
                context = (ITCFObject) b;
                element = a;
            }
            if (context != null) {
                return elementMatches((ITCFObject)DebugPlugin.getAdapter(element, ITCFObject.class), context);
            }
            return false;
        }
        
        private boolean elementMatches(ITCFObject element, ITCFObject pattern) {
            return element != null && element.getID().equals(pattern.getID());
        }
        
        public int hashCode(Object element) {
            throw new UnsupportedOperationException();
        }
    };

    class MatchingSet extends AbstractSet<TreePath> {
        List<TreePath> fList = new ArrayList<TreePath>(4);
        
        @Override
        public Iterator<TreePath> iterator() {
            return fList.iterator();
        }

        @Override
        public int size() {
            return fList.size();
        }
        
        @Override
        public boolean add(TreePath o) {
            return fList.add(o);
        }
        
        @Override
        public void clear() {
            fList.clear();
        }
        
        @Override
        public boolean contains(Object o) {
            if (o instanceof TreePath) {
                return find((TreePath)o) >= 0;
            }
            return false;
        }
        
        @Override
        public boolean remove(Object o) {
            if (o instanceof TreePath) {
                int index = find((TreePath)o);
                if (index >= 0) {
                    fList.remove(index);
                    return true;
                }
            }
            return false;
        }
        
        private int find(TreePath path) {
            for (int i = 0; i < fList.size(); i++) {
                if (matchPath(fList.get(i), path)) {
                    return i;
                }
            }
            return -1;
        }
        
    }
}
