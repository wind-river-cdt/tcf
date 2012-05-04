/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.internal.cdt.ui.breakpoints;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.tcf.internal.cdt.ui.ImageCache;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * 
 */
public class BreakpointScopeCategory extends PlatformObject implements IWorkbenchAdapter {

    private static Object[] EMPTY_CHILDREN_ARRAY = new Object[0];
    
    private final String fFilter;
    
    public BreakpointScopeCategory(String filter) {
        fFilter = filter;
    }
    
    public String getFilter() {
        return fFilter;
    }
    
    public String getLabel(Object o) {
        return "Scope: " + getFilter();
    }
    
    public ImageDescriptor getImageDescriptor(Object object) {
        return ImageCache.getImageDescriptor(ImageCache.IMG_BREAKPOINT_SCOPE);
    }
    
    public Object[] getChildren(Object o) {
        // Not used
        return EMPTY_CHILDREN_ARRAY;
    }
    
    public Object getParent(Object o) {
        // Not used
        return null;
    }
    
    @Override
    public boolean equals(Object obj) {
        return obj instanceof BreakpointScopeCategory && 
               ((BreakpointScopeCategory)obj).getFilter().equals(getFilter());
    }
    
    @Override
    public int hashCode() {
        return fFilter.hashCode();
    }
}
