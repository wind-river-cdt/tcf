/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.ui.viewer.dnd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.utils.elf.Elf.Attribute;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.tcf.te.core.cdt.elf.ElfUtils;
import org.eclipse.tcf.te.launch.core.bindings.LaunchConfigTypeBindingsManager;
import org.eclipse.tcf.te.launch.core.lm.LaunchConfigHelper;
import org.eclipse.tcf.te.launch.core.lm.LaunchManager;
import org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchManagerDelegate;
import org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification;
import org.eclipse.tcf.te.launch.core.persistence.launchcontext.LaunchContextsPersistenceDelegate;
import org.eclipse.tcf.te.launch.core.selection.LaunchSelection;
import org.eclipse.tcf.te.launch.core.selection.ProjectSelectionContext;
import org.eclipse.tcf.te.launch.core.selection.RemoteSelectionContext;
import org.eclipse.tcf.te.launch.core.selection.interfaces.ILaunchSelection;
import org.eclipse.tcf.te.launch.core.selection.interfaces.ISelectionContext;
import org.eclipse.tcf.te.launch.ui.activator.UIPlugin;
import org.eclipse.tcf.te.launch.ui.model.LaunchModel;
import org.eclipse.tcf.te.launch.ui.model.LaunchNode;
import org.eclipse.tcf.te.launch.ui.nls.Messages;
import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;
import org.eclipse.tcf.te.ui.jface.dialogs.OptionalMessageDialog;
import org.eclipse.tcf.te.ui.views.Managers;
import org.eclipse.tcf.te.ui.views.ViewsUtil;
import org.eclipse.tcf.te.ui.views.interfaces.ICategory;
import org.eclipse.tcf.te.ui.views.interfaces.IRoot;
import org.eclipse.tcf.te.ui.views.interfaces.IUIConstants;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.navigator.CommonDropAdapter;

/**
 * Common DND operation implementations.
 */
public class CommonDnD {

