/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.processes.ui.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.processes.ui.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.processes.ui.interfaces.INodeStateListener;

/**
 * The process model manager used to create and manage the process model for peers.
 */
public class ProcessModelManager {
	/* default */static final String PROCESS_ROOT_KEY = UIPlugin.getUniqueIdentifier() + ".process.root"; //$NON-NLS-1$
	// The single instance of ProcessModelManager
	private static final ProcessModelManager instance = new ProcessModelManager();

	/**
	 * Get the single instance of ProcessModelManager.
	 * 
	 * @return The single instance of ProcessModelManager
	 */
	public static ProcessModelManager getInstance() {
		return instance;
	}

	// All of the process tree models used for global notification.
	private List<ProcessModel> processModels;
	
	/**
	 * Private constructor.
	 */
	private ProcessModelManager() {
		processModels = Collections.synchronizedList(new ArrayList<ProcessModel>());
	}

	/**
	 * Add a process tree model to the list.
	 * 
	 * @param model The new process tree model.
	 */
	private void addModel(ProcessModel model) {
		processModels.add(model);
	}

	/**
	 * Remove the listener from the models.
	 * 
	 * @param listener The listener.
	 */
	public void removeListener(INodeStateListener listener) {
		for (ProcessModel model : processModels) {
			model.removeNodeStateListener(listener);
		}
	}

	/**
	 * Get all the process models now created.
	 * 
	 * @return All the process models.
	 */
	public List<ProcessModel> getProcessModels() {
		return processModels;
	}

	/**
	 * Get the process model stored in the peer model.
	 * If there's no process model yet, create a new process model. 
	 * 
	 * @param peerModel The target's peer model.
	 * @return The process model representing the process.
	 */
	public ProcessModel getProcessModel(final IPeerModel peerModel) {
		if (peerModel != null) {
			if (Protocol.isDispatchThread()) {
				ProcessModel model = (ProcessModel) peerModel.getProperty(PROCESS_ROOT_KEY);
				if (model == null) {
					model = new ProcessModel(peerModel);
					addModel(model);
					peerModel.setProperty(PROCESS_ROOT_KEY, model);
				}
				return model;
			}
			final AtomicReference<ProcessModel> reference = new AtomicReference<ProcessModel>();
			Protocol.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					reference.set(getProcessModel(peerModel));
				}
			});
			return reference.get();
		}
		return null;
	}
}
