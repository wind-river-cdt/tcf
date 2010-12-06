/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.cdt.ui;

import org.eclipse.cdt.debug.core.model.ISteppingModeTarget;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFModel;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNode;

@SuppressWarnings("rawtypes")
public class TCFNodeAdapterFactory implements IAdapterFactory {

    private static final Class<?>[] CLASSES = { ISteppingModeTarget.class };

    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (adaptableObject instanceof TCFNode) {
            if (ISteppingModeTarget.class == adapterType) {
                TCFNode node = (TCFNode) adaptableObject;
                TCFModel model = node.getModel();
                ISteppingModeTarget target = (ISteppingModeTarget) model.getAdapter(adapterType, node);
                if (target == null) {
                    model.setAdapter(adapterType, target = new TCFSteppingModeTarget(model));
                }
                return target;
            }
        }
        return null;
    }

    public Class[] getAdapterList() {
        return CLASSES;
    }

}