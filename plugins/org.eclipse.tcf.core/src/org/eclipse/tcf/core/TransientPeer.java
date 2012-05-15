/*******************************************************************************
 * Copyright (c) 2008, 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.tcf.internal.core.TransportManager;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IPeer;

/**
 * Transient implementation of IPeer interface.
 * Objects of this class are not tracked by Locator service.
 * See AbstractPeer for IPeer objects that should go into the Locator table.
 */
public class TransientPeer implements IPeer {

    protected final Map<String, String> ro_attrs;
    protected final Map<String, String> rw_attrs;

    public TransientPeer(Map<String,String> attrs) {
        rw_attrs = new HashMap<String,String>(attrs);
        ro_attrs = Collections.unmodifiableMap(rw_attrs);
    }

    public Map<String, String> getAttributes() {
        return ro_attrs;
    }

    public String getID() {
        return ro_attrs.get(ATTR_ID);
    }

    public String getServiceManagerID() {
        return ro_attrs.get(ATTR_SERVICE_MANGER_ID);
    }

    public String getAgentID() {
        return ro_attrs.get(ATTR_AGENT_ID);
    }

    public String getName() {
        return ro_attrs.get(ATTR_NAME);
    }

    public String getOSName() {
        return ro_attrs.get(ATTR_OS_NAME);
    }

    public String getUserName() {
        return ro_attrs.get(ATTR_USER_NAME);
    }

    public String getTransportName() {
        return ro_attrs.get(ATTR_TRANSPORT_NAME);
    }

    public IChannel openChannel() {
        return TransportManager.openChannel(this);
    }
}
