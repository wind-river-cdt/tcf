/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.core.util.persistence;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tcf.te.runtime.utils.net.IPAddressUtil;

/**
 * Persistable name utility method implementations.
 */
public class PersistableNameUtil {
	/* default */ final static Pattern pattern = Pattern.compile("TCP:([0-9\\.]+):[0-9]+", Pattern.CASE_INSENSITIVE); //$NON-NLS-1$

	/**
	 * Matches the given name against a set of patterns to isolate the IP address.
	 * If the address could be isolated and the address is an local host address,
	 * replace the address with the string "localhost".
	 *
	 * @param name
	 * @return
	 */
	public static String normalizeLocalhostAddress(String name) {
		Assert.isNotNull(name);

		// Let's see if the name matches the pattern
		Matcher matcher = pattern.matcher(name);
		if (matcher.matches()) {
			String ip = matcher.group(1);
			// If the IP address is for the local host, reconstruct the name
			// string to include "localhost" instead of the IP address
			if (IPAddressUtil.getInstance().isLocalHost(ip)) {
				name = name.replace(ip, "localhost"); //$NON-NLS-1$
			}
		}

		return name;
	}
}
