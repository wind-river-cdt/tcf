/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tests.tcf.filesystem.utils;

import org.eclipse.tcf.te.tcf.filesystem.internal.UserAccount;
import org.eclipse.tcf.te.tcf.filesystem.internal.utils.UserManager;


@SuppressWarnings("restriction")
public class UserManagerTest extends UtilsTestBase {
	public void testUserAccount() {
		assertNotNull(peerModel);
		UserAccount user = UserManager.getInstance().getUserAccount(peerModel);
		assertNotNull(user);
	}
}
