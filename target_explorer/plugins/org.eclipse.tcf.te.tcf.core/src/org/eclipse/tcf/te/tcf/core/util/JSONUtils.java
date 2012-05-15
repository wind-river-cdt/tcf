/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.core.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * JSON utilities helper implementations.
 */
public final class JSONUtils {

	/**
	 * Decodes a string object from the given byte array.
	 * <p>
	 * Used if services sends plain strings instead of encoding them through JSON.
	 *
	 * @param data The byte array or <code>null</code>.
	 * @return The decoded string or <code>null</code>.
	 */
	public static String decodeStringFromByteArray(byte[] data) {
		String args = null;
		if (data != null) {
			StringBuilder builder = new StringBuilder();
			InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(data));
			try {
				int c = reader.read();
				while (c != -1) {
					builder.append(c != 0 ? Character.valueOf((char)c).charValue() : ' ');
					c = reader.read();
				}
			} catch (IOException ex) { /* ignored on purpose */ }

			if (builder.length() > 0) args = builder.toString().trim();
		}
		return args;
	}
}
