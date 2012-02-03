/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.core.delegates;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.tcf.te.launch.core.nls.Messages;

/**
 * Abstract launch configuration delegate implementation.
 */
public abstract class AbstractLaunchConfigurationDelegate extends LaunchConfigurationDelegate {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.LaunchConfigurationDelegate#buildProjects(org.eclipse.core.resources.IProject[], org.eclipse.core.runtime.IProgressMonitor)
	 * <p>
	 * This method is a copy of the super implementation, except it does not lock the workspace to perform the build.
	 * This is required to support "Edit while build is ongoing".
	 */
	@Override
	protected void buildProjects(final IProject[] projects, IProgressMonitor monitor) throws CoreException {
		IWorkspaceRunnable build = new IWorkspaceRunnable(){
			@Override
            public void run(IProgressMonitor pm) throws CoreException {
				SubMonitor localmonitor = SubMonitor.convert(pm, Messages.AbstractLaunchConfigurationDelegate_scoped_incremental_build, projects.length);
				try {
					for (int i = 0; i < projects.length; i++ ) {
						if (localmonitor.isCanceled()) {
							throw new OperationCanceledException();
						}
						projects[i].build(IncrementalProjectBuilder.INCREMENTAL_BUILD, localmonitor.newChild(1));
					}
				} finally {
					localmonitor.done();
				}
			}
		};
		ResourcesPlugin.getWorkspace().run(build, null, IWorkspace.AVOID_UPDATE, monitor);
	}

}