	/**
	 * If the current selection is draggable.
	 *
	 * @param selection The currently selected nodes.
	 * @return true if it is draggable.
	 */
	public boolean isDraggable(IStructuredSelection selection) {
		if (selection == null || selection.isEmpty()) {
			return false;
		}
		Object[] objects = selection.toArray();
		for (Object object : objects) {
			if (!isDraggableObject(translateObject(object))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Translate the object.
	 * @param object
	 * @return
	 */
	private Object translateObject(Object object) {
		if (object == null || object instanceof LaunchNode || object instanceof IModelNode || object instanceof ICategory) {
			return object;
		}
		IResource resource = (IResource)Platform.getAdapterManager().loadAdapter(object, IResource.class.getName());
		if (resource != null) {
			IPath location = resource.getLocation();
			if (location != null) {
				try {
					int elfType = ElfUtils.getELFType(location.toFile());
					if (elfType == Attribute.ELF_TYPE_EXE || elfType == Attribute.ELF_TYPE_OBJ) {
						return resource;
					}
				}
				catch (Exception e) {
				}
			}
		}
		return null;
	}

	/**
	 * If the specified object is a draggable element.
	 *
	 * @param object The object to be dragged.
	 * @return true if it is draggable.
	 */
	private boolean isDraggableObject(Object object) {
		return (object instanceof LaunchNode && ((LaunchNode)object).getLaunchConfiguration() != null) ||
						(!(object instanceof LaunchNode) && object instanceof IModelNode) ||
						object instanceof IResource;
	}

	/**
	 * Validate dropping when the elements being dragged are local selection.
	 *
	 * @param dropAdapter The common drop adapter.
	 * @param target The target object.
	 * @param operation The DnD operation.
	 * @param transferType The transfered data type.
	 *
	 * @return true if it is valid for dropping.
	 */
	public boolean isValidDnD(CommonDropAdapter dropAdapter, Object target, int operation, TransferData transferType) {
		boolean valid = false;

		int overrideOperation = -1;

		IStructuredSelection selection = null;
		if (LocalSelectionTransfer.getTransfer().isSupportedType(transferType)) {
			LocalSelectionTransfer transfer = LocalSelectionTransfer.getTransfer();
			selection = (IStructuredSelection) transfer.getSelection();
		}
		if (selection == null) {
			return false;
		}

		boolean allow = true;
		target = translateObject(target);
		Iterator<?> iterator = selection.iterator();
		boolean isCopy = (operation & DND.DROP_COPY) != 0;
		while (iterator.hasNext() && allow) {
			Object from = translateObject(iterator.next());
			if (from instanceof LaunchNode) {
				LaunchNode fromNode = (LaunchNode)from;
				if (target instanceof LaunchNode) {
					//					LaunchNode toNode = (LaunchNode)target;
					allow = false;
				}
				else if (target instanceof IModelNode) {
					IModelNode toContext = (IModelNode)target;
					allow &= internalValidate(fromNode, null, toContext, true, isCopy);
				}
				else if (target instanceof IResource) {
					IResource toResource = (IResource)target;
					allow &= internalValidate(fromNode, toResource, null, true, isCopy);
				}
				else if (target instanceof ICategory) {
					ICategory toCategory = (ICategory)target;
					overrideOperation = DND.DROP_LINK;
					allow &= fromNode.getLaunchConfiguration() != null && IUIConstants.ID_CAT_FAVORITES.equals(toCategory.getId()) &&
									!Managers.getCategoryManager().belongsTo(toCategory.getId(), LaunchModel.getCategoryId(fromNode.getLaunchConfiguration()));
				}
				else if (target instanceof IRoot) {
					allow &= fromNode.getModel().getModelRoot() instanceof ICategory && fromNode.getLaunchConfiguration() != null &&
									Managers.getCategoryManager().belongsTo(((ICategory)fromNode.getModel().getModelRoot()).getId(), LaunchModel.getCategoryId(fromNode.getLaunchConfiguration()));
				}
				else {
					allow = false;
				}
			}
			else if (from instanceof IModelNode) {
				IModelNode fromContext = (IModelNode)from;
				if (target instanceof LaunchNode) {
					LaunchNode toNode = (LaunchNode)target;
					overrideOperation = DND.DROP_MOVE;
					allow &= internalValidate(toNode, null, fromContext, true, false);
				}
				else {
					allow = false;
				}
			}
			else if (from instanceof IResource) {
				IResource fromResource = (IResource)from;
				if (target instanceof LaunchNode) {
					LaunchNode toNode = (LaunchNode)target;
					allow &= internalValidate(toNode, fromResource, null, !isCopy, false);
				}
				else if (target instanceof IModelNode) {
					IModelNode toContext = (IModelNode)target;
					ILaunchSelection launchSel = new LaunchSelection(null, new ISelectionContext[]{
									new RemoteSelectionContext(toContext, new Object[]{toContext}, true),
									new ProjectSelectionContext(fromResource.getProject(), new Object[]{fromResource.getLocation()}, true)
					});
					String[] typeIds = LaunchConfigTypeBindingsManager.getInstance().getValidLaunchConfigTypes(launchSel);
					allow &= typeIds != null && typeIds.length > 0;
				}
				else {
					allow = false;
				}
			}
			else {
				allow = false;
			}
		}
		valid = allow;

		if (dropAdapter != null) {
			if (!valid) {
				dropAdapter.overrideOperation(DND.DROP_NONE);
			}
			else if (overrideOperation > -1) {
				dropAdapter.overrideOperation(overrideOperation);
			}
		}

		return valid;
	}

	private boolean internalValidate(LaunchNode node, IResource resource, IModelNode context, boolean replaceData, boolean duplicateConfig) {
		if (node.getLaunchConfiguration() == null) {
			return false;
		}
		if (resource != null) {
			if (!LaunchConfigTypeBindingsManager.getInstance().isValidLaunchConfigType(
							node.getLaunchConfigurationType().getIdentifier(),
							new LaunchSelection(null, new ProjectSelectionContext(resource.getProject(), new Object[]{resource.getLocation()}, true)))) {
				return false;
			}
		}
		if (context != null) {
			if (!LaunchConfigTypeBindingsManager.getInstance().isValidLaunchConfigType(
							node.getLaunchConfigurationType().getIdentifier(),
							new LaunchSelection(null, new RemoteSelectionContext(context, true)))) {
				return false;
			}
			IModelNode[] oldContexts = LaunchContextsPersistenceDelegate.getLaunchContexts(node.getLaunchConfiguration());
			if (oldContexts != null && Arrays.asList(oldContexts).contains(context) && !duplicateConfig) {
				return false;
			}
		}

		return true;

	}
	/**
	 * Perform the drop operation over dragged selection.
	 *
	 * @param dropAdapter The common drop adapter.
	 * @param target The target Object to be moved to.
	 * @param operation The current DnD operation.
	 * @param selection The local selection being dropped.
	 * @return true if the dropping is successful.
	 */
	public boolean doDnD(CommonDropAdapter dropAdapter, Object target, int operation, IStructuredSelection selection) {
		boolean success = true;
		target = translateObject(target);
		Iterator<?> iterator = selection.iterator();
		boolean isCopy = (operation & DND.DROP_COPY) != 0;
		while (iterator.hasNext()) {
			Object from = translateObject(iterator.next());
			if (from instanceof LaunchNode) {
				LaunchNode fromNode = (LaunchNode)from;
				if (target instanceof LaunchNode) {
					//					LaunchNode toNode = (LaunchNode)target;
					success = false;
				}
				else if (target instanceof IModelNode) {
					IModelNode toContext = (IModelNode)target;
					success &= internalDrop(fromNode, null, toContext, true, isCopy);
				}
				else if (target instanceof IResource) {
					IResource toResource = (IResource)target;
					success &= internalDrop(fromNode, toResource, null, true, isCopy);
				}
				else if (target instanceof ICategory) {
					ICategory toCategory = (ICategory)target;
					success &= Managers.getCategoryManager().add(toCategory.getId(), LaunchModel.getCategoryId(fromNode.getLaunchConfiguration()));
				}
				else if (target instanceof IRoot) {
					success &= Managers.getCategoryManager().remove(((ICategory)fromNode.getModel().getModelRoot()).getId(),
									LaunchModel.getCategoryId(fromNode.getLaunchConfiguration()));
				}
				else {
					success = false;
				}
			}
			else if (from instanceof IModelNode) {
				IModelNode fromContext = (IModelNode)from;
				if (target instanceof LaunchNode) {
					LaunchNode toNode = (LaunchNode)target;
					success &= internalDrop(toNode, null, fromContext, true, false);
				}
				else {
					success = false;
				}
			}
			else if (from instanceof IResource) {
				IResource fromResource = (IResource)from;
				if (target instanceof LaunchNode) {
					LaunchNode toNode = (LaunchNode)target;
					success &= internalDrop(toNode, fromResource, null, !isCopy, false);
				}
				else if (target instanceof IModelNode) {
					IModelNode toContext = (IModelNode)target;
					ILaunchSelection launchSel = new LaunchSelection(null, new ISelectionContext[]{
									new RemoteSelectionContext(toContext, new Object[]{toContext}, true),
									new ProjectSelectionContext(fromResource.getProject(), new Object[]{fromResource.getLocation()}, true)
					});
					String[] typeIds = LaunchConfigTypeBindingsManager.getInstance().getValidLaunchConfigTypes(launchSel);
					if (typeIds != null && typeIds.length > 0) {
						String typeId = null;
						if (typeIds.length == 1) {
							typeId = typeIds[0];
						}
						else {
							typeId = askForLaunchType(typeIds);
						}
						if (typeId != null) {
							ILaunchConfigurationType type = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(typeId);
							String[] modes = LaunchConfigHelper.getLaunchConfigTypeModes(type, false);
							if (modes != null && modes.length > 0) {
								String mode = null;
								if (modes.length == 1) {
									mode = modes[0];
								}
								else {
									mode = askForLaunchMode(type, modes);
								}
								if (mode != null) {
									success &= openLaunchConfigDialog(type, mode, new LaunchSelection(mode, launchSel.getSelectedContexts()));
								}
							}
						}
					}
				}
				else {
					success = false;
				}
			}
			else {
				success = false;
			}
		}
		// Fire a refresh of the view
		ViewsUtil.refresh(IUIConstants.ID_EXPLORER);
		return success;
	}

	private String askForLaunchType(String[] typeIds) {
		List<ILaunchConfigurationType> types = new ArrayList<ILaunchConfigurationType>();
		for (String typeId : typeIds) {
			types.add(DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(typeId));
		}
		ListDialog dialog = new ListDialog(UIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell());
		dialog.setTitle(Messages.CommonDnD_launchType_dialog_title);
		dialog.setMessage(Messages.CommonDnD_launchType_dialog_message);
		dialog.setInput(types.toArray());
		dialog.setContentProvider(new ArrayContentProvider());
		dialog.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((ILaunchConfigurationType)element).getName();
			}
		});
		dialog.setInitialSelections(new Object[]{types.get(0)});
		if (dialog.open() == Window.OK) {
			return ((ILaunchConfigurationType)dialog.getResult()[0]).getIdentifier();
		}
		return null;
	}

