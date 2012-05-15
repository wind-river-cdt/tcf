/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.internal.debug.ui.adapters;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tcf.internal.debug.ui.model.TCFModelPresentation;

public class TCFBreakpointAdapterFactory implements IAdapterFactory {

    private static final Class<?>[] adapter_list = {
        ILabelProvider.class,
    };

    private static final ILabelProvider label_provider = new ILabelProvider() {

        public void removeListener(ILabelProviderListener listener) {
        }

        public boolean isLabelProperty(Object element, String property) {
            return false;
        }

        public void dispose() {
        }

        public void addListener(ILabelProviderListener listener) {
        }

        public String getText(Object element) {
            return TCFModelPresentation.getDefault().getText(element);
        }

        public Image getImage(Object element) {
            return TCFModelPresentation.getDefault().getImage(element);
        }
    };

    @SuppressWarnings("rawtypes")
    public Object getAdapter(Object obj, Class cls) {
        if (obj instanceof IBreakpoint) {
            if (cls == ILabelProvider.class) return label_provider;
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    public Class[] getAdapterList() {
        return adapter_list;
    }
}
