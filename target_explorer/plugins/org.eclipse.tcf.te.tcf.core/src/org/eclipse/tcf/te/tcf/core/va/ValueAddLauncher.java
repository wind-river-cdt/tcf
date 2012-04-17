/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.core.va;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.te.runtime.interfaces.IDisposable;
import org.eclipse.tcf.te.runtime.processes.ProcessLauncher;
import org.eclipse.tcf.te.runtime.processes.ProcessOutputReaderThread;
import org.eclipse.tcf.te.runtime.utils.Host;
import org.eclipse.tcf.te.tcf.core.activator.CoreBundleActivator;
import org.eclipse.tcf.te.tcf.core.interfaces.tracing.ITraceIds;
import org.eclipse.tcf.te.tcf.core.nls.Messages;
import org.osgi.framework.Bundle;

/**
 * Value-add launcher implementation.
 */
public class ValueAddLauncher extends ProcessLauncher implements IDisposable {
	// The target peer id
	private final String id;
	// The path of the value-add to launch
	private final IPath path;
	// The value-add id
	private final String valueAddId;
	// The process handle
	private Process process;
	// The process output reader
	private ProcessOutputReaderThread outputReader;

	/**
	 * Constructor.
	 *
	 * @param id The target peer id. Must not be <code>null</code>.
	 * @param path The value-add path. Must not be <code>null</code>.
	 * @param valueAddId The value-add id. Must not be <code>null</code>.
	 */
	public ValueAddLauncher(String id, IPath path, String valueAddId) {
		super(null, null, 0);

		Assert.isNotNull(id);
		this.id = id;
		Assert.isNotNull(path);
		this.path = path;
		Assert.isNotNull(valueAddId);
		this.valueAddId = valueAddId;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.interfaces.IDisposable#dispose()
	 */
	@Override
	public void dispose() {
		if (process != null) {
			process.destroy();
			process = null;
		}
	}

	/**
	 * Returns the process handle.
	 *
	 * @return The process handle or <code>null</code>.
	 */
	public Process getProcess() {
		return process;
	}

	/**
	 * Returns the process output reader.
	 *
	 * @return The process output reader or <code>null</code>.
	 */
	public ProcessOutputReaderThread getOutputReader() {
		return outputReader;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.processes.ProcessLauncher#launch()
	 */
	@Override
	public void launch() throws Throwable {
		IPath dir = path.removeLastSegments(1);
		String cmd = Host.isWindowsHost() ? path.toOSString() : "./" + path.lastSegment(); //$NON-NLS-1$

		// Build up the command
		List<String> command = new ArrayList<String>();
		command.add(cmd);
		addToCommand(command, "-I180"); //$NON-NLS-1$
		addToCommand(command, "-S"); //$NON-NLS-1$
		addToCommand(command, "-sTCP::;ValueAdd=1"); //$NON-NLS-1$

		// Enable logging?
		if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITraceIds.VA_LOGGING_ENABLE)) {
			// Calculate the location and name of the log file
			Bundle bundle = Platform.getBundle("org.eclipse.tcf.te.tcf.log.core"); //$NON-NLS-1$
			IPath location = bundle != null ? Platform.getStateLocation(bundle) : null;
			if (location != null) {
				location = location.append(".logs"); //$NON-NLS-1$

				String name = "Output_" + valueAddId + "_" + id + ".log"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				name = name.replaceAll("\\s", "_"); //$NON-NLS-1$ //$NON-NLS-2$
				name = name.replaceAll("[:/\\;,]", "_"); //$NON-NLS-1$ //$NON-NLS-2$

				location = location.append(name);
				addToCommand(command, "-L" + location.toString()); //$NON-NLS-1$

				String level = Platform.getDebugOption(CoreBundleActivator.getUniqueIdentifier() + "/" + ITraceIds.VA_LOGGING_LEVEL); //$NON-NLS-1$
				if (level != null && !"".equals(level.trim())) { //$NON-NLS-1$
					addToCommand(command, "-l" + level.trim()); //$NON-NLS-1$
				}
			}
		}

		if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_CHANNEL_MANAGER)) {
			CoreBundleActivator.getTraceHandler().trace(NLS.bind(Messages.ValueAddLauncher_launch_command, new Object[] { command, id, valueAddId }),
														0, ITraceIds.TRACE_CHANNEL_MANAGER, IStatus.INFO, this);
		}

		// Launch the value-add
		process = Runtime.getRuntime().exec(command.toArray(new String[command.size()]), null, dir.toFile());

		// Launch the process output reader
		outputReader = new ProcessOutputReaderThread(null, new InputStream[] { process.getInputStream() });
		outputReader.start();
	}

	/**
	 * Adds the given argument to the given command.
	 * <p>
	 * Custom value add launcher implementations may overwrite this method to
	 * validate and/or modify the command used to launch the value-add.
	 *
	 * @param command The command. Must not be <code>null</code>.
	 * @param arg The argument. Must not be <code>null</code>.
	 */
	protected void addToCommand(List<String> command, String arg) {
		Assert.isNotNull(command);
		Assert.isNotNull(arg);
		command.add(arg);
	}

}