	private String askForLaunchMode(ILaunchConfigurationType type, String[] modes) {
		List<String> modeLabels = new ArrayList<String>();
		int defaultIndex = 0;
		for (String mode : modes) {
			ILaunchMode launchMode = DebugPlugin.getDefault().getLaunchManager().getLaunchMode(mode);
			modeLabels.add(launchMode.getLabel());
			if (mode.equals(ILaunchManager.DEBUG_MODE)) {
				defaultIndex = modeLabels.size()-1;
			}
		}
		if (modeLabels.size() > 0) {
			modeLabels.add(IDialogConstants.CANCEL_LABEL);
			OptionalMessageDialog dialog = new OptionalMessageDialog(
							UIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell(),
							Messages.CommonDnD_launchMode_dialog_title,
							null,
							NLS.bind(Messages.CommonDnD_launchMode_dialog_message, type.getName()),
							MessageDialog.QUESTION,
							modeLabels.toArray(new String[modeLabels.size()]),
							defaultIndex,
							null, null);
			int result = dialog.open();
			if (result >= IDialogConstants.INTERNAL_ID) {
				return modes[result - IDialogConstants.INTERNAL_ID];
			}
		}
		return null;
	}

	private boolean openLaunchConfigDialog(ILaunchConfigurationType type, String mode, ILaunchSelection launchSel) {
		try {
			ILaunchManagerDelegate delegate = LaunchManager.getInstance().getLaunchManagerDelegate(type, mode);
			if (delegate != null) {
				// create an empty launch configuration specification to initialize all attributes with their default defaults.
				ILaunchSpecification launchSpec = delegate.getLaunchSpecification(type.getIdentifier(), launchSel);
				// initialize the new launch config.
				// ignore validation result of launch spec - init as much attributes as possible
				if (launchSpec != null) {
					ILaunchConfiguration[] launchConfigs = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations(type);
					launchConfigs = delegate.getMatchingLaunchConfigurations(launchSpec, launchConfigs);

					ILaunchConfiguration config = launchConfigs != null && launchConfigs.length > 0 ? launchConfigs[0] : null;
					config = LaunchManager.getInstance().createOrUpdateLaunchConfiguration(config, launchSpec);

					ILaunchGroup launchGroup = DebugUITools.getLaunchGroup(config, mode);
					DebugUITools.openLaunchConfigurationDialogOnGroup(UIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell(),
									new StructuredSelection(config), launchGroup.getIdentifier());
					return true;
				}
			}
		}
		catch (Exception e) {
			return false;
		}
		return false;
	}

