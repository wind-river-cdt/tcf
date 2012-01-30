/*******************************************************************************
 * Copyright (c) 2008, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.internal.debug.ui.adapters;

import java.util.Collection;
import java.util.Map;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.tcf.internal.debug.model.TCFLaunch;
import org.eclipse.tcf.internal.debug.ui.ImageCache;
import org.eclipse.tcf.internal.debug.ui.model.TCFModel;
import org.eclipse.tcf.services.IProcesses;

class TCFLaunchLabelProvider implements IElementLabelProvider {

    public void update(ILabelUpdate[] updates) {
        for (int i = 0; i < updates.length; i++) {
            ILabelUpdate result = updates[i];
            final TCFLaunch launch = (TCFLaunch)result.getElement();
            ImageDescriptor image = DebugUITools.getDefaultImageDescriptor(launch);
            if (image == null) image = ImageCache.getImageDescriptor(ImageCache.IMG_TCF);
            result.setImageDescriptor(image, 0);
            String status = "";
            if (launch.isConnecting()) {
                status = "Connecting";
            }
            else if (launch.isDisconnected()) {
                status = "Disconnected";
            }
            String peer_name = launch.getPeerName();
            if (peer_name != null) {
                if (status.length() == 0) status = peer_name;
                else status = peer_name + ": " + status;
            }
            if (status.length() > 0) status = " (" + status + ")";
            Throwable error = launch.getError();
            if (error != null) {
                status += ": " + TCFModel.getErrorMessage(error, false);
                result.setForeground(new RGB(255, 0, 0), 0);
            }
            else if (launch.isExited()) {
                status += ": All exited or detached";
                int code = launch.getExitCode();
                if (code > 0) status += ", exit code " + code;
                if (code < 0) {
                    status += ", signal " + (-code);
                    Collection<Map<String,Object>> sigs = launch.getSignalList();
                    if (sigs != null) {
                        for (Map<String,Object> m : sigs) {
                            Number num = (Number)m.get(IProcesses.SIG_CODE);
                            if (num == null) continue;
                            if (num.intValue() != -code) continue;
                            String s = (String)m.get(IProcesses.SIG_NAME);
                            if (s == null) continue;
                            status += " (" + s + ")";
                            break;
                        }
                    }
                }
            }
            String name = "?";
            ILaunchConfiguration cfg = launch.getLaunchConfiguration();
            if (cfg != null) name = cfg.getName();
            result.setLabel(name + status, 0);
            result.done();
        }
    }
}
