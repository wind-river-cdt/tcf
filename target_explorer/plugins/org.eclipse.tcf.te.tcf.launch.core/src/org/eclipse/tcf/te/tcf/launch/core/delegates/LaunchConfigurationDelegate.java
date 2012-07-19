/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.launch.core.delegates;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.launch.core.lm.interfaces.ICommonLaunchAttributes;
import org.eclipse.tcf.util.TCFTask;

/**
 * Default tcf launch configuration delegate implementation.
 * <p>
 * The launch configuration delegate implements the bridge between the native Eclipse launch
 * configuration framework and the stepper engine. The delegate is standard for all
 * launch configurations which supports extensible and modularized launching.
 * <p>
 * <b>Implementation Details</b>:<br>
 * <ul>
 * <li>The launch configuration delegate signals the completion of the launch sequence via
 * the custom {@link ILaunch} attribute {@link ICommonLaunchAttributes#ILAUNCH_ATTRIBUTE_LAUNCH_SEQUENCE_COMPLETED}.</li>
 * <li>The launch configuration delegates enforces the removal of the launch from the Eclipse
 * debug platforms launch manager if the progress monitor is set to canceled or an status with
 * severity {@link IStatus#CANCEL} had been thrown by the stepper implementation.</li>
 * <li>The launch configuration delegate creates launches of type {@link Launch}.</li>
 * </ul>
 */
@SuppressWarnings("restriction")
public class LaunchConfigurationDelegate extends org.eclipse.tcf.te.launch.core.lm.internal.LaunchConfigurationDelegate {

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.delegates.LaunchConfigurationDelegate#getLaunch(org.eclipse.debug.core.ILaunchConfiguration, java.lang.String)
	 */
	@Override
	public ILaunch getLaunch(final ILaunchConfiguration configuration, final String mode) throws CoreException {
		return new TCFTask<ILaunch>() {
			int cnt;
			@Override
			public void run() {
				// Need to delay at least one dispatch cycle to work around
				// a possible racing between thread that calls getLaunch() and
				// the process of activation of other TCF plug-ins.
				if (cnt++ < 2) {
					Protocol.invokeLater(this);
				}
				else {
					done(new Launch(configuration, mode));
				}
			}
		}.getE();
	}
}
