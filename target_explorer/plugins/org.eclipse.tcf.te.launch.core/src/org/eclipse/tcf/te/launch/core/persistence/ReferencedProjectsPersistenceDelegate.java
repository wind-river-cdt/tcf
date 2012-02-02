/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.core.persistence;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.tcf.te.launch.core.lm.interfaces.ICommonLaunchAttributes;

/**
 * Launch framework referenced projects persistence delegate.
 */
public class ReferencedProjectsPersistenceDelegate {

	/**
	 * Interface declaring the public accessible attributes or a project reference.
	 */
	public static interface IProjectReference {

		/**
		 * Returns the name of the referenced project.
		 *
		 * @return The name of the referenced project.
		 */
		public String getName();

		/**
		 * Returns the referenced project instance.
		 *
		 * @return The referenced project instance or <code>null</code> if not in the workspace.
		 */
		public IProject getProject();

		/**
		 * Returns the enabled state of the project reference.
		 *
		 * @return <code>True</code> if the project reference is enabled, <code>false</code> otherwise.
		 */
		public boolean isEnabled();

		/**
		 * Set's the project reference enabled state.
		 *
		 * @param enabled <code>True</code> to enable the project reference, <code>false</code> otherwise.
		 */
		public void setEnabled(boolean enabled);
	}

	/**
	 * Internal default {@link IProjectReference} implementation.
	 */
	private static class ProjectReference implements IProjectReference {
		private final String name;
		private boolean enabled;
		private final IProject project;

		/**
		 * Constructor
		 *
		 * @param name The project name. Must not be <code>null</code>.
		 * @param project The project instance or <code>null</code>.
		 * @param enabled The enabled state.
		 */
		public ProjectReference(String name, IProject project, boolean enabled) {
			Assert.isNotNull(name);

			this.name = name;
			this.enabled = enabled;
			this.project = project;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.tcf.te.launch.core.persistence.ReferencedProjectsPersistenceDelegate.IProjectReference#getName()
		 */
		@Override
        public String getName() {
			return name;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.tcf.te.launch.core.persistence.ReferencedProjectsPersistenceDelegate.IProjectReference#getProject()
		 */
		@Override
        public IProject getProject() {
			return project;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.tcf.te.launch.core.persistence.ReferencedProjectsPersistenceDelegate.IProjectReference#isEnabled()
		 */
		@Override
        public boolean isEnabled() {
			return enabled;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.tcf.te.launch.core.persistence.ReferencedProjectsPersistenceDelegate.IProjectReference#setEnabled(boolean)
		 */
		@Override
        public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof IProjectReference) {
				return name.equals(((IProjectReference)obj).getName());
			}
			return false;
		}
	}

	/**
	 * Returns a new project reference instance.
	 *
	 * @param name The project name. Must not be <code>null</code>.
	 * @param project The project instance or <code>null</code>.
	 * @param enabled The enabled state.
	 *
	 * @return The new project reference instance.
	 */
	public static IProjectReference create(String name, IProject project, boolean enabled) {
		return new ProjectReference(name, project, enabled);
	}

	/**
	 * Read the list of project name from the launch configuration attributes and transforms
	 * it into a list of project references.
	 *
	 * @param launchConfig The launch configuration. Must not be <code>null</code>.
	 *
	 * @return The list of project references or an empty list.
	 */
	public static List<IProjectReference> getProjects(ILaunchConfiguration launchConfig) {
		Assert.isNotNull(launchConfig);

		List<IProjectReference> references = new ArrayList<IProjectReference>();

		// Get the list of referenced project from the launch configuration
		List<String> projects = (List<String>)DefaultPersistenceDelegate.getAttribute(launchConfig, ICommonLaunchAttributes.ATTR_REFERENCED_PROJECTS, (List<String>)null);
		if (projects != null) {
			// Loop over the found project names and lookup the corresponding project instance.
			for (String name : projects) {
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
				IProjectReference ref = create(name, project, false);
				if (!references.contains(ref)) {
					references.add(ref);
				}
			}
		}

		// Get the list of referenced project and enabled for build from the launch configuration
		projects = (List<String>)DefaultPersistenceDelegate.getAttribute(launchConfig, ICommonLaunchAttributes.ATTR_PROJECTS_FOR_BUILD, (List<String>)null);
		if (projects != null) {
			// Loop over the found project names and lookup the corresponding project instance.
			for (String name : projects) {
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
				IProjectReference ref = create(name, project, true);
				// If already referenced, but not enabled, enable now.
				int i = references.indexOf(ref);
				if (i >= 0) {
					references.get(i).setEnabled(true);
				}
				else {
					references.add(ref);
				}
			}
		}

		return references;
	}

	/**
	 * Writes the list of referenced projects to the given launch configuration working copy.
	 *
	 * @param wc The launch configuration working copy. Must not be <code>null</code>.
	 * @param projects The list of referenced projects or <code>null</code>.
	 */
	public static void setProjects(ILaunchConfigurationWorkingCopy wc, List<IProjectReference> projects) {
		Assert.isNotNull(wc);

		// If the project list is null or empty, we reset the attributes to use the default value
		if (projects == null || projects.isEmpty()) {
			DefaultPersistenceDelegate.setAttribute(wc, ICommonLaunchAttributes.ATTR_REFERENCED_PROJECTS, (List<String>)null);
			DefaultPersistenceDelegate.setAttribute(wc, ICommonLaunchAttributes.ATTR_PROJECTS_FOR_BUILD, (List<String>)null);
			return;
		}

		// The list instances
		List<String> projectsForBuild = new ArrayList<String>();
		List<String> refProjects = new ArrayList<String>();

		// Loop all project references and find the referenced projects and
		// the projects enabled for build before launch
		for (IProjectReference projectRef : projects) {
			refProjects.add(projectRef.getName());
			if (projectRef.isEnabled()) {
				projectsForBuild.add(projectRef.getName());
			}
		}

		// The attribute "projects for build" is set always.
		DefaultPersistenceDelegate.setAttribute(wc, ICommonLaunchAttributes.ATTR_PROJECTS_FOR_BUILD, projectsForBuild);
		// The referenced projects list contains only the excluded/disabled projects. Therefore
		// reset the attribute if the lists are equal
		if (projectsForBuild.size() == refProjects.size()) {
			DefaultPersistenceDelegate.setAttribute(wc, ICommonLaunchAttributes.ATTR_REFERENCED_PROJECTS, (List<String>)null);
		}
		else {
			DefaultPersistenceDelegate.setAttribute(wc, ICommonLaunchAttributes.ATTR_REFERENCED_PROJECTS, refProjects);
		}
	}
}
