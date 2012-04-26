/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.ui.navigator.dnd;

import java.util.Iterator;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.ui.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.ui.internal.categories.CategoryManager;
import org.eclipse.tcf.te.tcf.ui.internal.preferences.IPreferenceConsts;
import org.eclipse.tcf.te.ui.views.Managers;
import org.eclipse.tcf.te.ui.views.ViewsUtil;
import org.eclipse.tcf.te.ui.views.interfaces.ICategory;
import org.eclipse.tcf.te.ui.views.interfaces.IRoot;
import org.eclipse.tcf.te.ui.views.interfaces.IUIConstants;
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
	public static boolean isDraggable(IStructuredSelection selection) {
		if (selection.isEmpty()) {
			return false;
		}
		Object[] objects = selection.toArray();
		for (Object object : objects) {
			if (!isDraggableObject(object)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * If the specified object is a draggable element.
	 *
	 * @param object The object to be dragged.
	 * @return true if it is draggable.
	 */
	private static boolean isDraggableObject(Object object) {
		return object instanceof IPeerModel;
	}

	/**
	 * Perform the drop operation over dragged selection.
	 *
	 * @param target The target Object to be moved to.
	 * @param operations The current DnD operation.
	 * @param selection The local selection being dropped.
	 * @return true if the dropping is successful.
	 */
	public static boolean dropLocalSelection(Object target, int operations, IStructuredSelection selection) {
		if ((operations & DND.DROP_MOVE) != 0) {
			if (target instanceof ICategory) {
				ICategory hovered = (ICategory) target;
				if (IUIConstants.ID_CAT_FAVORITES.equals(hovered.getId())) {
					// Mark the peer nodes as favorite
					Iterator<?> iterator = selection.iterator();
					while (iterator.hasNext()) {
						Object element = iterator.next();
						if (!(element instanceof IPeerModel)) continue;
						Managers.getCategoryManager().add(hovered.getId(), ((IPeerModel)element).getPeerId());
					}
					// Fire a refresh of the view
					ViewsUtil.refresh(IUIConstants.ID_EXPLORER);
				}
				else if (IUIConstants.ID_CAT_MY_TARGETS.equals(hovered.getId())) {
					// Create a static copy of the dropped peer node
					Iterator<?> iterator = selection.iterator();
					while (iterator.hasNext()) {
						Object element = iterator.next();
						if (!(element instanceof IPeerModel)) continue;
						CategoryManager.getInstance().addToMyTargets((IPeerModel)element);
					}
					// Fire a refresh of the view
					ViewsUtil.refresh(IUIConstants.ID_EXPLORER);
				}
			} else if (target instanceof IRoot) {
				// Remove the peer nodes from the favorites list
				Iterator<?> iterator = selection.iterator();
				while (iterator.hasNext()) {
					Object element = iterator.next();
					if (!(element instanceof IPeerModel)) continue;
					Managers.getCategoryManager().remove(IUIConstants.ID_CAT_FAVORITES, ((IPeerModel)element).getPeerId());
				}
				// Fire a refresh of the view
				ViewsUtil.refresh(IUIConstants.ID_EXPLORER);
			}
		}
		return false;
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
	public static boolean validateLocalSelectionDrop(CommonDropAdapter dropAdapter, Object target, int operation, TransferData transferType) {
		boolean isLinkMode = UIPlugin.getDefault().getPreferenceStore().getBoolean(IPreferenceConsts.PREF_FAVORITES_CATEGORY_MODE_LINK);

		int overrideOperation = -1;
		boolean valid = false;

		// If not in copy mode, the operation should be always "DROP_MOVE"
		if (!isLinkMode && (operation & DND.DROP_MOVE) == 0) {
			overrideOperation = DND.DROP_MOVE;
		}

		LocalSelectionTransfer transfer = LocalSelectionTransfer.getTransfer();
		IStructuredSelection selection = (IStructuredSelection) transfer.getSelection();

		if (target instanceof ICategory) {
			ICategory hovered = (ICategory) target;

			// Dragging to the "Neighborhood" category is not possible
			if (!IUIConstants.ID_CAT_NEIGHBORHOOD.equals(hovered.getId())) {
				valid = true;

				// If the target is the "Favorites" category and copy mode is on,
				// force DROP_LINK operation
				if (isLinkMode && IUIConstants.ID_CAT_FAVORITES.equals(hovered.getId())
						&& (operation & DND.DROP_LINK) == 0) {
					overrideOperation = DND.DROP_LINK;
				}
			}
		} else if (target instanceof IRoot) {
			// Allow to drag into empty space from favorite nodes only
			boolean allow = true;
			Iterator<?> iterator = selection.iterator();
			while (iterator.hasNext()) {
				Object element = iterator.next();
				if (!(element instanceof IPeerModel)) {
					allow = false;
					break;
				}
				if (!Managers.getCategoryManager().belongsTo(IUIConstants.ID_CAT_FAVORITES, ((IPeerModel)element).getPeerId())) {
					allow = false;
					break;
				}
			}
			valid = allow;
		}

		if (overrideOperation != -1 && dropAdapter != null) {
			dropAdapter.overrideOperation(overrideOperation);
		}

		return valid;
	}
}