	private boolean internalDrop(LaunchNode node, IResource resource, IModelNode context, boolean replaceData, boolean duplicateConfig) {
		try {
			ILaunchManagerDelegate delegate = LaunchManager.getInstance().getLaunchManagerDelegate(node.getLaunchConfigurationType(), ""); //$NON-NLS-1$
			ILaunchConfigurationWorkingCopy wc = node.getLaunchConfiguration().getWorkingCopy();
			if (resource != null) {
				delegate.updateLaunchConfig(wc, new ProjectSelectionContext(resource.getProject(), new Object[]{resource.getLocation()}, true), replaceData);
			}
			if (context != null) {
				delegate.updateLaunchConfig(wc, new RemoteSelectionContext(context, new Object[]{context}, true), replaceData);
			}
			if (duplicateConfig) {
				wc = wc.copy(LaunchConfigHelper.getUniqueLaunchConfigName(delegate.getDefaultLaunchName(wc)));
			}
			else {
				String newName = LaunchConfigHelper.getUniqueLaunchConfigName(delegate.getDefaultLaunchName(wc)).replaceAll("\\([0-9]+\\)$", "").trim(); //$NON-NLS-1$ //$NON-NLS-2$
				String oldName = wc.getName().replaceAll("\\([0-9]+\\)$", "").trim(); //$NON-NLS-1$ //$NON-NLS-2$;
				if (!newName.equals(oldName)) {
					wc.rename(LaunchConfigHelper.getUniqueLaunchConfigName(newName));
				}
			}
			wc.doSave();
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}
}
